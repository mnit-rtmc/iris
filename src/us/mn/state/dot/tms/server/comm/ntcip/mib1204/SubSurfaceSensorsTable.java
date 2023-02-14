/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Iteris Inc.
 * Copyright (C) 2019-2023  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.snmp.DisplayString;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.Json;

import static us.mn.state.dot.tms.units.Distance.Units.CENTIMETERS;
import static us.mn.state.dot.tms.units.Distance.Units.METERS;

/**
 * SubSurface sensors data table, where each table row contains data read from a
 * single sensor within the same controller.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class SubSurfaceSensorsTable {

	/** Depth of 1001 indicates error or missing value */
	static private final int DEPTH_ERROR_MISSING = 1001;

	/** Convert depth to Distance.
	 * @param d Depth in centimeters with 1001 indicating an error or
	 *          missing value.
	 * @return Depth distance or null for missing */
	static private Distance convertDepth(ASN1Integer d) {
		if (d != null) {
			int id = d.getInteger();
			if (id != DEPTH_ERROR_MISSING)
				return new Distance(id, CENTIMETERS);
		}
		return null;
	}

	/** Number of temperature sensors in table */
	public final ASN1Integer num_sensors =
		numEssSubSurfaceSensors.makeInt();

	/** Table row */
	static public class Row {
		public final DisplayString location;
		public final ASN1Enum<SubSurfaceType> sub_surface_type;
		public final ASN1Integer depth;
		public final TemperatureObject temp;
		public final PercentObject moisture;
		public final ASN1Enum<SubSurfaceSensorError> sensor_error;

		/** Create a table row */
		private Row(int row) {
			location = new DisplayString(
				essSubSurfaceSensorLocation.node, row);
			sub_surface_type = new ASN1Enum<SubSurfaceType>(
				SubSurfaceType.class, essSubSurfaceType.node,
				row);
			depth = essSubSurfaceSensorDepth.makeInt(row);
			depth.setInteger(DEPTH_ERROR_MISSING);
			temp = new TemperatureObject("temp",
				essSubSurfaceTemperature.makeInt(row));
			moisture = new PercentObject("moisture",
				essSubSurfaceMoisture.makeInt(row));
			sensor_error = new ASN1Enum<SubSurfaceSensorError>(
				SubSurfaceSensorError.class,
				essSubSurfaceSensorError.node, row);
		}

		/** Get the sensor location */
		public String getSensorLocation() {
			String sl = location.getValue();
			return (sl.length() > 0) ? sl : null;
		}

		/** Get sub-surface type or null on error */
		public SubSurfaceType getSubSurfaceType() {
			SubSurfaceType sst = sub_surface_type.getEnum();
			return (sst != SubSurfaceType.undefined) ? sst : null;
		}

		/** Get sub-surface sensor depth in meters */
		private String getDepth() {
			Distance d = convertDepth(depth);
			if (d != null) {
				Float dm = d.asFloat(METERS);
				return Num.format(dm, 2); // cm
			} else
				return null;
		}

		/** Get sub-surface temp or null on error */
		public Integer getTempC() {
			return temp.getTempC();
		}

		/** Get sensor error or null on error */
		public SubSurfaceSensorError getSensorError() {
			SubSurfaceSensorError se = sensor_error.getEnum();
			return (se != null && se.isError()) ? se : null;
		}

		/** Get JSON representation */
		private String toJson() {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			sb.append(Json.str("location", getSensorLocation()));
			sb.append(Json.str("sub_surface_type",
				getSubSurfaceType()));
			sb.append(Json.num("depth", getDepth()));
			sb.append(temp.toJson());
			sb.append(moisture.toJson());
			sb.append(Json.str("sensor_error", getSensorError()));
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
		return num_sensors.getInteger();
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

	/** Get JSON representation */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		if (table_rows.size() > 0) {
			sb.append("\"sub_surface_sensor\":[");
			for (Row row : table_rows)
				sb.append(row.toJson());
			// remove trailing comma
			if (sb.charAt(sb.length() - 1) == ',')
				sb.setLength(sb.length() - 1);
			sb.append("],");
		}
		return sb.toString();
	}
}
