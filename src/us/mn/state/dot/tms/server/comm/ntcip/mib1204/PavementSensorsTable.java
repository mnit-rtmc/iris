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
import us.mn.state.dot.tms.units.Temperature;

/**
 * Temperature sensors data table, where each table row contains data read from
 * a single pavement sensor within the same controller.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class PavementSensorsTable {

	/** Number of temperature sensors in table */
	public final ASN1Integer num_sensors = numEssPavementSensors.makeInt();

	/** Table row */
	static public class Row {
		public final ASN1Integer pavement_type;
		public final ASN1Integer sensor_type;
		public final ASN1Enum<EssSurfaceStatus> surface_status;
		public final TemperatureObject surface_temp;
		public final TemperatureObject pavement_temp;
		public final TemperatureObject surface_freeze_point;
		public final ASN1Enum<PavementSensorError> pavement_sensor_error;
		public final ASN1Integer surface_water_depth;

		/** Create a table row */
		private Row(int row) {
			pavement_type = essPavementType.makeInt(row);
			sensor_type = essPavementSensorType.makeInt(row);
			surface_status = new ASN1Enum<EssSurfaceStatus>(
				EssSurfaceStatus.class, essSurfaceStatus.node,
				row);
			surface_temp = new TemperatureObject(
				essSurfaceTemperature.makeInt(row));
			pavement_temp = new TemperatureObject(
				essPavementTemperature.makeInt(row));
			surface_freeze_point = new TemperatureObject(
				essSurfaceFreezePoint.makeInt(row));
			pavement_sensor_error = new ASN1Enum<PavementSensorError>(
				PavementSensorError.class,
				essPavementSensorError.node, row);
			surface_water_depth = essSurfaceWaterDepth.makeInt(row);
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

	/** Get nth surface temp or null on error */
	public Temperature getSurfTemp(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1).surface_temp.getTemperature()
		      : null;
	}

	/** Get nth pavement surf temp or null on error */
	public Temperature getPvmtTemp(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1).pavement_temp.getTemperature()
		      : null;
	}

	/** Get nth pvmt surf status or null on error */
	public EssSurfaceStatus getPvmtSurfStatus(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1).surface_status.getEnum()
		      : null;
	}

	/** Get nth surf freeze temp or null on error */
	public Temperature getSurfFreezeTemp(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1).surface_freeze_point.getTemperature()
		      : null;
	}

	/** Get nth surf sensor error or null on error */
	public PavementSensorError getPvmtSensErr(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1).pavement_sensor_error.getEnum()
		      : null;
	}
}
