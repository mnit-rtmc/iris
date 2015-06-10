/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2015  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.LaneUseMultiHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DeviceImpl;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Object;

/**
 * Operation for NTCIP device.
 *
 * @author Douglas Lau
 */
abstract public class OpNtcip extends OpDevice {

	/** NTCIP debug log */
	static private final DebugLog NTCIP_LOG = new DebugLog("ntcip");

	/** Lookup a sign message number */
	static protected int lookupMsgNum(SignMessage sm) {
		LaneUseMulti lum = LaneUseMultiHelper.find(sm.getMulti());
		if (lum != null) {
			Integer msg_num = lum.getMsgNum();
			if (msg_num != null)
				return msg_num;
		}
		return 1;
	}

	/** Lookup an LCS indication on a sign message */
	static protected Integer lookupIndication(SignMessage sm) {
		String m = sm.getMulti();
		MultiString ms = new MultiString(m);
		if (ms.isBlank())
			return LaneUseIndication.DARK.ordinal();
		LaneUseMulti lum = LaneUseMultiHelper.find(m);
		if (lum != null)
			return lum.getIndication();
		else
			return null;
	}

	/** Log an error msg */
	protected void logError(String msg) {
		if (NTCIP_LOG.isOpen())
			NTCIP_LOG.log(device.getName() + "! " + msg);
	}

	/** Log a property query */
	protected void logQuery(ASN1Object prop) {
		if (NTCIP_LOG.isOpen())
			NTCIP_LOG.log(device.getName() + ": " + prop);
	}

	/** Log a property store */
	protected void logStore(ASN1Object prop) {
		if (NTCIP_LOG.isOpen())
			NTCIP_LOG.log(device.getName() + ":= " + prop);
	}

	/** Create a new NTCIP operation */
	protected OpNtcip(PriorityLevel p, DeviceImpl d) {
		super(p, d);
	}
}
