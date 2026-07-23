mod api_utility;

use resin::{Database, Error, Result};
use serde_json::{Map, Value, json};
use std::time::{SystemTime, UNIX_EPOCH};
use tokio::join;
use tokio_postgres::Client;

pub use api_utility::ApiUtility;

/// Runs the CampbellCloud API process ("RWIS API").
/// Retrieves API credentials from database, fetches data from API,
/// formats it into weather sensor samples, then updates the database.
pub async fn run(db: Option<Database>) -> Result<()> {
    let db = db.ok_or(Error::InvalidConfig("Database is None"))?;
    let client = db.client().await?;

    // Read IRIS server properties for API and database credentials
    let (host, id, user, pass) = get_creds_from_db(&client).await?;

    let mut api_util =
        api_utility::ApiUtility::new(&host, &user, &pass, &id).await;

    // Get all the samples first
    if let Ok(serials) = get_serial_numbers(&client).await {
        let mut samples = vec![];
        let mut fails = vec![];
        for s in serials {
            if let Some((sample, time, up_to_date)) =
                build_sample_json(&mut api_util, &s).await
            {
                // Update the sample with the most up-to-date data...
                samples.push((s.clone(), sample, time));
                // ...but if it's outdated (>1 day), also mark a failure
                if !up_to_date {
                    fails.push((s, Some(time)));
                }
            } else {
                fails.push((s, None));
            }
        }

        // Then push them at same time
        if !samples.is_empty() {
            insert_samples(&client, samples).await?;
        }
        if !fails.is_empty() {
            insert_fails(&client, fails).await?;
        }
    }
    Ok(())
}

/// Gets host, organization ID, user, and password for the CampbellCloud API.
async fn get_creds_from_db(
    client: &Client,
) -> Result<(String, String, String, String)> {
    let links = client
        .query(
            "
            SELECT name, uri
            FROM iris.comm_link
            WHERE comm_config IN
            (
                SELECT name
                FROM iris.comm_config
                WHERE description='CampbellCloud'
            );
            ",
            &[],
        )
        .await?;
    // All CampbellCloud controllers should be on one link
    let cl_name: &str = links[0].try_get("name")?;

    // org_id@https://...
    if let Some((org_id, uri)) =
        links[0].try_get::<_, &str>("uri")?.split_once("@")
    {
        let passwords = client
            .query(
                "
                SELECT password
                FROM iris.controller
                WHERE comm_link=$1
                ORDER BY drop_id
                ",
                &[&cl_name],
            )
            .await?;
        for password in &passwords {
            if let Some((user, pass)) =
                password.try_get::<_, &str>(0)?.split_once(":")
            {
                return Ok((
                    uri.to_owned(),
                    org_id.to_owned(),
                    user.to_owned(),
                    pass.to_owned(),
                ));
            }
        }
    }

    Err(Error::InvalidConfig(
        "Could not get credentials from database",
    ))
}

/// Gets the serial numbers stored in the alt_id column of the database.
async fn get_serial_numbers(client: &Client) -> Result<Vec<String>> {
    let mut serials = vec![];

    for row in client
        .query("SELECT alt_id FROM iris._weather_sensor", &[])
        .await?
    {
        let alt_id: Option<String> = row.try_get(0)?;

        if let Some(id) = alt_id
            && !id.is_empty()
        {
            serials.push(id);
        }
    }
    Ok(serials)
}

/// Builds the sample for one sensor designated by its serial number.
/// Returns data, sample time, and whether the data is less than one day old.
async fn build_sample_json(
    api: &mut ApiUtility,
    serial_number: &str,
) -> Option<(Value, u64, bool)> {
    let _ = api.update_auth().await;
    let id_val = api.get_id_from_serial(serial_number).await?;
    let id = id_val.as_str()?;
    let mut time: u64 = 0;
    let mut s = Map::new();

    let dpt_fut = api.get_asset_last_datapoint_value(id, "DewPointTemp");
    let st_fut = api.get_asset_last_datapoint_value(id, "SurfaceTemp");
    let rh_fut = api.get_asset_last_datapoint_value(id, "RH");
    let at_fut = api.get_asset_last_datapoint_value(id, "AirTemp");
    let (dpt, st, rh, at) = join!(dpt_fut, st_fut, rh_fut, at_fut);

    // Check for values and use latest timestamp
    if let Ok((dpt, ts)) = dpt {
        if ts > time {
            time = ts;
        }

        if dpt.is_f64() {
            s.insert(String::from("dew_point_temp"), dpt);
        } else {
            log::error!("DewPointTemp for asset {serial_number} is invalid");
        }
    }
    if let Ok((st, ts)) = st {
        if ts > time {
            time = ts;
        }

        if st.is_f64() {
            let data = json!([{"surface_temp": st}]);
            s.insert(String::from("pavement_sensor"), data);
        } else {
            log::error!("SurfaceTemp for asset {serial_number} is invalid");
        }
    }
    if let Ok((rh, ts)) = rh {
        if ts > time {
            time = ts;
        }

        if rh.is_f64() {
            s.insert(
                String::from("relative_humidity"),
                (rh.as_f64()? as i64).into(),
            );
        } else {
            log::error!("RH for asset {serial_number} is invalid");
        }
    }
    if let Ok((at, ts)) = at {
        if ts > time {
            time = ts;
        }

        if at.is_f64() {
            let data = json!([{"air_temp": at}]);
            s.insert(String::from("temperature_sensor"), data);
        } else {
            log::error!("AirTemp for asset {serial_number} is invalid");
        }
    }

    if !s.is_empty() && time > 0 {
        let one_day = 24 * 60 * 60 * 1000;
        let now = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .ok()?
            .as_millis();
        let up_to_date = now - (time as u128) < one_day;
        return Some((Value::Object(s), time, up_to_date));
    }

    None
}

/// Inserts the samples, one list item per sensor, into the database.
/// Clears the fail times, which are updated in insert_fails.
async fn insert_samples(
    client: &Client,
    samples: Vec<(String, Value, u64)>,
) -> Result<()> {
    // for updating _weather_sensor
    let mut values = String::new();
    // for updating controller.fail_time
    let mut serial_values = String::new();
    for (serial, sample, time) in samples {
        values.push_str(&format!(
            "('{}', '{}'::jsonb, to_timestamp({} / 1000.0)),",
            serial, sample, time
        ));
        serial_values.push_str(&format!("('{}'),", serial));
    }
    values.pop(); // remove trailing comma
    serial_values.pop(); // remove trailing comma

    let update_ws: String = format!(
        "\
        UPDATE iris._weather_sensor AS ws \
        SET sample = new.sample, sample_time = new.time \
        FROM (VALUES {}) AS new(alt_id, sample, time) \
        WHERE new.alt_id = ws.alt_id;",
        values
    );
    let ws_updated = client.execute(&update_ws, &[]).await?;

    let clear_failtimes = format!(
        "\
        UPDATE iris.controller AS c \
        SET fail_time = NULL \
        FROM iris.controller_io AS cio \
        JOIN iris._weather_sensor AS ws ON ws.name = cio.name \
        JOIN (VALUES {}) AS new(alt_id) \
            ON new.alt_id = ws.alt_id \
        WHERE cio.controller = c.name;",
        serial_values
    );
    let fails_cleared = client.execute(&clear_failtimes, &[]).await?;

    log::debug!(
        "Updated sample, fail_time for {}, {} rows",
        ws_updated,
        fails_cleared
    );
    Ok(())
}

/// Inserts the fail times, one per failed sensor, into the database.
async fn insert_fails(
    client: &Client,
    fails: Vec<(String, Option<u64>)>,
) -> Result<()> {
    let mut query: String = "\
        UPDATE iris.controller as c \
        SET fail_time=new.time \
        FROM (VALUES "
        .to_owned();
    for (serial, t) in fails {
        let time = if let Some(time) = t {
            &format!("to_timestamp({time} / 1000.0)")
        } else {
            "current_timestamp"
        };
        query.push_str(&format!("('{}', {}),", serial, time));
    }
    query.pop();
    query.push_str(
        ") AS new(alt_id, time) \
        WHERE c.name = (\
            SELECT controller from iris.controller_io \
            WHERE name=(\
                SELECT name from iris._weather_sensor \
                WHERE alt_id=new.alt_id\
            )\
        )",
    );
    let rows_updated = client.execute(&query, &[]).await?;
    log::debug!("Updated fail_time for {} rows", rows_updated);
    Ok(())
}
