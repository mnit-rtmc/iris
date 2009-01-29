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
package us.mn.state.dot.tms.comm.ntcip;

import java.io.EOFException;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.BaseObjectImpl;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontImpl;
import us.mn.state.dot.tms.PixelMapBuilder;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignRequest;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DiagnosticOperation;
import us.mn.state.dot.tms.comm.DMSPoller;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.Messenger;

/**
 * NtcipPoller
 *
 * @author Douglas Lau
 */
public class NtcipPoller extends MessagePoller implements DMSPoller {

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

	/** Simple class to download fonts to a sign */
	protected class FontDownloader implements Checker<Font> {
		static protected final int FIRST_INDEX = 2;
		protected final DMSImpl dms;
		protected final int priority;
		protected int index = 2;
		protected FontDownloader(DMSImpl d, int p) {
			dms = d;
			priority = p;
		}
		public boolean check(Font font) {
			DMSFontDownload f = new DMSFontDownload(dms,
				(FontImpl)font, index, index == FIRST_INDEX);
			f.setPriority(priority);
			f.start();
			index++;
			return false;
		}
	}

	/** Download the font to a sign controller */
	protected void downloadFonts(DMSImpl dms, int p) {
		Integer w = dms.getWidthPixels();
		Integer h = dms.getHeightPixels();
		Integer cw = dms.getCharWidthPixels();
		Integer ch = dms.getCharHeightPixels();
		if(w != null && h != null && cw != null && ch != null) {
			PixelMapBuilder builder = new PixelMapBuilder(
				BaseObjectImpl.namespace, w, h, cw, ch);
			builder.findFonts(new FontDownloader(dms, p));
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

	/** Send a sign request message to the sign */
	public void sendRequest(DMSImpl dms, SignRequest r) {
		switch(r) {
		case QUERY_CONFIGURATION:
			new DMSQueryConfiguration(dms).start();
			break;
		case QUERY_MESSAGE:
			new DMSQueryMessage(dms).start();
			break;
		case QUERY_STATUS:
			new DMSQueryStatus(dms).start();
			break;
		case QUERY_PIXEL_FAILURES:
			new DMSQueryPixelFailures(dms).start();
			break;
		case TEST_PIXELS:
			new DMSPixelTest(dms).start();
			break;
		case TEST_LAMPS:
			new DMSLampTest(dms).start();
			break;
		case BRIGHTNESS_GOOD:
		case BRIGHTNESS_TOO_DIM:
		case BRIGHTNESS_TOO_BRIGHT:
			new DMSBrightnessFeedback(dms, r).start();
			break;
		case SEND_LEDSTAR_SETTINGS:
			new DMSSetLedstarPixel(dms).start();
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Send a new message to the sign */
	public void sendMessage(DMSImpl dms, SignMessage m)
		throws InvalidMessageException
	{
		if(shouldSetTimeRemaining(dms, m))
			new DMSSetTimeRemaining(dms, m).start();
		else
			new DMSCommandMessage(dms, m).start();
	}

	/** Check if we should just set the message time remaining */
	protected boolean shouldSetTimeRemaining(DMSImpl dms, SignMessage m) {
		return isDurationZero(m) || isMessageDeployed(dms, m);
	}

	/** Check if the duration of a message is zero */
	protected boolean isDurationZero(SignMessage m) {
		return m.getDuration() != null && m.getDuration() <= 0;
	}

	/** Check if the message is already deployed on the sign */
	protected boolean isMessageDeployed(DMSImpl dms, SignMessage m) {
		return m.getMulti().equals(dms.getMessageCurrent().getMulti());
	}
}
