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
package us.mn.state.dot.tms.comm.ntcip;

import java.io.EOFException;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.FontImpl;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignTravelTime;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DiagnosticOperation;
import us.mn.state.dot.tms.comm.DMSPoller;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.Messenger;
import us.mn.state.dot.tms.comm.SignPoller;

/**
 * NtcipPoller
 *
 * @author Douglas Lau
 */
public class NtcipPoller extends MessagePoller implements SignPoller,
	DMSPoller
{
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
			messenger.getInputStream(c));
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		// FIXME: this doesn't belong here
		return drop > 0 && drop <= HDLC.NTCIP_MAX_ADDRESS;
	}

	/** Download the font to a sign controller */
	protected void downloadFonts(DMSImpl dms, int p) {
		FontImpl font = dms.getFont();
		if(font != null) {
			DMSFontDownload f = new DMSFontDownload(dms, font);
			f.setPriority(p);
			f.start();
		}
	}

	/** Perform a controller download */
	public void download(ControllerImpl c, boolean reset, int p) {
		DMSImpl dms = c.getActiveSign();
		if(dms != null) {
			if(reset) {
				DMSReset r = new DMSReset(dms);
				r.setPriority(p);
				r.start();
			}
			if(dms.hasProportionalFonts())
				downloadFonts(dms, p);
			DMSDefaultDownload o = new DMSDefaultDownload(dms);
			o.setPriority(p);
			o.start();
		}
	}

	/** Perform a sign status poll */
	public void pollSigns(ControllerImpl c, Completer comp) {
		DMSImpl dms = c.getActiveSign();
		if(dms != null)
			new DMSQueryMessage(dms).start();
	}

	/** Perform a 30-second poll */
	public void poll30Second(ControllerImpl c, Completer comp) {
		// Nothing to do here
	}

	/** Perform a 5-minute poll */
	public void poll5Minute(ControllerImpl c, Completer comp) {
		DMSImpl dms = c.getActiveSign();
		if(dms != null)
			new DMSQueryStatus(dms).start();
	}

	/** Start a test for the given controller */
	public DiagnosticOperation startTest(ControllerImpl c) {
		DiagnosticOperation test = new DiagnosticNtcip(c);
		test.start();
		return test;
	}

	/** Query the DMS configuration */
	public void queryConfiguration(DMSImpl dms) {
		new DMSQueryConfiguration(dms).start();
	}

	/** Send a new message to the sign */
	public void sendMessage(DMSImpl dms, SignMessage m)
		throws InvalidMessageException
	{
		DMSCommandMessage cmd = new DMSCommandMessage(dms, m);
		if(m instanceof SignTravelTime) {
			// Avoid race with user/alert messages
			if(dms.acquire(cmd) != cmd)
				return;
		}
		cmd.start();
	}

	/** Set the time remaining for the currently displayed message */
	public void setMessageTimeRemaining(DMSImpl dms, SignMessage m) {
		DMSSetTimeRemaining cmd = new DMSSetTimeRemaining(dms, m);
		if(m instanceof SignTravelTime) {
			// Avoid races with user/alert messages
			if(dms.acquire(cmd) != cmd)
				return;
		}
		cmd.start();
	}

	/** Set manual brightness level (null for photocell control) */
	public void setBrightnessLevel(DMSImpl dms, Integer l) {
		if(l != null) {
			// FIXME: combine these into one operation
			new DMSManualBrightness(dms, l).start();
			new DMSBrightnessControl(dms, true).start();
		} else
			new DMSBrightnessControl(dms, false).start();
	}

	/** Activate a pixel test */
	public void testPixels(DMSImpl dms) {
		new DMSPixelTest(dms).start();
	}

	/** Activate a lamp test */
	public void testLamps(DMSImpl dms) {
		new DMSLampTest(dms).start();
	}

	/** Activate a fan test */
	public void testFans(DMSImpl dms) {
		new DMSFanTest(dms).start();
	}

	/** Set Ledstar pixel configuration */
	public void setLedstarPixel(DMSImpl dms, int ldcPotBase,
		int pixelCurrentLow, int pixelCurrentHigh, int badPixelLimit)
	{
		new DMSSetLedstarPixel(dms, ldcPotBase, pixelCurrentLow,
			pixelCurrentHigh, badPixelLimit).start();
	}
}
