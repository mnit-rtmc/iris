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

	/** Field description */
	static public final int MAX_ADDRESS = 255;

	// consts

	/** Field description */
	static public final int MIN_ADDRESS = 1;

	/**
	 * Create a new dmslite poller
	 *
	 * @param n
	 * @param m
	 */
	public DmsLitePoller(String n, Messenger m) {
		super(n, m);
		assert m instanceof SocketMessenger;
	}

	/**
	 * Create a new message for the specified controller. Called
	 * by parent MessagePoller.doPoll().
	 *
	 * @param c
	 *
	 * @return
	 *
	 * @throws MessengerException
	 */
	public AddressedMessage createMessage(ControllerImpl c)
		throws MessengerException {
		System.err.println("DmsLitePoller.createMessage() called.");

		return new Message(messenger.getOutputStream(c),
				   messenger.getInputStream(c));
	}

	/**
	 * Check if a drop address is valid
	 */
	public boolean isAddressValid(int drop) {
		return ((drop >= MIN_ADDRESS) && (drop <= MAX_ADDRESS));
	}

	/**
	 * Download the font to a sign controller
	 */
	protected void downloadFonts(DMSImpl dms, int p) {
		System.err.println(
		    "DmsLitePoller.downloadFonts() called, ignored.");
	}

	/**
	 * Perform a controller download. Called: when IRIS server is shutting down, etc.
	 */
	public void download(ControllerImpl c, boolean reset, int p) {
		System.err.println("DmsLitePoller.download() called, ignored.");

		/*
		 *       DMSImpl dms = c.getActiveSign();
		 *       if(dms != null) {
		 *               if(reset) {
		 *       System.err.println("DmsLitePoller.download(): reset requested by caller.");
		 *                       //DMSReset r = new DMSReset(dms);
		 *                       //r.setPriority(p);
		 *                       //r.start();
		 *               }
		 *               //if(dms.hasProportionalFonts())
		 *               //      downloadFonts(dms, p);
		 *               OpDmsDefaultDownload o = new OpDmsDefaultDownload(dms);
		 *               o.setPriority(p);
		 *               o.start();
		 *       }
		 */
	}

	/**
	 * Perform a sign status poll. Called every 30 seconds.
	 */
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
		System.err.println("DmsLitePoller.Second() called, ignored.");
	}

	/**
	 * Perform a 5-minute poll
	 */
	public void poll5Minute(ControllerImpl c, Completer comp) {
		System.err.println("DmsLitePoller.Minute() called, ignored.");
	}

	/**
	 * Start a test for the given controller
	 *
	 * @param c
	 *
	 * @return
	 */
	public DiagnosticOperation startTest(ControllerImpl c) {
		System.err.println(
		    "DmsLitePoller.startTest() called, ignored.");

		// DiagnosticOperation test = new DiagnosticNtcip(c);
		// test.start();
		// return test;
		return null;
	}

	/**
	 * Query the DMS configuration. This method is called at the successful
	 * completion of each operation.
	 *
	 * @param dms
	 */
	public void queryConfiguration(DMSImpl dms) {
		System.err.println(
		    "DmsLitePoller.queryConfiguration() called.");
		new OpDmsQueryConfig(dms).start();
	}

	/**
	 * Send a new message to the sign
	 *
	 * @param dms
	 * @param m
	 *
	 * @throws InvalidMessageException
	 */
	public void sendMessage(DMSImpl dms, SignMessage m)
		throws InvalidMessageException {
		System.err.println("DmsLitePoller.sendMessage() called.");
		System.err.println(
		    "DmsLitePoller.sendMessage(), SignMessage multistring="
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

		// create operation
		OpCommandMessage cmd = new OpCommandMessage(dms, m);

		// needed?

		/*
		 * if (m instanceof SignTravelTime) {
		 *   // Avoid race with user/alert messages
		 *   if (dms.acquire(cmd) != cmd) {
		 *       return;
		 *   }
		 * }
		 */

		// start operation
		cmd.start();
	}

	/**
	 * Set the time remaining for the currently displayed message. This is
	 * called when the "clear" button in the IRIS client is pressed.
	 *
	 * @param dms
	 * @param m
	 */
	public void setMessageTimeRemaining(DMSImpl dms, SignMessage m) {
		System.err.println(
		    "DmsLitePoller.setMessageTimeRemaining() called, duration:"
		    + m.getDuration());

		// blank the sign
		if (m.getDuration() <= 0) {
			new OpDmsBlank(dms, m).start();

		// error
		} else {

			// FIXME: add support for changing ontime for message
			System.err.println(
			    "ERROR: DmsLitePoller.setMessageTimeRemaining(): called with non zero duration ("
			    + m.getDuration() + "). Ignored.");
		}

		/*
		 *       DMSSetTimeRemaining cmd = new DMSSetTimeRemaining(dms, m);
		 *       if(m instanceof SignTravelTime) {
		 *               // Avoid races with user/alert messages
		 *               if(dms.acquire(cmd) != cmd)
		 *                       return;
		 *       }
		 *       cmd.start();
		 */
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
		System.err.println(
		    "DmsLitePoller.testLamps() called, ignored.");

		// new DMSLampTest(dms).start();
	}

	/**
	 * Activate a fan test
	 */
	public void testFans(DMSImpl dms) {
		System.err.println("DmsLitePoller.testFans() called, ignored.");

		// new DMSFanTest(dms).start();
	}

	/**
	 * Set Ledstar pixel configuration
	 *
	 * @param dms
	 * @param ldcPotBase
	 * @param pixelCurrentLow
	 * @param pixelCurrentHigh
	 * @param badPixelLimit
	 */
	public void setLedstarPixel(DMSImpl dms, int ldcPotBase,
				    int pixelCurrentLow, int pixelCurrentHigh,
				    int badPixelLimit) {
		System.err.println(
		    "DmsLitePoller.setLedstarPixel() called, ignored.");

		// new DMSSetLedstarPixel(dms, ldcPotBase, pixelCurrentLow,
		// pixelCurrentHigh, badPixelLimit).start();
	}
}
