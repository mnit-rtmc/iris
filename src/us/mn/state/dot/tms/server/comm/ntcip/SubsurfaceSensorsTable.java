/*
 * IRIS -- Intelligent Roadway Information System
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.util.TreeMap;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;

/**
 * Subsurface sensors data table, where each table row 
 * contains data read from a single sensor within the 
 * same controller.
 *
 * @author Michael Darter
 */
public class SubsurfaceSensorsTable {

	/** Number of temperature sensors in table */
	public final ASN1Integer num_sensors = 
		numEssSubSurfaceSensors.makeInt();

	/** Table row */
	static private class TableRow {
		private final int row_num;
		private final ASN1Integer subsurf_temp;

		/** Row constructor */
		private TableRow(int rn, ASN1Integer sst) {
			row_num = rn;
			subsurf_temp = sst;
		}
	}

	/** Table of rows, which maps row number to row */
	private final TreeMap<Integer, TableRow> table_rows =
		new TreeMap<Integer, TableRow>();

	/** Get number of rows in table reported by ESS */
	public int size() {
		return num_sensors.getInteger();
	}

	/** Add a row to the table.
	 * @param row Row number (1 based) */
	public void addRow(int row, ASN1Integer sst) {
		table_rows.put(row, new TableRow(row, sst));
	}

	/** Get nth subsurface temp or null on error */
	public ASN1Integer getTemp(int row) {
		TableRow tr = table_rows.get(row);
		return (tr != null ? tr.subsurf_temp : null);
	}
}
