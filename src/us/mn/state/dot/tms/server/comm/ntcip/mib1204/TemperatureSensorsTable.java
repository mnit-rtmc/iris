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
import us.mn.state.dot.tms.units.Temperature;

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

	/** Number of temperature sensors in table */
	public final ASN1Integer num_temp_sensors =
		essNumTemperatureSensors.makeInt();

	/** Temperature table row */
	static public class Row {
		public final ASN1Integer temperature_sensor_height;
		public final TemperatureObject air_temperature;

		/** Create a table row */
		private Row(int row) {
			temperature_sensor_height = essTemperatureSensorHeight
				.makeInt(row);
			temperature_sensor_height.setInteger(
				HEIGHT_ERROR_MISSING);
			air_temperature = new TemperatureObject(
				essAirTemperature.makeInt(row));
		}

		/** Get JSON representation */
		private String toJson() {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			// FIXME: add height
			String at = air_temperature.toJson("air_temperature");
			if (at != null)
				sb.append(at);
			sb.append("},");
			return sb.toString();
		}
	}

	/** Rows in table */
	private final ArrayList<Row> table_rows = new ArrayList<Row>();

	/** Get number of rows in table reported by ESS */
	public int size() {
		return num_temp_sensors.getInteger();
	}

	/** Add a row to the table */
	public Row addRow(int row) {
		Row tr = new Row(row);
		table_rows.add(tr);
		return tr;
	}

	/** Get nth temperature reading or null on error */
	public Temperature getAirTemperature(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1).air_temperature.getTemperature()
		      : null;
	}

	/** Get JSON representation */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		if (table_rows.size() > 0) {
			sb.append("\"temperature_sensor\":[");
			for (Row row : table_rows)
				sb.append(row.toJson());
			sb.append("],");
		}
		String wbc = wet_bulb_temp.toJson("wet_bulb_temp");
		if (wbc != null)
			sb.append(wbc);
		String dpc = dew_point_temp.toJson("dew_point_temp");
		if (dpc != null)
			sb.append(dpc);
		String mxt = max_air_temp.toJson("max_air_temp");
		if (mxt != null)
			sb.append(mxt);
		String mnt = min_air_temp.toJson("min_air_temp");
		if (mnt != null)
			sb.append(mnt);
		// remove trailing comma
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1);
		return sb.toString();
	}
}
