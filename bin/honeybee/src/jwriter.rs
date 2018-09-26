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
use postgres::{Connection, TlsMode};
use std::fs::File;
use std::io::{BufWriter,Write};

pub fn start(uds: String) -> Result<(), Error> {
    let conn = Connection::connect(uds, TlsMode::None)?;
    query_json_file(&conn, DMS_SQL, "dms.json")?;
    query_json_file(&conn, INCIDENT_SQL, "incident.json")?;
    Ok(())
}

static DMS_SQL: &str =
        "SELECT row_to_json(r)::text FROM (\
            SELECT name, sign_config, roadway, road_dir, cross_street, \
                   location, lat, lon \
            FROM dms_view ORDER BY name \
        ) r";

static INCIDENT_SQL: &str =
        "SELECT row_to_json(r)::text FROM (\
            SELECT name, event_date, description, road, direction, lane_type, \
                   impact, confirmed, camera, detail, replaces, lat, lon \
            FROM incident_view \
            WHERE cleared = false \
        ) r";

fn query_json_file(conn: &Connection, sql: &str, file_name: &str)
    -> Result<(), Error>
{
    let f = BufWriter::new(File::create(file_name)?);
    let r = query_json(&conn, sql, f)?;
    println!("{} rows: {}", file_name, r);
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
    w.write("\n]".as_bytes())?;
    Ok(c)
}
