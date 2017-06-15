/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017 Iteris Inc.
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
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;

/**
 * Operation to query a weather sensor's settings.
 *
 * @author Michael Darter
 */
public class OpQueryEssSettings extends OpEss {

	/** Table of temperature sensor data */
	private final TemperatureSensorsTable ts_table = 
		new TemperatureSensorsTable();

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
			ASN1String sdes = new ASN1String(sysDescr.node, 0);
			ASN1String scon = new ASN1String(sysContact.node, 0);
			ASN1String snam = new ASN1String(sysName.node, 0);
			ASN1String sloc = new ASN1String(sysLocation.node, 0);
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
			return null;
		}
	}
}
