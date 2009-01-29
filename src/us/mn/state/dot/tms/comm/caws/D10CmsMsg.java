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
package us.mn.state.dot.tms.comm.caws;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.SString;

/**
 * CAWS D10CmsMsg. This is a single CMS message.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class D10CmsMsg {

	// consts
	private static final String DESC_BLANK = "Blank";
	private static final String DESC_ONEPAGENORM = "1 Page (Normal)";
	private static final String DESC_TWOPAGENORM = "2 Page (Normal)";
	private static final String SINGLESTROKE = "Single Stroke";
	private static final String DOUBLESTROKE = "Double Stroke";

	// fields
	private final int m_cmsid;		// cms ID
	private final Date m_date;		// message date and time
	private final CawsMsgType m_type;	// message type
	private final String m_multistring;	// message as multistring
	private final double m_ontime;

	// types
	public enum CawsMsgType { BLANK, ONEPAGEMSG, TWOPAGEMSG, TRAVELTIME }

	/**
	 * Create a new object using text line.
	 * @param line A single line from the CAWS message file. e.g.
	 *     "20080403085910;25;Blank;Single Stroke;Single Stroke;;;;;;;0.0;"
	 */
	public D10CmsMsg(String line) throws IllegalArgumentException {
		String[] f = (line + ' ').split(";");
		if(f.length != 13) {
			throw new IllegalArgumentException(
			    "Bogus CMS message format (" + line + ").");
		}

		m_date = convertDate(f[0]);
		m_cmsid = SString.stringToInt(f[1]);
		m_type = parseDescription(f[2]);
		int page_1_font = parseFont(f[3]);
		int page_2_font = parseFont(f[4]);

		String row1 = f[5].trim().toUpperCase();
		String row2 = f[6].trim().toUpperCase();
		String row3 = f[7].trim().toUpperCase();
		String row4 = f[8].trim().toUpperCase();
		String row5 = f[9].trim().toUpperCase();
		String row6 = f[10].trim().toUpperCase();

		// create message-pg1
		MultiString m = new MultiString();
		// FIXME: should use default font number, instead of 1 here
		if(page_1_font != 1)
			m.setFont(page_1_font);
		m.addText(row1);
		m.addLine();
		m.addText(row2);
		m.addLine();
		m.addText(row3);

		// page 2
		if(row4.length() + row5.length() + row6.length() > 0) {
			m.addPage();
			if(page_1_font != page_2_font)
				m.setFont(page_2_font);
			m.addText(row4);
			m.addLine();
			m.addText(row5);
			m.addLine();
			m.addText(row6);
		}
		m_multistring = m.toString();

		m_ontime = SString.stringToDouble(f[11]);

		if(m_type != CawsMsgType.BLANK) {
			System.err.println("D10CmsMsg.D10CmsMsg():" + m_date
				+ "," + m_cmsid + "," + m_multistring + ","
				+ m_ontime);
		}
	}

	/**
	 * Convert a local time date string from the d10 cms file to a Date.
	 * @param date String date/time in the format "20080403085910" which is
	 *             local time.
	 * @return A Date cooresponding to the argument.
	 * @throws IllegalArgumentException if the argument is bogus.
	 */
	private static Date convertDate(String argdate)
		throws IllegalArgumentException {

		// sanity check
		if(argdate.length() != 14) {
			throw new IllegalArgumentException(
			    "Bogus date string received: " + argdate);
		}

		// year (note the column range is: inclusive, exclusive)
		int y = SString.stringToInt(argdate.substring(0, 4));
		if(y < 2008) {
			throw new IllegalArgumentException(
			    "Bogus year received:" + argdate + "," + y);
		}

		// month (zero based)
		int m = SString.stringToInt(argdate.substring(4, 6)) - 1;
		if((m < 0) || (m > 11)) {
			throw new IllegalArgumentException(
			    "Bogus month received:" + argdate + "," + m);
		}

		// day
		int d = SString.stringToInt(argdate.substring(6, 8));
		if((d < 1) || (d > 31)) {
			throw new IllegalArgumentException(
			    "Bogus day received:" + argdate + "," + d);
		}

		// hour
		int h = SString.stringToInt(argdate.substring(8, 10));
		if((h < 0) || (h > 23)) {
			throw new IllegalArgumentException(
			    "Bogus hour received:" + argdate + "," + h);
		}

		// min
		int mi = SString.stringToInt(argdate.substring(10, 12));
		if((mi < 0) || (mi > 59)) {
			throw new IllegalArgumentException(
			    "Bogus minute received:" + argdate + "," + mi);
		}

		// sec
		int s = SString.stringToInt(argdate.substring(12, 14));
		if((s < 0) || (s > 59)) {
			throw new IllegalArgumentException(
			    "Bogus second received:" + argdate + "," + s);
		}

		// create Date
		Calendar c = new GregorianCalendar(y, m, d, h, mi, s);
		return c.getTime();
	}

	/**
	 * Parse a message description to a message type.
	 * @param d Message description.
	 * @return CawsMsgType enum value.
	 */
	static protected CawsMsgType parseDescription(String d) {
		if(d.equals(DESC_BLANK))
			return CawsMsgType.BLANK;
		else if(d.equals(DESC_ONEPAGENORM))
			return CawsMsgType.ONEPAGEMSG;
		else if(d.equals(DESC_TWOPAGENORM))
			return CawsMsgType.TWOPAGEMSG;
		else if(false)	//FIXME: add in the future
			return CawsMsgType.TRAVELTIME;
		else {
			// FIXME: should throw InvalidArgumentException
			String msg = "D10CmsMsg.parseDescription: WARNING: " +
				"unknown message description (" + d + ").";
			assert false: msg;
			System.err.println(msg);
			return CawsMsgType.BLANK;
		}
	}

	/**
	 * Parse a font.
	 * @param f Font name.
	 * @return Font number.
	 */
	static protected int parseFont(String f) {
		// FIXME: should lookup font number from name
		if(f.equals(SINGLESTROKE))
			return 1;
		else if(f.equals(DOUBLESTROKE))
			return 2;
		else {
			// FIXME: should throw InvalidArgumentException
			System.err.println("WARNING: unknown font received " +
				"in D10CmsMsg.parseFont(): " + f);
			// FIXME: should return default font number for DMS
			return 1;
		}
	}

	/**
	 * Activate the message. CAWS messages are activated only if the DMS is
	 * currently blank or contains a message owned by CAWS.
	 * @param dms Activate the message on this DMS.
	 */
	public void activate(DMSImpl dms) {
		System.err.println("-----D10CmsMsg.activate(" + dms +
			") called, msg=" + this);
		if(shouldSendMessage(dms))
			sendMessage(dms);
	}

	/**
	 * Decide if a caws message should be sent to a DMS.
	 * @params dms The associated DMS.
	 * @return true to send the message.
	 */
	protected boolean shouldSendMessage(DMSImpl dms) {
		if(dms == null)
			return false;

		// is caws activated for the sign?
		if(!(dms.getAwsAllowed() && dms.getAwsControlled())) {
			System.err.println("D10CmsMsg.shouldSendMessage(): DMS "
				+ getIrisCmsId() +
				" is NOT activated for CAWS control.");
			return false;
		}
		System.err.println("D10CmsMsg.shouldSendMessage(): DMS " +
			getIrisCmsId() + " is activated for CAWS control.");

		// be safe and send the caws message by default
		boolean send = true;

		// message already deployed?
		SignMessage cur = dms.getMessageCurrent();
		if(cur != null)
			send = !cur.getMulti().equals(m_multistring);

		System.err.println("D10CmsMsg.shouldSendMessage(): should send="
			+ send);
		return send;
	}

	/**
	 * Send a message to the specified DMS.
	 * @params dms The associated DMS.
	 */
	protected void sendMessage(DMSImpl dms) {
		switch(m_type) {
		case BLANK:
		case ONEPAGEMSG:
		case TWOPAGEMSG:
			System.err.println("D10CmsMsg.sendMessage(): will " +
				"activate DMS " + getIrisCmsId() + ":" + this);
			try {
				dms.sendMessage(toSignMessage(dms));
			}
			catch(TMSException e) {
				System.err.println("D10CmsMsg.sendMessage(): " +
					"exception:" + e);
			}
			break;
		case TRAVELTIME:
			//FIXME: add in future
			break;
		default:
			assert false:
				"D10CmsMsg.sendMessage(): unknown CawsMsgType";
		}
	}

	/**
	 * toSignMessage builds a SignMessage version of this message.
	 * @param dms The associated DMS.
	 * @return A SignMessage that contains the text of the message and
	 *         rendered bitmap(s).
	 */
	public SignMessage toSignMessage(DMSImpl dms) {
		return dms.createMessage(m_multistring, DMSMessagePriority.AWS);
	}

	/** toString */
	public String toString() {
		String s = "";
		s += "Message: " + m_multistring + ", ";
		s += "On time: " + m_ontime;
		return s;
	}

	/** get the CMS id, e.g. "39" */
	public int getCmsId() {
		return m_cmsid;
	}

	/** get the CMS id in IRIS form, e.g. "V39" */
	public String getIrisCmsId() {
		return "V" + getCmsId();
	}
}
