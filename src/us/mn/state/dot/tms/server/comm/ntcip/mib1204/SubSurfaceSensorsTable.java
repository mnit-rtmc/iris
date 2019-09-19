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
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;
import us.mn.state.dot.tms.units.Temperature;

/**
 * SubSurface sensors data table, where each table row contains data read from a
 * single sensor within the same controller.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class SubSurfaceSensorsTable {

	/** Number of temperature sensors in table */
	public final ASN1Integer num_sensors =
		numEssSubSurfaceSensors.makeInt();

	/** Table row */
	static public class Row {
		public final ASN1String sub_surface_sensor_location;
		public final ASN1Integer sub_surface_type;
		public final ASN1Integer sub_surface_sensor_depth;
		public final TemperatureObject sub_surface_temp;
		public final ASN1Integer sub_surface_moisture;
		public final ASN1Enum<SubSurfaceSensorError> sub_surface_sensor_error;

		/** Create a table row */
		private Row(int row) {
			sub_surface_sensor_location = new ASN1String(
				essSubSurfaceSensorLocation.node, row);
			sub_surface_type = essSubSurfaceType.makeInt(row);
			sub_surface_sensor_depth = essSubSurfaceSensorDepth
				.makeInt(row);
			sub_surface_temp = new TemperatureObject(
				essSubSurfaceTemperature.makeInt(row));
			sub_surface_moisture = essSubSurfaceMoisture.makeInt(
				row);
			sub_surface_sensor_error = new ASN1Enum<
				SubSurfaceSensorError>(SubSurfaceSensorError.class,
				essSubSurfaceSensorError.node, row);
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

	/** Get nth sub-surface temp or null on error */
	public Temperature getTemp(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1).sub_surface_temp.getTemperature()
		      : null;
	}
}
