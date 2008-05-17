/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.FontImpl;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignTravelTime;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DMSPoller;
import us.mn.state.dot.tms.comm.DiagnosticOperation;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.Messenger;
import us.mn.state.dot.tms.comm.MessengerException;
import us.mn.state.dot.tms.comm.SignPoller;
import us.mn.state.dot.tms.comm.SocketMessenger;

/**
 * DmsLitePoller. This class provides a DMS Poller developed
 * to support the Caltrans D10 IRIS implementation.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DmsLitePoller extends MessagePoller
	implements SignPoller, DMSPoller {

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
	 * @throws MessengerException
	 */
	public AddressedMessage createMessage(ControllerImpl c)
		throws MessengerException {
		System.err.println("DmsLitePoller.createMessage() called.");
		return new Message(messenger.getOutputStream(c),
				   messenger.getInputStream(c));
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		return ((drop >= MIN_ADDRESS) && (drop <= MAX_ADDRESS));
	}

	/** Download the font to a sign controller */
	protected void downloadFonts(DMSImpl dms, int p) {
		System.err.println("DmsLitePoller.downloadFonts() called, ignored.");
	}

	/** Perform a controller download. Called: when IRIS server is shutting down, etc. */
	public void download(ControllerImpl c, boolean reset, int p) {
		System.err.println("DmsLitePoller.download() called, ignored.");
	}

	/** Perform a sign status poll. Called every 30 seconds */
	public void pollSigns(ControllerImpl c, Completer comp) {
		System.err.println("DmsLitePoller.pollSigns() called.");

		DMSImpl dms = c.getActiveSign();
		if (dms == null)
			return;

		// don't poll signs connected by modem
		if (dms.getSignAccess().equalsIgnoreCase("modem")) {
			return;
		}

		// start operation
		new OpQueryDms(dms).start();
	}

	/** Perform a 30-second poll */
	public void poll30Second(ControllerImpl c, Completer comp) {
		System.err.println("DmsLitePoller.poll30Second() called, ignored.");
	}

	/** Perform a 5-minute poll */
	public void poll5Minute(ControllerImpl c, Completer comp) {
		System.err.println("DmsLitePoller.poll5Minute() called, ignored.");
	}

	/** Start a test for the given controller */
	public DiagnosticOperation startTest(ControllerImpl c) {
		System.err.println("DmsLitePoller.startTest() called, ignored.");
		return null;
	}

	/**
	 * Query the DMS configuration. This method is called at the successful
	 * completion of each operation.
	 */
	public void queryConfiguration(DMSImpl dms) {
		System.err.println("DmsLitePoller.queryConfiguration() called.");
		new OpDmsQueryConfig(dms).start();
	}

	/**
	 * Send a new message to the sign
	 * @throws InvalidMessageException
	 */
	public void sendMessage(DMSImpl dms, SignMessage m)
		throws InvalidMessageException {
		System.err.println("DmsLitePoller.sendMessage() called.");
		System.err.println("DmsLitePoller.sendMessage(), SignMessage multistring="
		    + m.getMulti().toString() + ",bitmap len="
		    + m.getBitmap().getBitmap().length + ", bitmap="
		    + Convert.toHexString(m.getBitmap().getBitmap()));

		// was bitmap rendered? If not, it's probably because a GetDMSConfig message has
		// not been received yet, so the DMS physical properties are not yet valid.
		if (m.getBitmap().getBitmap().length <= 0) {
			System.err.println(
			    "DmsLitePoller.sendMessage(): bitmap has a zero length. Message ignored.");

			return;
		}

		OpCommandMessage cmd = new OpCommandMessage(dms, m);
		cmd.start();
	}

	/**
	 * Set the time remaining for the currently displayed message. This is
	 * called when the "clear" button in the IRIS client is pressed.
	 */
	public void setMessageTimeRemaining(DMSImpl dms, SignMessage m) {
		System.err.println(
		    "DmsLitePoller.setMessageTimeRemaining() called, duration:"
		    + m.getDuration());

		// blank the sign
		if (m.getDuration() <= 0) {
			new OpDmsBlank(dms, m).start();
			return;
		}

		// should never get here
		System.err.println(
		    "ERROR: DmsLitePoller.setMessageTimeRemaining(): should never get here, duration="+m.getDuration());
	}

	/**
	 * Set manual brightness level (null for photocell control)
	 */
	public void setBrightnessLevel(DMSImpl dms, Integer l) {
		System.err.println(
		    "DmsLitePoller.setBrightnessLevel() called, ignored.");
	}

	/**
	 * Activate a pixel test, which performs a dms query.
	 */
	public void testPixels(DMSImpl dms) {
		System.err.println(
		    "DmsLitePoller.testPixels() called, performing query operation.");

		if (dms == null)
			return;

		// start operation
		new OpQueryDms(dms).start();
	}

	/**
	 * Activate a lamp test
	 */
	public void testLamps(DMSImpl dms) {
		System.err.println("DmsLitePoller.testLamps() called, ignored.");
	}

	/**
	 * Activate a fan test
	 */
	public void testFans(DMSImpl dms) {
		System.err.println("DmsLitePoller.testFans() called, ignored.");
	}

	/**
	 * Set Ledstar pixel configuration
	 */
	public void setLedstarPixel(DMSImpl dms, int ldcPotBase,
				    int pixelCurrentLow, int pixelCurrentHigh,
				    int badPixelLimit) {
		System.err.println("DmsLitePoller.setLedstarPixel() called, ignored.");
	}
}
