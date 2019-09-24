/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Iteris Inc.
 * Copyright (C) 2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.METERS;

/**
 * Temperature sensors data table, where each table row contains data read from
 * a single temperature sensor within the same controller.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class TemperatureSensorsTable {

	/** A height of 1001 is an error condition or missing value */
	static private final int HEIGHT_ERROR_MISSING = 1001;

	/** Convert height to Distance.
	 * @param h Height in meters with 1001 indicating an error or missing
	 *          value.
	 * @return Height distance or null for missing */
	static private Distance convertHeight(ASN1Integer h) {
		if (h != null) {
			int ih = h.getInteger();
			if (ih < HEIGHT_ERROR_MISSING)
				return new Distance(ih, METERS);
		}
		return null;
	}

	/** Number of sensors in table */
	public final ASN1Integer num_temp_sensors =
		essNumTemperatureSensors.makeInt();

	/** Wet-bulb temperature */
	public final TemperatureObject wet_bulb_temp = new TemperatureObject(
		essWetbulbTemp.makeInt());

	/** Dew point temperature */
	public final TemperatureObject dew_point_temp = new TemperatureObject(
		essDewpointTemp.makeInt());

	/** Maximum air temperature */
	public final TemperatureObject max_air_temp = new TemperatureObject(
		essMaxTemp.makeInt());

	/** Minimum air temperature */
	public final TemperatureObject min_air_temp = new TemperatureObject(
		essMinTemp.makeInt());

	/** Temperature table row */
	static public class Row {
		public final ASN1Integer height;
		public final TemperatureObject air_temp;

		/** Create a table row */
		private Row(int row) {
			height = essTemperatureSensorHeight.makeInt(row);
			height.setInteger(HEIGHT_ERROR_MISSING);
			air_temp = new TemperatureObject(
				essAirTemperature.makeInt(row));
		}

		/** Get sensor height in meters */
		public Integer getHeight() {
			Distance h = convertHeight(height);
			return (h != null) ? h.round(METERS) : null;
		}

		/** Get air temperature or null on error */
		public Integer getAirTempC() {
			return air_temp.getTempC();
		}

		/** Get JSON representation */
		private String toJson() {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			sb.append(Json.num("height", getHeight()));
			sb.append(air_temp.toJson("air_temp"));
			// remove trailing comma
			if (sb.charAt(sb.length() - 1) == ',')
				sb.setLength(sb.length() - 1);
			sb.append("},");
			return sb.toString();
		}
	}

	/** Rows in table */
	private final ArrayList<Row> table_rows = new ArrayList<Row>();

	/** Get number of rows in table reported by ESS */
	private int size() {
		return num_temp_sensors.getInteger();
	}

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

	/** Get the dew point temp */
	public Integer getDewPointTempC() {
		return dew_point_temp.getTempC();
	}

	/** Get the max temp */
	public Integer getMaxTempC() {
		return max_air_temp.getTempC();
	}

	/** Get the min temp */
	public Integer getMinTempC() {
		return min_air_temp.getTempC();
	}

	/** Get JSON representation */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		if (table_rows.size() > 0) {
			sb.append("\"temperature_sensor\":[");
			for (Row row : table_rows)
				sb.append(row.toJson());
			// remove trailing comma
			if (sb.charAt(sb.length() - 1) == ',')
				sb.setLength(sb.length() - 1);
			sb.append("],");
		}
		sb.append(wet_bulb_temp.toJson("wet_bulb_temp"));
		sb.append(dew_point_temp.toJson("dew_point_temp"));
		sb.append(max_air_temp.toJson("max_air_temp"));
		sb.append(min_air_temp.toJson("min_air_temp"));
		return sb.toString();
	}
}
