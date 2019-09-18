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
	static private class TableRow {
		private final ASN1Integer subsurf_temp;

		/** Create a table row */
		private TableRow(ASN1Integer sst) {
			subsurf_temp = sst;
		}
	}

	/** Rows in table */
	private final ArrayList<TableRow> table_rows = new ArrayList<TableRow>();

	/** Get number of rows in table reported by ESS */
	public int size() {
		return num_sensors.getInteger();
	}

	/** Add a row to the table */
	public void addRow(ASN1Integer sst) {
		table_rows.add(new TableRow(sst));
	}

	/** Get nth subsurface temp or null on error */
	public ASN1Integer getTemp(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1).subsurf_temp
		      : null;
	}
}
