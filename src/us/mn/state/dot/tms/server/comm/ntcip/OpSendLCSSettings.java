/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to send settings to a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class OpSendLCSSettings extends OpLCS {

	/** Create a new operation to send LCS settings */
	public OpSendLCSSettings(LCSArrayImpl l) {
		super(PriorityLevel.DEVICE_DATA, l);
		sendSettings();
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		return null;
	}

	/** Send settings to all DMS in an LCS array */
	protected void sendSettings() {
		for(DMSImpl dms: dmss) {
			if(dms != null) {
				dms.setDeviceRequest(
					DeviceRequest.SEND_SETTINGS.ordinal());
			}
		}
	}
}
