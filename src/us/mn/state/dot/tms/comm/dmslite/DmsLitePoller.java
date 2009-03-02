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
package us.mn.state.dot.tms.comm.dmslite;

import java.io.EOFException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignRequest;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DMSPoller;
import us.mn.state.dot.tms.comm.DiagnosticOperation;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.Messenger;
import us.mn.state.dot.tms.comm.SocketMessenger;

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
		//System.err.println("DmsLitePoller.createMessage() called.");
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
		if (dms == null)
			return;

		// reset button pressed
		if (reset) {
			sendRequest(dms, SignRequest.RESET_DMS);

		// download button pressed
		} else {
			// start operation
			new OpQueryMsg(dms).start();
		}
	}

	/** Perform a sign status poll. Called every 60 seconds, via TimerJobSigns */
	public void pollSigns(ControllerImpl c, Completer comp) {
		//System.err.println("DmsLitePoller.pollSigns() called.");

		DMSImpl dms = c.getActiveSign();
		if (dms == null)
			return;

		// don't poll signs connected by modem
		if (containsIgnoreCase(dms.getSignAccess(),"modem"))
			return;

		// start operation
		new OpQueryMsg(dms).start();
	}

	/**
	 *  Does a string contain another string?
	 *  @param arg1 string 1
	 *  @param arg2 string 2
	 *  @return true if string1 contains string2, case insensitive.
	 */
	private static boolean containsIgnoreCase(String arg1,String arg2) {
		if (arg1==null || arg2==null)
			return false;
		if (arg1.length()<=0 || arg2.length()<=0)
			return false;
		return arg1.toLowerCase().contains(arg2.toLowerCase());
	}

	/** Perform a 30-second poll */
	public void poll30Second(ControllerImpl c, Completer comp) {
		//System.err.println("DmsLitePoller.poll30Second() called, ignored.");
	}

	/** Perform a 5-minute poll */
	public void poll5Minute(ControllerImpl c, Completer comp) {
		//System.err.println("DmsLitePoller.poll5Minute() called, ignored.");
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
		// sanity checks
		if(m.getBitmaps() == null) {
			System.err.println("Warning: DmsLitePoller.sendMessage(): bitmap is null, ignored.");
			return;
		}
		// Are the DMS width and height valid?  If not, it's probably
		// because a OpQueryConfig message has not been received yet,
		// so the DMS physical properties are not yet valid.
		if(dms.getWidthPixels() == null || dms.getHeightPixels() ==null)
			return;

		// blank the sign
		if(m.getDuration() != null && m.getDuration() <= 0) {
			new OpBlank(dms, m, o).start();
			return;
		}

		// Note: in the future, check for SV170 firmware version, if
		//       start and stop times are supported, adjust the CMS
		//       stop time and send the message.

		// finally, send message to field controller
		OpMessage cmd = new OpMessage(dms, m, o);
		cmd.start();
	}

	/** Send a sign request message to the sign */
	public void sendRequest(DMSImpl dms, SignRequest r) {
		switch(r) {
		case QUERY_CONFIGURATION:
			new OpQueryConfig(dms).start();
			break;
		case QUERY_MESSAGE:
			new OpQueryMsg(dms).start();
			break;
		case RESET_DMS:
			new OpReset(dms).start();
			break;
		case RESET_MODEM:
			new OpResetModem(dms).start();
			break;
		default:
			// Ignore other requests
			break;
		}
	}
}
