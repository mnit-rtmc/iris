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
package us.mn.state.dot.tms.server.comm.dmslite;

import java.io.EOFException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.server.UserImpl;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignRequest;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.DMSPoller;
import us.mn.state.dot.tms.server.comm.DiagnosticOperation;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.SocketMessenger;
import us.mn.state.dot.tms.utils.Log;

/**
 * DmsLitePoller. This class provides a DMS Poller developed
 * to support the Caltrans D10 IRIS implementation.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DmsLitePoller extends MessagePoller implements DMSPoller {

	/** valid address range (inclusive) */
	static public final int MAX_ADDRESS = 255;
	static public final int MIN_ADDRESS = 1;

	/** Create a new dmslite poller */
	public DmsLitePoller(String n, Messenger m) {
		super(n, m);
		assert m instanceof SocketMessenger;
	}

	/**
	 * Create a new message for the specified controller. Called
	 * by parent MessagePoller.doPoll().
	 *
	 * @param c Associated controller.
	 * @return A newly created Message.
	 * @throws EOFException
	 */
	public AddressedMessage createMessage(ControllerImpl c)
		throws EOFException
	{
		//Log.finest("DmsLitePoller.createMessage() called.");
		return new Message(messenger.getOutputStream(c),
				   messenger.getInputStream(c));
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		return ((drop >= MIN_ADDRESS) && (drop <= MAX_ADDRESS));
	}

	/** 
	 * Perform a controller download. Called when the IRIS server is shutting down, 
	 * when the 'reset' button is pressed on the controller status tab. 
	 */
	public void download(ControllerImpl c, boolean reset, int p) {
		DMSImpl dms = c.getActiveSign();
		if(dms == null)
			return;

		// reset button pressed
		if(reset)
			sendRequest(dms, SignRequest.RESET_DMS);
		// download button pressed
		else
			sendRequest(dms, SignRequest.QUERY_MESSAGE);
	}

	/** Perform a 30-second poll */
	public void poll30Second(ControllerImpl c, Completer comp) {
	}

	/** Perform a 5-minute poll */
	public void poll5Minute(ControllerImpl c, Completer comp) {
	}

	/** Start a test for the given controller */
	public DiagnosticOperation startTest(ControllerImpl c) {
		return null;
	}

	/**
	 * Send a new message to the sign. Called by DMSImpl.
	 * @throws InvalidMessageException
	 * @see DMSImpl,DMS
	 */
	public void sendMessage(DMSImpl dms, SignMessage m, User o)
		throws InvalidMessageException
	{
		if(dms == null || m == null || m.getBitmaps() == null)
			return;
		// Are the DMS width and height valid?  If not, it's probably
		// because a OpQueryConfig message has not been received yet,
		// so the DMS physical properties are not yet valid.
		if(dms.getWidthPixels() == null || dms.getHeightPixels() == null)
			return;

		// blank the sign
		if(m.getDuration() != null && m.getDuration() <= 0) {
			new OpBlank(dms, m, o).start();
			return;
		}

		// send message to field controller
		new OpMessage(dms, m, o).start();
	}


	/** Send a sign request message to the sign, no user specified */
	public void sendRequest(DMSImpl dms, SignRequest r) {
		// user assumed to be IRIS
		User u = null; //UserImpl.create("IRIS");
		//u.setFullName("IRIS");
		sendRequest(dms, r, u);
	}

	/** Send a sign request message to the sign from a specific user */
	public void sendRequest(DMSImpl dms, SignRequest r, User u) {
		if(dms == null)
			return;
		switch(r) {
		case QUERY_CONFIGURATION:
			new OpQueryConfig(dms, u).start();
			break;
		case QUERY_MESSAGE:
		case QUERY_STATUS:
			new OpQueryMsg(dms, u).start();
			break;
		case RESET_DMS:
			new OpReset(dms, u).start();
			break;
		case RESET_MODEM:
			//new OpResetModem(dms, u).start();
			break;
		default:
			// Ignore other requests
			Log.warning("DmsLitePoller: "+
				"unknown request in sendRequest(). "+
				"r="+r+", desc="+r.description);
			break;
		}
	}
}
