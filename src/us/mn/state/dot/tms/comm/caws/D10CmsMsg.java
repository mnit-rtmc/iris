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
import java.util.StringTokenizer;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.Log;
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
	private int m_cmsid;			// cms ID
	private Date m_date;			// message date and time
	private String m_desc;			// has predefined valid values
	private CawsMsgType m_type;		// message type
	private String m_multistring;		// message as multistring
	private boolean m_valid = false;	// is message valid?
	private double m_ontime;

	// types
	public enum CawsMsgType { BLANK, ONEPAGEMSG, TWOPAGEMSG, TRAVELTIME }

	/**
 	 * Parse a string that contains a single DMS message.
 	 * @param argline a single DMS message, fields delimited with ';'.
 	 */
	public void parse(String argline) {
		if(argline == null)
			argline = "";

		boolean ok = true;

		try {
			// add a space between successive delimiters. This is done so the
			// tokenizer doesn't skip over delimeters with nothing between them.
			// System.err.println("D10CmsMsg.D10CmsMsg() called, argline="+argline);
			String line = argline.replace(";;", "; ;");

			// System.err.println("D10CmsMsg.D10CmsMsg() called, line1="+line);
			line = line.replace(";;", "; ;");

			// System.err.println("D10CmsMsg.D10CmsMsg() called, line2="+line);
			// verify syntax
			StringTokenizer tok = new StringTokenizer(line, ";");

			// validity check, note that 12 or 13 tokens are expected
			int numtoks = tok.countTokens();
			final int EXPNUMTOKENS1 = 12;
			final int EXPNUMTOKENS2 = 13;
			if(numtoks != EXPNUMTOKENS1 && numtoks != EXPNUMTOKENS2) {
				throw new IllegalArgumentException(
					"Bogus CMS message format, numtoks was " + 
					numtoks + ", expected " + EXPNUMTOKENS1 + 
					" or " + EXPNUMTOKENS2 + " (" + argline + 
					").");
			}

			// #1, date: 20080403085910
			m_date = convertDate(tok.nextToken());

			// #2, id: 39
			m_cmsid = SString.stringToInt(tok.nextToken());

			// #3, message description
			String f02 = tok.nextToken();
			if(!f02.equals(DESC_BLANK) &&!f02.equals(
				DESC_ONEPAGENORM) &&!f02.equals(
				DESC_TWOPAGENORM)) {    // FIXME: verify possibilities
				String msg = "D10CmsMsg.parse(): unknown " +
					"message description received in " +
					"D10CmsMsg.parse(): " + f02;
				throw new IllegalArgumentException(msg);
			}
			m_desc = f02;

			// #4, pg 1 font
			String f03 = tok.nextToken();
			if(!f03.equals(SINGLESTROKE) &&!f03.equals(DOUBLESTROKE)) {
				String msg = "D10CmsMsg.parse(): unknown pg " +
					"1 font received: " + f03;
				throw new IllegalArgumentException(msg);
			}

			// #5, pg 2 font
			String f04 = tok.nextToken();
			if(!f04.equals(SINGLESTROKE) &&!f04.equals(DOUBLESTROKE)) {
				String msg = "D10CmsMsg.parse(): unknown pg " +
					"2 font received: " + f04;
				throw new IllegalArgumentException(msg);
			}

			// #6 - #11, rows of text
			{
				String row1 = tok.nextToken().trim().toUpperCase();
				String row2 = tok.nextToken().trim().toUpperCase();
				String row3 = tok.nextToken().trim().toUpperCase();
				String row4 = tok.nextToken().trim().toUpperCase();
				String row5 = tok.nextToken().trim().toUpperCase();
				String row6 = tok.nextToken().trim().toUpperCase();

				// create message-pg1
				StringBuilder m = new StringBuilder();

				m.append(row1);
				m.append("[nl]");
				m.append(row2);
				m.append("[nl]");
				m.append(row3);
				m.append("[nl]");

				// pg2
				if(row4.length() + row5.length() + row6.length() > 0) {
					m.append("[np]");
					m.append(row4);
					m.append("[nl]");
					m.append(row5);
					m.append("[nl]");
					m.append(row6);
				}

				m_multistring = m.toString();
			}

			// #12, on time: 0.0
			m_ontime = SString.stringToDouble(tok.nextToken());

			// #13, ignore this field, follows last semicolon if
			//      there are 13 tokens.

		} catch(Exception ex) {
			System.err.println("D10CmsMsg.parse(): unexpected " +
				"exception: " + ex);
			ok = false;
		}

		this.setValid(ok);
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

	/** set valid */
	private void setValid(boolean v) {
		m_valid = v;
	}

	/** return true if the DMS message is valid else false */
	public boolean getValid() {
		return (m_valid);
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
			Log.finest(msg);
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
			Log.warning("WARNING: unknown font received " +
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
		Log.finest("-----D10CmsMsg.activate(" + dms +
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
			Log.finest("D10CmsMsg.shouldSendMessage(): DMS "
				+ getIrisCmsId() +
				" is NOT activated for CAWS control.");
			return false;
		}
		Log.finest("D10CmsMsg.shouldSendMessage(): DMS " +
			getIrisCmsId() + " is activated for CAWS control.");

		// be safe and send the caws message by default
		boolean send = true;

		// message already deployed?
		SignMessage cur = dms.getMessageCurrent();
		if(cur != null)
			send = !cur.getMulti().equals(m_multistring);

		Log.finest("D10CmsMsg.shouldSendMessage(): should send="
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
			Log.finest("D10CmsMsg.sendMessage(): will " +
				"activate DMS " + getIrisCmsId() + ":" + this);
			try {
				dms.sendMessage(toSignMessage(dms));
			}
			catch(Exception e) {
				Log.warning("D10CmsMsg.sendMessage(): " +
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
	public SignMessage toSignMessage(DMSImpl dms) throws SonarException,
		TMSException
	{
		return dms.createMessage(m_multistring, DMSMessagePriority.AWS,
			null);
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
