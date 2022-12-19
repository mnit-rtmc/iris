/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.DeviceImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.snmp.DisplayStringObject;
import static us.mn.state.dot.tms.server.comm.snmp.MIB.*;

/**
 * Operation to query system values.
 *
 * @author Douglas Lau
 */
public class OpQuerySystem extends OpNtcip {

	/** System description */
	private final DisplayStringObject sys_descr = new DisplayStringObject(
		"sys_descr", sysDescr.node);

	/** System contact */
	private final DisplayStringObject sys_contact = new DisplayStringObject(
		"sys_contact", sysContact.node);

	/** System name */
	private final DisplayStringObject sys_name = new DisplayStringObject(
		"sys_name", sysName.node);

	/** System location */
	private final DisplayStringObject sys_location = new DisplayStringObject(
		"sys_location", sysLocation.node);

	/** Create a new query system object */
	public OpQuerySystem(DeviceImpl d) {
		super(PriorityLevel.CONFIGURE, d);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QuerySettings();
	}

	/** Phase to query settings */
	protected class QuerySettings extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(sys_descr.node);
			mess.add(sys_contact.node);
			mess.add(sys_name.node);
			mess.add(sys_location.node);
			mess.queryProps();
			logQuery(sys_descr.node);
			logQuery(sys_contact.node);
			logQuery(sys_name.node);
			logQuery(sys_location.node);
			return null;
		}
	}
}
