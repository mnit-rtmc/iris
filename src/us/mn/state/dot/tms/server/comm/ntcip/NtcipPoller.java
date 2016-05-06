/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.DMSPoller;
import us.mn.state.dot.tms.server.comm.LCSPoller;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.snmp.SNMP;

/**
 * NtcipPoller
 *
 * @author Douglas Lau
 */
public class NtcipPoller extends MessagePoller implements DMSPoller, LCSPoller {

	/** SNMP message protocol */
	private final SNMP snmp = new SNMP();

	/** Create a new Ntcip poller */
	public NtcipPoller(String n, Messenger m) {
		super(n, m);
	}

	/** Create a new message for the specified operation */
	@Override
	protected CommMessage createCommMessage(OpController o)
		throws IOException
	{
		ControllerImpl c = o.getController();
		return snmp.new Message(messenger.getOutputStream(c),
			messenger.getInputStream("", c), c.getPassword());
	}

	/** Send a device request message to the sign */
	@SuppressWarnings("unchecked")
	@Override
	public void sendRequest(DMSImpl dms, DeviceRequest r) {
		switch (r) {
		case RESET_DEVICE:
			addOperation(new OpResetDMS(dms));
			break;
		case SEND_SETTINGS:
			addOperation(new OpSendDMSFonts(dms));
			addOperation(new OpSendDMSDefaults(dms));
			break;
		case QUERY_CONFIGURATION:
			addOperation(new OpQueryDMSConfiguration(dms));
			break;
		case QUERY_MESSAGE:
			addOperation(new OpQueryDMSMessage(dms));
			break;
		case QUERY_STATUS:
			addOperation(new OpQueryDMSStatus(dms));
			break;
		case QUERY_PIXEL_FAILURES:
			addOperation(new OpTestDMSPixels(dms, false));
			break;
		case TEST_PIXELS:
			addOperation(new OpTestDMSPixels(dms, true));
			break;
		case BRIGHTNESS_TOO_DIM:
			addOperation(new OpUpdateDMSBrightness(dms,
				EventType.DMS_BRIGHT_LOW));
			break;
		case BRIGHTNESS_GOOD:
			addOperation(new OpUpdateDMSBrightness(dms,
				EventType.DMS_BRIGHT_GOOD));
			break;
		case BRIGHTNESS_TOO_BRIGHT:
			addOperation(new OpUpdateDMSBrightness(dms,
				EventType.DMS_BRIGHT_HIGH));
			break;
		case SEND_LEDSTAR_SETTINGS:
			addOperation(new OpSendDMSLedstar(dms));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Send a new message to the sign */
	@SuppressWarnings("unchecked")
	@Override
	public void sendMessage(DMSImpl dms, SignMessage sm, User o)
		throws InvalidMessageException
	{
		if (dms.isMessageCurrentEquivalent(sm))
			addOperation(new OpUpdateDMSDuration(dms, sm));
		else
			addOperation(new OpSendDMSMessage(dms, sm, o));
	}

	/** Send a device request message to an LCS array */
	@SuppressWarnings("unchecked")
	@Override
	public void sendRequest(LCSArrayImpl lcs_array, DeviceRequest r) {
		switch (r) {
		case SEND_SETTINGS:
			addOperation(new OpSendLCSSettings(lcs_array));
			break;
		case QUERY_MESSAGE:
			addOperation(new OpQueryLCSIndications(lcs_array));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Send new indications to an LCS array.
	 * @param lcs_array LCS array.
	 * @param ind New lane use indications.
	 * @param o User who deployed the indications. */
	@SuppressWarnings("unchecked")
	@Override
	public void sendIndications(LCSArrayImpl lcs_array, Integer[] ind,
		User o)
	{
		addOperation(new OpSendLCSIndications(lcs_array, ind, o));
	}
}
