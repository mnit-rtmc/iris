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

import java.io.EOFException;
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

	/** Download the font to a sign controller */
	protected void downloadFonts(DMSImpl dms, int p) {
		//System.err.println("DmsLitePoller.downloadFonts() called, ignored.");
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
			this.reset(dms);

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
	 * Query the DMS configuration. This method is called at the successful
	 * completion of each operation.
	 */
	public void queryConfiguration(DMSImpl dms) {
		//System.err.println("DmsLitePoller.queryConfiguration() called.");
		new OpQueryConfig(dms).start();
	}

	/**
	 * Send a new message to the sign. Called by DMSImpl.
	 * @throws InvalidMessageException
	 * @see DMSImpl,DMS
	 */
	public void sendMessage(DMSImpl dms, SignMessage m)
		throws InvalidMessageException {
		//System.err.println("DmsLitePoller.sendMessage() called.");

		// sanity checks
		if (dms==null || m==null)
			return;
		if (m.getBitmap()==null) {
			System.err.println("Warning: DmsLitePoller.sendMessage(): bitmap is null, ignored.");
			return;
		}
		if (m.getBitmap().getBitmap()==null) {
			System.err.println("Warning: DmsLitePoller.sendMessage():m.getBitmap().getBitmap() is null, ignored.");
			return;
		}
		// was bitmap rendered? If not, it's probably because a GetDMSConfig message has
		// not been received yet, so the DMS physical properties are not yet valid.
		if (m.getBitmap().getBitmap().length <= 0) {
			//System.err.println("Warning: DmsLitePoller.sendMessage(): m.getBitmap().getBitmap().length<=0, ignored.");
			return;
		}

		//System.err.println("DmsLitePoller.sendMessage(), SignMessage multistring="
		//	+ m.getMulti().toString());
		//System.err.println("DmsLitePoller.sendMessage(), bitmap len="
		//	+m.getBitmap().getBitmap().length);
		//System.err.println("DmsLitePoller.sendMessage(), bitmap="
		//    + HexString.toHexString(m.getBitmap().getBitmap()));

		// finally, send message to field controller
		OpMessage cmd = new OpMessage(dms, m);
		cmd.start();
	}

	/**
	 * Set the time remaining for the currently displayed message. This is
	 * called when:
	 * 	-The "clear" button in the IRIS client is pressed.
	 *	-An existing travel time message 
	 */
	public void setMessageTimeRemaining(DMSImpl dms, SignMessage m) {
		//System.err.println("DmsLitePoller.setMessageTimeRemaining() called, duration:"
		//	+ m.getDuration());

		// blank the sign
		if (m.getDuration() <= 0) {
			new OpBlank(dms, m).start();
			return;
		}

		// Note: in the future, check for SV170 firmware version, if start and stop
		//       times are supported, adjust the CMS stop time and send the message.

		// should never get here
		String msg="WARNING: DmsLitePoller.setMessageTimeRemaining(): should never get here, duration="+m.getDuration();
		System.err.println(msg);
		//assert false : msg;
	}

	/**
	 * Set manual brightness level (null for photocell control)
	 */
	public void setBrightnessLevel(DMSImpl dms, Integer l) {
	}

	/**
	 * Activate a pixel test, which performs a dms query.
	 */
	public void testPixels(DMSImpl dms) {
	}

	/**
	 * Activate a lamp test
	 */
	public void testLamps(DMSImpl dms) {
	}

	/**
	 * Activate a fan test
	 */
	public void testFans(DMSImpl dms) {
	}

	/** 
	 * Reset the dms, called from DMSImpl.reset(), via button on 
	 * the dms status tab. 
	 */
	public void reset(DMSImpl dms) {
		if (dms == null)
			return;
		new OpReset(dms).start();
	}

	/** 
	 * Reset the dms modem, called from DMSImpl.resetModem(), via button on 
	 * the dms status tab. 
	 */
	public void resetModem(DMSImpl dms) {
		if (dms == null)
			return;
		new OpResetModem(dms).start();
	}

	/** 
	 * Get the sign message, called from DMSImpl.getSignMessage(), 
	 * via button on the dms status tab. 
	 */
	public void getSignMessage(DMSImpl dms) {
		if (dms == null)
			return;
		new OpQueryMsg(dms).start();
	}

	/**
	 * Set Ledstar pixel configuration
	 */
	public void setLedstarPixel(DMSImpl dms, int ldcPotBase,
		int pixelCurrentLow, int pixelCurrentHigh,int badPixelLimit) {
	}
}
