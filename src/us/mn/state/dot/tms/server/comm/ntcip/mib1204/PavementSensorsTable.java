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
	static private class TableRow {
		private final ASN1Enum<EssSurfaceStatus> surf_status;
		private final ASN1Integer surf_temp;
		private final ASN1Integer pvmt_temp;
		private final ASN1Integer surf_freeze_temp;
		private final ASN1Enum<PavementSensorError> pvmt_sens_err;

		/** Create a table row */
		private TableRow(ASN1Enum<EssSurfaceStatus> ess,
			ASN1Integer est, ASN1Integer ept, ASN1Integer sft,
			ASN1Enum<PavementSensorError> pse)
		{
			surf_status = ess;
			surf_temp = est;
			pvmt_temp = ept;
			surf_freeze_temp = sft;
			pvmt_sens_err = pse;
		}
	}

	/** Rows in table */
	private final ArrayList<TableRow> table_rows = new ArrayList<TableRow>();

	/** Get number of rows in table reported by ESS */
	public int size() {
		return num_sensors.getInteger();
	}

	/** Add a row to the table */
	public void addRow(ASN1Enum<EssSurfaceStatus> ess, ASN1Integer est,
		ASN1Integer ept, ASN1Integer sfp,
		ASN1Enum<PavementSensorError> pse)
	{
		table_rows.add(new TableRow(ess, est, ept, sfp, pse));
	}

	/** Get nth surface temp or null on error */
	public ASN1Integer getSurfTemp(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1).surf_temp
		      : null;
	}

	/** Get nth pavement surf temp or null on error */
	public ASN1Integer getPvmtTemp(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1).pvmt_temp
		      : null;
	}

	/** Get nth pvmt surf status or null on error */
	public EssSurfaceStatus getPvmtSurfStatus(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1).surf_status.getEnum()
		      : null;
	}

	/** Get nth surf freeze temp or null on error */
	public ASN1Integer getSurfFreezeTemp(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1).surf_freeze_temp
		      : null;
	}

	/** Get nth surf sensor error or null on error */
	public PavementSensorError getPvmtSensErr(int row) {
		return (row >= 1 && row <= table_rows.size())
		      ? table_rows.get(row - 1).pvmt_sens_err.getEnum()
		      : null;
	}
}
