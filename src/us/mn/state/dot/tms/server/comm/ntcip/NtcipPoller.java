/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

import java.io.EOFException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.DMSPoller;
import us.mn.state.dot.tms.server.comm.LCSPoller;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;

/**
 * NtcipPoller
 *
 * @author Douglas Lau
 */
public class NtcipPoller extends MessagePoller implements DMSPoller, LCSPoller {

	/** Create an operation to send a DMS message */
	static protected OpDMS createOperation(DMSImpl dms, SignMessage sm,
		User o)
	{
		if(shouldSetTimeRemaining(dms, sm))
			return new OpUpdateDMSDuration(dms, sm, o);
		else
			return new OpSendDMSMessage(dms, sm, o);
	}

	/** Check if we should just set the message time remaining */
	static protected boolean shouldSetTimeRemaining(DMSImpl dms,
		SignMessage m)
	{
		return SignMessageHelper.isDurationZero(m) ||
		       isMessageDeployed(dms, m);
	}

	/** Check if the message is already deployed on the sign */
	static protected boolean isMessageDeployed(DMSImpl dms, SignMessage m) {
		return m.getMulti().equals(dms.getMessageCurrent().getMulti());
	}

	/** SNMP message protocol */
	protected final SNMP snmp = new SNMP();

	/** Create a new Ntcip poller */
	public NtcipPoller(String n, Messenger m) {
		super(n, m);
	}

	/** Create a new message for the specified controller */
	public AddressedMessage createMessage(ControllerImpl c)
		throws EOFException
	{
		return snmp.new Message(messenger.getOutputStream(c),
			messenger.getInputStream(c), c.getPassword());
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		// FIXME: this doesn't belong here
		return drop > 0 && drop <= HDLC.NTCIP_MAX_ADDRESS;
	}

	/** Send a device request message to the sign */
	public void sendRequest(DMSImpl dms, DeviceRequest r) {
		switch(r) {
		case RESET_DEVICE:
			new OpResetDMS(dms).start();
			break;
		case SEND_SETTINGS:
			new OpSendDMSFonts(dms).start();
			new OpSendDMSDefaults(dms).start();
			break;
		case QUERY_CONFIGURATION:
			new OpQueryDMSConfiguration(dms).start();
			break;
		case QUERY_MESSAGE:
			new OpQueryDMSMessage(dms).start();
			break;
		case QUERY_STATUS:
			new OpQueryDMSStatus(dms).start();
			break;
		case QUERY_PIXEL_FAILURES:
			new OpTestDMSPixels(dms, false).start();
			break;
		case TEST_PIXELS:
			new OpTestDMSPixels(dms, true).start();
			break;
		case TEST_LAMPS:
			new OpTestDMSLamps(dms).start();
			break;
		case BRIGHTNESS_GOOD:
		case BRIGHTNESS_TOO_DIM:
		case BRIGHTNESS_TOO_BRIGHT:
			new OpUpdateDMSBrightness(dms, r).start();
			break;
		case SEND_LEDSTAR_SETTINGS:
			new OpSendDMSLedstar(dms).start();
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Send a new message to the sign */
	public void sendMessage(DMSImpl dms, SignMessage sm, User o)
		throws InvalidMessageException
	{
		createOperation(dms, sm, o).start();
	}

	/** Send a device request message to an LCS array */
	public void sendRequest(LCSArrayImpl lcs_array, DeviceRequest r) {
		switch(r) {
		case SEND_SETTINGS:
			new OpSendLCSSettings(lcs_array).start();
			break;
		case QUERY_MESSAGE:
			new OpQueryLCSIndications(lcs_array).start();
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
	public void sendIndications(LCSArrayImpl lcs_array, Integer[] ind,
		User o)
	{
		new OpSendLCSIndications(lcs_array, ind, o).start();
	}
}
