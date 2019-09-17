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

/**
 * Temperature sensors data table, where each table row contains data read from
 * a single temperature sensor within the same controller.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class TemperatureSensorsTable {

	/** Number of temperature sensors in table */
	public final ASN1Integer num_temp_sensors =
		essNumTemperatureSensors.makeInt();

	/** Table row */
	static private class TableRow {
		// height in meters relative to essReferenceHeight
		// 1001 indicates missing value
		private final ASN1Integer height;
		private final ASN1Integer air_temp;

		/** Create a table row */
		private TableRow(ASN1Integer hm, ASN1Integer at) {
			height = hm;
			air_temp = at;
		}
	}

	/** Rows in table */
	private final ArrayList<TableRow> table_rows = new ArrayList<TableRow>();

	/** Get number of rows in table reported by ESS */
	public int size() {
		return num_temp_sensors.getInteger();
	}

	/** Add a row to the table.
	 * @param tsh Temperature sensor height in meters
	 * @param tsa Air temperature in tenths of a degree C */
	public void addRow(ASN1Integer tsh, ASN1Integer tsa) {
		table_rows.add(new TableRow(tsh, tsa));
	}

	/** Get nth temperature reading or null on error */
	public ASN1Integer getTemp(int row) {
		if (row >= 1 && row <= table_rows.size())
			return table_rows.get(row - 1).air_temp;
		else
			return null;
	}
}
