/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2022  Minnesota Department of Transportation
 * Copyright (C) 2017  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1204;

import java.util.ArrayList;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.utils.Json;

/**
 * Wind sensors data table, where each table row contains data read from a
 * single wind sensor within the same controller.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class WindSensorsTable {

	/** Wind sensor row */
	static public class Row {
		public final HeightObject height;
		public final WindSpeedObject avg_speed;
		public final DirectionObject avg_direction;
		public final WindSpeedObject spot_speed;
		public final DirectionObject spot_direction;
		public final WindSpeedObject gust_speed;
		public final DirectionObject gust_direction;
		public final ASN1Enum<WindSituation> situation;

		private WindSituation getSituation() {
			WindSituation sit = situation.getEnum();
			return (sit != WindSituation.undefined) ? sit : null;
		}

		/** Create a table row */
		private Row(int row) {
			height = new HeightObject("height",
				windSensorHeight.makeInt(row));
			avg_speed = new WindSpeedObject("avg_speed",
				windSensorAvgSpeed.makeInt(row));
			avg_direction = new DirectionObject("avg_direction",
				windSensorAvgDirection.makeInt(row));
			spot_speed = new WindSpeedObject("spot_speed",
				windSensorSpotSpeed.makeInt(row));
			spot_direction = new DirectionObject("spot_direction",
				windSensorSpotDirection.makeInt(row));
			gust_speed = new WindSpeedObject("gust_speed",
				windSensorGustSpeed.makeInt(row));
			gust_direction = new DirectionObject("gust_direction",
				windSensorGustDirection.makeInt(row));
			// Note: this object not supported by all vendors
			situation = new ASN1Enum<WindSituation>(
				WindSituation.class, windSensorSituation.node,
				row);
		}

		/** Get JSON representation */
		private String toJson() {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			sb.append(height.toJson());
			sb.append(avg_speed.toJson());
			sb.append(avg_direction.toJson());
			sb.append(spot_speed.toJson());
			sb.append(spot_direction.toJson());
			sb.append(gust_speed.toJson());
			sb.append(gust_direction.toJson());
			sb.append(Json.str("situation", getSituation()));
			// remove trailing comma
			if (sb.charAt(sb.length() - 1) == ',')
				sb.setLength(sb.length() - 1);
			sb.append("},");
			return sb.toString();
		}
	}

	/** Wind sensor height in meters (deprecated in V2) */
	public final HeightObject height = new HeightObject("height",
		essWindSensorHeight.makeInt());

	/** Wind situation.
	 * Note: this object not supported by all vendors */
	public final ASN1Enum<WindSituation> situation =
		new ASN1Enum<WindSituation>(WindSituation.class,
		essWindSituation.node);

	/** Two minute average wind speed (deprecated in V2) */
	public final WindSpeedObject avg_speed = new WindSpeedObject(
		"avg_speed", essAvgWindSpeed.makeInt());

	/** Two minute average wind direction (deprecated in V2) */
	public final DirectionObject avg_direction = new DirectionObject(
		"avg_direction", essAvgWindDirection.makeInt());

	/** Spot wind speed (deprecated in V2) */
	public final WindSpeedObject spot_speed = new WindSpeedObject(
		"spot_speed", essSpotWindSpeed.makeInt());

	/** Spot wind direction (deprecated in V2) */
	public final DirectionObject spot_direction = new DirectionObject(
		"spot_direction", essSpotWindDirection.makeInt());

	/** Ten minute max gust wind speed (deprecated in V2) */
	public final WindSpeedObject gust_speed = new WindSpeedObject(
		"gust_speed", essMaxWindGustSpeed.makeInt());

	/** Ten minute max gust wind direction (deprecated in V2) */
	public final DirectionObject gust_direction = new DirectionObject(
		"gust_direction", essMaxWindGustDir.makeInt());

	/** Number of sensors in table (V2+) */
	public final ASN1Integer num_sensors =
		windSensorTableNumSensors.makeInt();

	/** Get number of sensors in table */
	private int size() {
		return num_sensors.getInteger();
	}

	/** Rows in table */
	private final ArrayList<Row> table_rows = new ArrayList<Row>();

	/** Check if all rows have been read */
	public boolean isDone() {
		return table_rows.size() >= size();
	}

	/** Add a row to the table */
	public Row addRow() {
		Row tr = new Row(table_rows.size() + 1);
		table_rows.add(tr);
		return tr;
	}

	/** Get one table row */
	public Row getRow(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1)
		      : null;
	}

	/** Get the wind situation */
	private WindSituation getSituation() {
		WindSituation sit = situation.getEnum();
		return (sit != WindSituation.undefined) ? sit : null;
	}

	/** Get two minute average wind speed */
	public WindSpeedObject getAvgSpeed() {
		return table_rows.isEmpty()
		      ? avg_speed
		      : table_rows.get(0).avg_speed;
	}

	/** Get two minute average wind direction */
	public DirectionObject getAvgDir() {
		return table_rows.isEmpty()
		      ? avg_direction
		      : table_rows.get(0).avg_direction;
	}

	/** Get spot wind speed */
	public WindSpeedObject getSpotSpeed() {
		return table_rows.isEmpty()
		      ? spot_speed
		      : table_rows.get(0).spot_speed;
	}

	/** Get spot wind direction */
	public DirectionObject getSpotDir() {
		return table_rows.isEmpty()
		      ? spot_direction
		      : table_rows.get(0).spot_direction;
	}

	/** Get ten minute max gust wind speed */
	public WindSpeedObject getGustSpeed() {
		return table_rows.isEmpty()
		      ? gust_speed
		      : table_rows.get(0).gust_speed;
	}

	/** Get ten minute max gust wind direction */
	public DirectionObject getGustDir() {
		return table_rows.isEmpty()
		      ? gust_direction
		      : table_rows.get(0).gust_direction;
	}

	/** Get JSON representation */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("\"wind_sensor\":[");
		if (table_rows.size() > 0) {
			for (Row row : table_rows)
				sb.append(row.toJson());
		} else {
			sb.append('{');
			sb.append(height.toJson());
			sb.append(avg_speed.toJson());
			sb.append(avg_direction.toJson());
			sb.append(spot_speed.toJson());
			sb.append(spot_direction.toJson());
			sb.append(gust_speed.toJson());
			sb.append(gust_direction.toJson());
			sb.append(Json.str("situation", getSituation()));
			// remove trailing comma
			if (sb.charAt(sb.length() - 1) == ',')
				sb.setLength(sb.length() - 1);
			sb.append("},");
		}
		// remove trailing comma
		if (sb.charAt(sb.length() - 1) == ',')
			sb.setLength(sb.length() - 1);
		sb.append("],");
		return sb.toString();
	}
}
