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
package us.mn.state.dot.tms.comm.aws;

import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.SFile;
import us.mn.state.dot.tms.utils.Log;
import us.mn.state.dot.tms.utils.SString;
import us.mn.state.dot.tms.utils.STime;

/**
 * AWS message.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class AwsMsg {

	/** constants */
	private static final String DESC_BLANK = "Blank";
	private static final String DESC_ONEPAGENORM = "1 Page (Normal)";
	private static final String DESC_TWOPAGENORM = "2 Page (Normal)";
	private static final String SINGLESTROKE = "Single Stroke";
	private static final String DOUBLESTROKE = "Double Stroke";

	/** AWS message fields */
	private int m_dmsid;			// DMS ID
	private Date m_date;			// message date and time
	private String m_desc;			// has predefined valid values
	private AwsMsgType m_type;		// message type
	private String m_multistring;		// message as multistring
	private boolean m_valid = false;	// is message valid?
	private double m_ontime;

	/** AWS message type */
	public enum AwsMsgType { BLANK, ONEPAGEMSG, TWOPAGEMSG, 
		TRAVELTIME, UNKNOWN}

	/** Parse a string that contains a single DMS message.
 	 * @param argline a single DMS message, fields delimited with ';'. */
	public void parse(String argline) {
		if(argline == null)
			argline = "";

		boolean ok = true;

		try {
			// add a space between successive delimiters. This is 
			// done so the tokenizer doesn't skip over delimeters 
			// with nothing between them.
			String line = argline.replace(";;", "; ;");
			line = line.replace(";;", "; ;");

			// verify syntax
			StringTokenizer tok = new StringTokenizer(line, ";");

			// validity check, 12 or 13 tokens are expected
			int numtoks = tok.countTokens();
			final int EXPTOKS1 = 12;
			final int EXPTOKS2 = 13;
			if(numtoks != EXPTOKS1 && numtoks != EXPTOKS2) {
				throw new IllegalArgumentException(
					"Bogus DMS msg format, numtoks=" + 
					numtoks + ", expected " + EXPTOKS1 + 
					" or " + EXPTOKS2 + " (" + argline + 
					").");
			}

			// #1, date: 20080403085910
			m_date = convertDate(tok.nextToken());

			// #2, id: 39
			m_dmsid = SString.stringToInt(tok.nextToken());

			// #3, message description
			m_desc = tok.nextToken();
			m_type = parseDescription(m_desc);

			// #4, pg 1 font
			String f03 = tok.nextToken();
			if(!f03.equals(SINGLESTROKE) && 
				!f03.equals(DOUBLESTROKE)) 
			{
				String msg = "AwsMsg.parse(): unknown pg" +
					" 1 font received: " + f03;
				throw new IllegalArgumentException(msg);
			}

			// #5, pg 2 font
			String f04 = tok.nextToken();
			if(!f04.equals(SINGLESTROKE) && 
				!f04.equals(DOUBLESTROKE)) 
			{
				String msg = "AwsMsg.parse(): unknown pg" +
					" 2 font received: " + f04;
				throw new IllegalArgumentException(msg);
			}

			// #6 - #11, rows of text
			{
				final int numrows = 6;
				String[] row = new String[numrows];
				for(int i = 0; i < numrows; ++i)
					row[i] = new MultiString(
						tok.nextToken()).normalize();

				// write AWS report
				appendAwsReport(row);

				// create message-pg1
				StringBuilder m = new StringBuilder();

				m.append(row[0]);
				m.append("[nl]");
				m.append(row[1]);
				m.append("[nl]");
				m.append(row[2]);
				m.append("[nl]");

				// pg2
				if(row[3].length() + row[4].length() + 
					row[5].length() > 0) 
				{
					m.append("[np]");
					m.append(row[3]);
					m.append("[nl]");
					m.append(row[4]);
					m.append("[nl]");
					m.append(row[5]);
				}

				m_multistring = m.toString();
			}

			// #12, on time: 0.0
			m_ontime = SString.stringToDouble(tok.nextToken());

			// #13, ignore this field, follows last semicolon if
			//      there are 13 tokens.

			Log.finest("AwsMsg: Read AWS message " + 
				"from file: " + toString());

		} catch(Exception ex) {
			Log.severe("AwsMsg.parse(): unexpected " +
				"exception: " + ex + ", argline=" + argline +
				", stack trace=" + SString.getStackTrace(ex));
			ok = false;
		}

		this.setValid(ok);
	}

	/**
	 * Convert a local time date string from the d10 DMS file to a Date.
	 * @param date String date/time in the format "20080403085910" which 
	 *        is local time.
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
				"Bogus second received:" + 
				argdate + "," + s);
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
	 * @return AwsMsgType enum value.
	 */
	static protected AwsMsgType parseDescription(String d) {
		if(d.equalsIgnoreCase(DESC_BLANK))
			return AwsMsgType.BLANK;
		else if(d.equalsIgnoreCase(DESC_ONEPAGENORM))
			return AwsMsgType.ONEPAGEMSG;
		else if(d.equalsIgnoreCase(DESC_TWOPAGENORM))
			return AwsMsgType.TWOPAGEMSG;
		else if(false)
			return AwsMsgType.TRAVELTIME; // future
		else {
			Log.severe("AwsMsg.parseDescription: " +
				"unknown message description (" + d + ").");
			return AwsMsgType.UNKNOWN;
		}
	}

	/** Parse a font name 
	 * @param f Font name.
	 * @return Font number. */
	static protected int parseFont(String f) {
		// FIXME: should lookup font number from name
		if(f.equals(SINGLESTROKE))
			return 1;
		else if(f.equals(DOUBLESTROKE))
			return 2;
		else {
			// FIXME: should throw InvalidArgumentException
			Log.warning("WARNING: unknown font received " +
				"in AwsMsg.parseFont(): " + f);
			// FIXME: should return default font number for DMS
			return 1;
		}
	}

	/**
	 * Activate the AWS message only if the DMS is
	 * currently blank or contains a message owned by AWS.
	 * @param dms Activate the message on this DMS.
	 */
	public void activate(DMSImpl dms) {
		Log.finest("-----AwsMsg.activate(" + dms +
			") called, msg=" + this);
		if(shouldSendMessage(dms))
			sendMessage(dms);
	}

	/**
	 * Decide if aws message should be sent to a DMS.
	 * @params dms The associated DMS.
	 * @return true to send the message.
	 */
	protected boolean shouldSendMessage(DMSImpl dms) {
		if(dms == null)
			return false;

		// is aws activated for the sign?
		if(!(dms.getAwsAllowed() && dms.getAwsControlled())) {
			Log.finest("AwsMsg.shouldSendMessage(): DMS "
				+ getIrisDmsId() +
				" is NOT activated for AWS control.");
			return false;
		}
		Log.finest("AwsMsg.shouldSendMessage(): DMS " +
			getIrisDmsId() + " is activated for AWS control.");

		// be safe and send the aws message by default
		boolean send = true;

		// message already deployed?
		SignMessage cur = dms.getMessageCurrent();
		if(cur != null)
			send = !cur.getMulti().equals(m_multistring);

		Log.finest("AwsMsg.shouldSendMessage(): should send="
			+ send);
		return send;
	}

	/** Send a message to the specified DMS.
	 * @params dms The associated DMS. */
	protected void sendMessage(DMSImpl dms) {
		if(m_type == null)
			return;
		switch(m_type) {
		case BLANK:
		case ONEPAGEMSG:
		case TWOPAGEMSG:
			Log.finest("AwsMsg.sendMessage(): will activate" +
				"DMS " + getIrisDmsId() + ":" + this);
			try {
				dms.sendMessage(toSignMessage(dms));
			}
			catch(Exception e) {
				Log.warning("AwsMsg.sendMessage(): " +
					"exception:" + e);
			}
			break;
		case TRAVELTIME:
			//FIXME: add in future
			Log.severe("AwsMsg: AWS TT not available.");
			break;
		case UNKNOWN:
			Log.severe("AwsMsg.sendMessage(): unknown AWS " +
				"message type not sent."); 
			break;
		default:
			assert false;
			Log.severe("AwsMsg: unknown AwsMsgType.");
		}
	}

	/** Build a SignMessage version of this message.
	 * @param dms The associated DMS.
	 * @return A SignMessage that contains the text of the message and
	 *         rendered bitmap(s). */
	public SignMessage toSignMessage(DMSImpl dms) throws SonarException,
		TMSException
	{
		return dms.createMessage(m_multistring, DMSMessagePriority.AWS,
			null);
	}

	/** toString */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DMS ").append(m_dmsid).append(", ");
		sb.append("Message: ").append(m_multistring).append(", ");
		sb.append("On time: ").append(m_ontime);
		return sb.toString();
	}

	/** get the DMS id, e.g. "39" */
	public int getDmsId() {
		return m_dmsid;
	}

	/** get the DMS id in IRIS form, e.g. "V39" */
	public String getIrisDmsId() {
		return "V" + getDmsId();
	}

	/** append a line to the AWS report file */
	protected void appendAwsReport(String[] line) {
		if(line == null)
			return;

		// write report?
		if(!SystemAttrEnum.DMS_AWS_LOG_ENABLE.getBoolean())
			return;
		String fname = SystemAttrEnum.DMS_AWS_LOG_FILENAME.getString();
		if(fname == null || fname.length() <= 0) {
			Log.config("The AWS log file name is empty.");
			return;
		}
		int numrows = line.length;

		// anything to write?
		/*
		int totallinelen = 0;
		for(int i = 0; i < numrows; ++i)
			totallinelen += line[i].length();
		if(totallinelen <= 0)
			return;
		*/

		// build report line
		String rptline = "";

		// timestamp
		rptline += STime.getCurDateTimeString(true) + ", ";

		// dms id
		rptline += Integer.toString(m_dmsid) + ", ";

		// concatenate message lines
		String lines = "";
		for(int i = 0; i < numrows; ++i) {
			lines += line[i];
			if(i < numrows - 1)
				lines += " / ";
		}
		rptline += lines;

		// append line to file
		SFile.writeStringToFile(fname, rptline + "\n", true);
	}
}
