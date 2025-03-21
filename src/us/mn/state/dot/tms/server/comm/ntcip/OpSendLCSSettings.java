/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.LcsImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to send settings to a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class OpSendLCSSettings extends OpLCS {

	/** Create a new operation to send LCS settings */
	public OpSendLCSSettings(LcsImpl l) {
		super(PriorityLevel.SETTINGS, l);
		sendSettings();
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return null;
	}

	/** Send settings to all DMS in an LCS array */
	private void sendSettings() {
		for (DMSImpl dms: dmss) {
			if (dms != null) {
				dms.setDeviceRequest(
					DeviceRequest.SEND_SETTINGS.ordinal());
			}
		}
	}
}
