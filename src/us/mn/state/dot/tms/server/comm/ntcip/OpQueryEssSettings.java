/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017 Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204.*;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.PavementSensorsTable;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;

/**
 * Operation to query a weather sensor's settings.
 *
 * @author Michael Darter
 */
public class OpQueryEssSettings extends OpEss {

	/** Pavement sensors table */
	private final PavementSensorsTable ps_table = new PavementSensorsTable();

	/** Create a new query status object */
	public OpQueryEssSettings(WeatherSensorImpl d) {
		super(PriorityLevel.DEVICE_DATA, d);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QuerySettings();
	}

	/** Phase to query settings */
	protected class QuerySettings extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1String sdes = new ASN1String(sysDescr.node);
			ASN1String scon = new ASN1String(sysContact.node);
			ASN1String snam = new ASN1String(sysName.node);
			ASN1String sloc = new ASN1String(sysLocation.node);
			mess.add(sdes);
			mess.add(scon);
			mess.add(snam);
			mess.add(sloc);
			mess.queryProps();
			controller.setVersionNotify(sdes.toString());
			logQuery(sdes);
			logQuery(scon);
			logQuery(snam);
			logQuery(sloc);
			return new QueryElevation();
		}
	}

	/** Phase to query elevation values */
	protected class QueryElevation extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer reh = essReferenceHeight.makeInt();
			ASN1Integer prh = essPressureHeight.makeInt();
			ASN1Integer wsh = essWindSensorHeight.makeInt();
			// essPavementElevation
			mess.add(reh);
			mess.add(prh);
			mess.add(wsh);
			mess.queryProps();
			logQuery(reh);
			logQuery(prh);
			logQuery(wsh);
			return new QueryPavement();
		}
	}

	/** Phase to query pavement values */
	protected class QueryPavement extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ps_table.num_sensors);
			mess.queryProps();
			logQuery(ps_table.num_sensors);
			return new QueryPavementTable();
		}
	}

	/** Phase to query all rows in pavement table */
	protected class QueryPavementTable extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			if (ps_table.isDone())
				return null;
			PavementSensorsTable.Row pr = ps_table.addRow();
			// FIXME: add pavement exposure & sensor location
			//        elevation
			mess.add(pr.pavement_type);
			mess.add(pr.sensor_type);
			mess.queryProps();
			logQuery(pr.pavement_type);
			logQuery(pr.sensor_type);
			return this;
		}
	}

	// FIXME: store as config json value
}
