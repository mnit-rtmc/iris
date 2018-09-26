/*
 * Copyright (C) 2018  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
use failure::Error;
use fallible_iterator::FallibleIterator;
use postgres::{Connection, TlsMode};
use std::fs::File;
use std::io::{BufWriter,Write};

pub fn start(uds: String) -> Result<(), Error> {
    let conn = Connection::connect(uds, TlsMode::None)?;
    query_json_file(&conn, CAMERA_SQL, "camera.json")?;
    query_json_file(&conn, DMS_SQL, "dms.json")?;
    query_json_file(&conn, DMS_MSG_SQL, "dms_message.json")?;
    query_json_file(&conn, INCIDENT_SQL, "incident.json")?;
    query_json_file(&conn, SIGN_CONFIG_SQL, "sign_config.json")?;
    query_json_file(&conn, PARKING_AREA_STAT_SQL, "parking_area_static.json")?;
    notify_loop(&conn)
}

static CAMERA_SQL: &str =
    "SELECT row_to_json(r)::text FROM (\
        SELECT name, publish, location, lat, lon \
        FROM camera_view \
        ORDER BY name \
    ) r";

static DMS_SQL: &str =
    "SELECT row_to_json(r)::text FROM (\
        SELECT name, sign_config, roadway, road_dir, cross_street, \
               location, lat, lon \
        FROM dms_view \
        ORDER BY name \
    ) r";

static DMS_MSG_SQL: &str =
    "SELECT row_to_json(r)::text FROM (\
        SELECT name, msg_current, multi, sources, duration, expire_time \
        FROM dms_message_view WHERE condition = 'Active' \
        ORDER BY name \
    ) r";

static INCIDENT_SQL: &str =
    "SELECT row_to_json(r)::text FROM (\
        SELECT name, event_date, description, road, direction, lane_type, \
               impact, confirmed, camera, detail, replaces, lat, lon \
        FROM incident_view \
        WHERE cleared = false \
    ) r";

static SIGN_CONFIG_SQL: &str =
    "SELECT row_to_json(r)::text FROM (\
        SELECT name, dms_type, portable, technology, sign_access, legend, \
               beacon_type, face_width, face_height, border_horiz, \
               border_vert, pitch_horiz, pitch_vert, pixel_width, \
               pixel_height, char_width, char_height, color_scheme, \
               monochrome_foreground, monochrome_background \
        FROM sign_config_view \
    ) r";

static PARKING_AREA_STAT_SQL: &str =
    "SELECT row_to_json(r)::text FROM (\
        SELECT site_id AS \"siteId\", time_stamp_static AS \"timeStamp\", \
               relevant_highway AS \"relevantHighway\", \
               reference_post AS \"referencePost\", exit_id AS \"exitId\", \
               road_dir AS \"directionOfTravel\", facility_name AS name, \
               json_build_object('latitude', lat, 'longitude', lon, \
               'streetAdr', street_adr, 'city', city, 'state', state, \
               'zip', zip, 'timeZone', time_zone) AS location, \
               ownership, capacity, \
               string_to_array(amenities, ', ') AS amenities, \
               array_remove(ARRAY[camera_image_base_url || camera_1, \
               camera_image_base_url || camera_2, \
               camera_image_base_url || camera_3], NULL) AS images, \
               ARRAY[]::text[] AS logos \
        FROM parking_area_view \
    ) r";

fn query_json_file(conn: &Connection, sql: &str, file_name: &str)
    -> Result<(), Error>
{
    let f = BufWriter::new(File::create(file_name)?);
    let r = query_json(&conn, sql, f)?;
    println!("wrote {}, rows: {}", file_name, r);
    Ok(())
}

fn query_json<T: Write>(conn: &Connection, q: &str, mut w: T)
    -> Result<u32, Error>
{
    let mut first = true;
    let mut c = 0;
    w.write("[".as_bytes())?;
    for row in &conn.query(q, &[])? {
        if !first {
            w.write(",".as_bytes())?;
        } else {
            first = false;
        }
        w.write("\n".as_bytes())?;
        let j: String = row.get(0);
        w.write(j.as_bytes())?;
        c += 1;
    }
    w.write("\n]\n".as_bytes())?;
    Ok(c)
}

fn notify_loop(conn: &Connection) -> Result<(), Error> {
    &conn.execute("LISTEN tms", &[])?;
    let nots = conn.notifications();
    loop {
        for n in nots.blocking_iter().iterator() {
            let n = n?;
            let res = match n.payload.as_ref() {
                "camera"      => Some((CAMERA_SQL, "camera.json")),
                "dms"         => Some((DMS_SQL, "dms.json")),
                "dms_message" => Some((DMS_MSG_SQL, "dms_message.json")),
                "incident"    => Some((INCIDENT_SQL, "incident.json")),
                "sign_config" => Some((SIGN_CONFIG_SQL, "sign_config.json")),
                "parking_area_static" => Some((PARKING_AREA_STAT_SQL,
                                              "parking_area_static.json")),
                _             => None,
            };
            if let Some((sql, file_name)) = res {
                query_json_file(&conn, sql, file_name)?;
            }
        }
    }
}
