/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dinrelay;

import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.IDebugLog;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.LCSPoller;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.HttpFileMessenger;

/**
 * Poller to control Digital Loggers Inc DIN Relay devices.
 *
 * @author Douglas Lau
 */
public class DinRelayPoller extends MessagePoller implements LCSPoller {

	/** DIN relay debug log */
	static private final IDebugLog DIN_LOG = new IDebugLog("dinrelay");

	/** Log a message to the debug log */
	static public void log(String msg) {
		DIN_LOG.log(msg);
	}

	/** HTTP messenger */
	private final HttpFileMessenger http_mess;

	/** Create a new DIN relay poller */
	public DinRelayPoller(String n, HttpFileMessenger m) {
		super(n, m);
		log("creating DIN relay poller");
		http_mess = m;
	}

	/** Create a new message for the specified controller, 
	 *  called by MessagePoller.doPoll(). */
	public CommMessage createMessage(ControllerImpl c) {
		return new Message(http_mess, c);
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		return true;
	}

	/** Query the outlet status */
	public void queryOutlets(ControllerImpl c, OutletProperty op) {
		log("creating OpQueryOutlets: " + c);
		addOperation(new OpQueryOutlets(c, op));
	}

	/** Command the outlet status */
	public void commandOutlets(ControllerImpl c, boolean[] outlets,
		OutletProperty op)
	{
		log("creating OpCommandOutlets: " + c);
		addOperation(new OpCommandOutlets(c, outlets, op));
	}

	/** Send a device request */
	public void sendRequest(LCSArrayImpl lcs_array, DeviceRequest r) {
		switch(r) {
		case SEND_SETTINGS:
			log("creating OpSendLCSSettings: " + lcs_array);
			addOperation(new OpSendLCSSettings(lcs_array));
			break;
		case QUERY_MESSAGE:
			log("creating OpQueryLCSIndications: " + lcs_array);
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
	public void sendIndications(LCSArrayImpl lcs_array, Integer[] ind,
		User o)
	{
		log("creating OpSendLCSIndications: " + lcs_array);
		addOperation(new OpSendLCSIndications(lcs_array, ind, o));
	}
}
