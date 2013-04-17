/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.DeviceImpl;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation for NTCIP device.
 *
 * @author Douglas Lau
 */
abstract public class OpNtcip extends OpDevice {

	/** NTCIP debug log */
	static private final DebugLog NTCIP_LOG = new DebugLog("ntcip");

	/** Log an error msg */
	protected void logError(String msg) {
		if(NTCIP_LOG.isOpen())
			NTCIP_LOG.log(device.getName() + "! " + msg);
	}

	/** Log a property query */
	protected void logQuery(ASN1Object prop) {
		if(NTCIP_LOG.isOpen())
			NTCIP_LOG.log(device.getName() + ": " + prop);
	}

	/** Log a property store */
	protected void logStore(ASN1Object prop) {
		if(NTCIP_LOG.isOpen())
			NTCIP_LOG.log(device.getName() + ":= " + prop);
	}

	/** Create a new NTCIP operation */
	protected OpNtcip(PriorityLevel p, DeviceImpl d) {
		super(p, d);
	}
}
