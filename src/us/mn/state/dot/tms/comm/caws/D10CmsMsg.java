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
import us.mn.state.dot.tms.TrafficDeviceAttributeHelper;
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
	private static final String DOUBLESTROKE = "Double Stroke";
	private static final String SINGLESTROKE = "Single Stroke";

	// fields
	private final int m_cmsid;	// cms ID
	private final Date m_date;	// message date and time
	private final String m_desc;	// this has predefined valid values
	private final String m_multistring; // message as multistring
	private final double m_ontime;

	// types
	public enum CawsMsgType { BLANK, ONEPAGEMSG, TWOPAGEMSG, TRAVELTIME }

	/**
	 * Create a new object using text line.
	 * @param line A single line from the CAWS message file. e.g.
	 *     "20080403085910;25;Blank;Single Stroke;Single Stroke;;;;;;;0.0;"
	 */
	public D10CmsMsg(String line) throws IllegalArgumentException {
		String[] f = (argline + ' ').split(";");
		if(f.length != 13) {
			throw new IllegalArgumentException(
			    "Bogus CMS message format (" + argline + ").");
		}

		m_date = convertDate(f[0]);
		m_cmsid = SString.stringToInt(f[1]);
		m_desc = parseDescription(f[2]);
		parseFont(f[3]);	// pg 1 font
		parseFont(f[4]);	// pg 2 font

		String row1 = f[5].trim().toUpperCase();
		String row2 = f[6].trim().toUpperCase();
		String row3 = f[7].trim().toUpperCase();
		String row4 = f[8].trim().toUpperCase();
		String row5 = f[9].trim().toUpperCase();
		String row6 = f[10].trim().toUpperCase();

		// create message-pg1
		MultiString m = new MultiString();
		m.addText(row1);
		m.addLine();
		m.addText(row2);
		m.addLine();
		m.addText(row3);

		// page 2
		if(row4.length() + row5.length() + row6.length() > 0) {
			m.addPage();
			m.addText(row4);
			m.addLine();
			m.addText(row5);
			m.addLine();
			m.addText(row6);
		}
		m_multistring = m.toString();

		// on time: 0.0
		m_ontime = SString.stringToDouble(f[11]);

		if(!m_desc.equals(DESC_BLANK)) {
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
	 * Parse a message description.
	 * @param d Message description.
	 * @return Message description.
	 */
	static protected String parseDescription(String d) {
		if(!d.equals(DESC_BLANK) &&
		   !d.equals(DESC_ONEPAGENORM) &&
		   !d.equals(DESC_TWOPAGENORM))
		{
			// FIXME: verify possibilities
			System.err.println("WARNING: unknown message " +
				"description received in " +
				"D10CmsMsg.parseDescription(): " + d);
		}
	}

	/**
	 * Parse a font.
	 * @param f Font name.
	 * @return Font name.
	 */
	static protected String parseFont(String f) {
		if(!f.equals(SINGLESTROKE) && !f.equals(DOUBLESTROKE)) {
			System.err.println("WARNING: unknown font " +
				"received in D10CmsMsg.parseFont(): " + f);
		}
	}

	/** get message type */
	public CawsMsgType getCawsMsgType() {
		CawsMsgType ret = CawsMsgType.BLANK;

		if(m_desc.equals(DESC_BLANK)) {
			ret = CawsMsgType.BLANK;
		} else if(m_desc.equals(DESC_ONEPAGENORM)) {
			ret = CawsMsgType.ONEPAGEMSG;
		} else if(m_desc.equals(DESC_TWOPAGENORM)) {
			ret = CawsMsgType.TWOPAGEMSG;
		} else if(false) {	//FIXME: add in the future
			ret = CawsMsgType.TRAVELTIME;
		} else {
			String msg = "D10CmsMsg.getCawsMsgType: Warning: " +
				"unknown D10 message description (" + m_desc +
				").";
			assert false: msg;
			System.err.println(msg);
		}

		return ret;
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
		if(!TrafficDeviceAttributeHelper.awsControlled(
			getIrisCmsId())) 
		{
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
		if(dms.getStatusCode() == DMS.STATUS_DEPLOYED) {
			SignMessage cur = dms.getMessage();
			if(cur != null)
				send = !cur.equals(m_multistring);
			System.err.println("D10CmsMsg.shouldSendMessage(): " +
				"DMS is deployed, m_multistring=" +
				m_multistring + ", cur msg=" + cur.toString());
		}

		System.err.println("D10CmsMsg.shouldSendMessage(): should send="
			+ send);
		return send;
	}

	/**
	 * Send a message to the specified DMS.
	 * @params dms The associated DMS.
	 */
	protected void sendMessage(DMSImpl dms) {
		switch(getCawsMsgType()) {
		case BLANK:
		case ONEPAGEMSG:
		case TWOPAGEMSG:
			// blank, 1 or 2 pg msg
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
			// travel time message
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
