mod api_utility;

use resin::{Database, Error, Result};
use tokio_postgres::Client;
use serde_json::{json, Map, Value};
use std::collections::HashMap;

pub use api_utility::ApiUtility;

pub async fn run(db: Option<Database>) -> Result<()> {
    // Read IRIS server properties for API and database credentials
    let props = read_properties();

    // Host with no trailing slash
    let host = props.get("campbellcloud.host").expect("campbellcloud.host not set in properties");
    // Organization ID for use with API
    let organization_id = props.get("campbellcloud.org_id").expect("campbellcloud.org_id not set in properties");
    // Credentials for account with access to the organization/API
    let username = props.get("campbellcloud.user").expect("campbellcloud.user not set in properties");
    let api_password = props.get("campbellcloud.pass").expect("campbellcloud.pass not set in properties");
    let mut api_util = api_utility::ApiUtility::new(&host, &username, &api_password, &organization_id).await;

    // Remove protocols and just use host/database name (user/pass must be inserted)
    let _db = db.ok_or(Error::InvalidConfig("Database is None"))?;
    let client = _db.client().await?;

    // Get all the samples first
    if let Ok(serials) = get_serial_numbers(&client).await {
        let mut samples = vec![];
        let mut fails = vec![];
        for s in serials {
            if let Some(sample) = build_sample_json(&mut api_util, &s).await {
                samples.push((s, sample));
            } else {
                fails.push(s);
            }
        }

        // Then push them at same time
        if let Err(e) = insert_samples(&client, samples).await {
            panic!("{}", e);
        }
        match insert_fails(&client, fails).await {
            Ok(_) => return Ok(()),
            Err(e) => panic!("{}", e),
        }
    }
    Ok(())
}

fn read_properties() -> HashMap<String, String> {
    let lines : Vec<String> = std::fs::read_to_string("/etc/iris/iris-server.properties")
        .unwrap()
        .lines()
        .map(String::from)
        .collect();

    let mut map = HashMap::<String, String>::new();
    for line in lines {
        match line.split_once("=") {
            Some((key, value)) => {
                if !key.starts_with("#") {
                    map.insert(key.into(), value.into());
                }
            },
            None => ()
        }
    }
    map
}

async fn get_serial_numbers(client: &Client) -> Result<Vec<String>> {
    let mut serials = vec![];

    for row in client.query("SELECT alt_id FROM iris._weather_sensor", &[]).await? {
        let alt_id: Option<String> = row.get(0);

        if let Some(id) = alt_id {
            if !id.is_empty() {
                serials.push(id);
            }
        }
    }
    Ok(serials)
}

/** Build the sample JSON for one station designated by SN */
async fn build_sample_json(api: &mut ApiUtility, serial_number: &str) -> Option<Value> {
    if let Some(id_val) = api.get_id_from_serial(serial_number).await {
        let id = id_val.as_str().unwrap_or("");
        let mut s = Map::new();
        let mut changed : bool = false;
        if let Ok(dpt) = api.get_asset_last_datapoint_value(id, "DewPointTemp").await {
            if dpt.is_f64() {
                s.insert(String::from("dew_point_temp"), dpt);
                changed = true;
            } else {
                eprintln!("DewPointTemp for asset {serial_number} is invalid");
            }
        }
        if let Ok(st) = api.get_asset_last_datapoint_value(id, "SurfaceTemp").await {
            if st.is_f64() {
                let data = json!([{"surface_temp": st}]);
                s.insert(String::from("pavement_sensor"), data);
                changed = true;
            } else {
                eprintln!("SurfaceTemp for asset {serial_number} is invalid");
            }
        }
        if let Ok(rh) = api.get_asset_last_datapoint_value(id, "RH").await {
            if rh.is_f64() {
                s.insert(String::from("relative_humidity"), (rh.as_f64().unwrap() as i64).into());
                changed = true;
            } else {
                eprintln!("RH for asset {serial_number} is invalid");
            }
        }
        if let Ok(at) = api.get_asset_last_datapoint_value(id, "AirTemp").await {
            if at.is_f64() {
                let data = json!([{"air_temp": at}]);
                s.insert(String::from("temperature_sensor"), data);
                changed = true;
            } else {
                eprintln!("AirTemp for asset {serial_number} is invalid");
            }
        }

        if changed {
            return Some(Value::Object(s));
        }
    }

    None
}

async fn insert_samples(client: &Client, samples: Vec<(String, Value)>) -> Result<()> {
    if samples.len() > 0 {
        // for updating _weather_sensor
        let mut values = String::new();
        // for updating controller.fail_time
        let mut serial_values = String::new();
        for (serial, sample) in samples {
            values.push_str(&format!("('{}', '{}'::jsonb),", serial, sample));
            serial_values.push_str(&format!("('{}'),", serial));
        }
        values.pop();  // remove trailing comma
        serial_values.pop();  // remove trailing comma

        let update_ws: String = format!("
            UPDATE iris._weather_sensor AS ws
            SET sample = new.sample, sample_time = current_timestamp
            FROM (VALUES {}) AS new(alt_id, sample)
            WHERE new.alt_id = ws.alt_id;",
            values
        );
        let ws_updated = client.execute(&update_ws, &[]).await?;

        let update_failtimes = format!("
            UPDATE iris.controller AS c
            SET fail_time = NULL
            FROM iris.controller_io AS cio
            JOIN iris._weather_sensor AS ws ON ws.name = cio.name
            JOIN (VALUES {}) AS new(alt_id)
                ON new.alt_id = ws.alt_id
            WHERE cio.controller = c.name;",
            serial_values
        );
        let fails_updated = client.execute(&update_failtimes, &[]).await?;

        println!("Updated sample, fail_time for {}, {} rows", ws_updated, fails_updated);
    }
    Ok(())
}

async fn insert_fails(client: &Client, fails: Vec<String>) -> Result<()> {
    if fails.len() > 0 {
        let mut query: String = "
            UPDATE iris.controller as c
            SET fail_time=current_timestamp
            FROM (VALUES ".to_owned();
        for serial in fails {
            query.push_str(format!("('{}'),", serial).as_str());
        }
        query.pop();
        query.push_str("
            ) AS new(alt_id)
            WHERE c.name = (
                SELECT controller from iris.controller_io
                WHERE name=(
                    SELECT name from iris._weather_sensor
                    WHERE alt_id=new.alt_id
                )
            )");
        let rows_updated = client.execute(&query, &[]).await?;
        println!("Updated fail_time for {} rows", rows_updated);
    }
    Ok(())
}
