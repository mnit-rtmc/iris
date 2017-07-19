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
import us.mn.state.dot.tms.PavementSensorError;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;

/**
 * Temperature sensors data table, where each table row 
 * contains data read from a single pavement sensor 
 * within the same controller.
 *
 * @author Michael Darter
 */
public class PavementSensorsTable {

	/** Number of temperature sensors in table */
	public final ASN1Integer num_sensors = 
		numEssPavementSensors.makeInt();

	/** Table row */
	static private class TableRow {
		private final int row_num;
		private final ASN1Integer surf_status;
		private final ASN1Integer surf_temp;
		private final ASN1Integer pvmt_temp;
		private final ASN1Integer surf_freeze_temp;
		private final ASN1Integer pvmt_sens_err;

		/** Row constructor */
		private TableRow(int rn, ASN1Integer ess, ASN1Integer est, 
			ASN1Integer ept, ASN1Integer sft, ASN1Integer pse)
		{
			row_num = rn;
			surf_status = ess;
			surf_temp = est;
			pvmt_temp = ept;
			surf_freeze_temp = sft;
			pvmt_sens_err = pse;
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
	public void addRow(int row, ASN1Integer ess, ASN1Integer est, 
		ASN1Integer ept, ASN1Integer sfp, ASN1Integer pse)
	{
		table_rows.put(row, new TableRow(row, ess, est, ept, 
			sfp, pse));
	}

	/** Get nth surface temp or null on error */
	public ASN1Integer getSurfTemp(int row) {
		TableRow tr = table_rows.get(row);
		return (tr != null ? tr.surf_temp : null);
	}

	/** Get nth pavement surf temp or null on error */
	public ASN1Integer getPvmtTemp(int row) {
		TableRow tr = table_rows.get(row);
		return (tr != null ? tr.pvmt_temp : null);
	}

	/** Get nth pvmt surf status or null on error */
	public Integer getPvmtSurfStatus(int row) {
		TableRow tr = table_rows.get(row);
		return (tr != null ? tr.surf_status.getInteger() : null);
	}

	/** Get nth surf freeze temp or null on error */
	public ASN1Integer getSurfFreezeTemp(int row) {
		TableRow tr = table_rows.get(row);
		return (tr != null ? tr.surf_freeze_temp : null);
	}

	/** Get nth surf sensor error or null on error */
	public PavementSensorError getPvmtSensErr(int row) {
		TableRow tr = table_rows.get(row);
		if (tr != null) {
			return PavementSensorError.fromOrdinal(
				tr.pvmt_sens_err.getInteger());
		}
		return PavementSensorError.UNDEFINED;
	}
}
