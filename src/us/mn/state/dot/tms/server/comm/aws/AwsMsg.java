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
package us.mn.state.dot.tms.server.comm.aws;

import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsPgTime;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.IrisUserHelper;
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
import us.mn.state.dot.tms.utils.I18N;

/**
 * AWS message.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class AwsMsg {

	/** Assumed number of text rows in AWS message file */
	private static final int NUM_TEXT_ROWS = 6;

	/** Message type descriptions. A 'starts with' comparison is 
	 *  used for matching. */
	private static final String AWS_MDESC_BLANK = "Blank";
	private static final String AWS_MDESC_ONEPAGE = "1 Page";
	private static final String AWS_MDESC_TWOPAGE = "2 Page";

	/** Names of fonts in the AWS message file */
	private static final String AWS_SINGLESTROKE = "Single Stroke";
	private static final String AWS_DOUBLESTROKE = "Double Stroke";

	/** Expected names of IRIS fonts */
	private static final String IRIS_DOUBLESTROKE = "CT_Double_Stroke";

	/** AWS message fields */
	private int m_dmsid;			// DMS ID
	private Date m_date;			// message date and time
	private String m_desc;			// has predefined valid values
	private AwsMsgType m_type;		// message type
	private int m_fontnumpg1;		// IRIS font number
	private int m_fontnumpg2;		// IRIS font number
	private String[] m_textlines;		// lines of text
	private boolean m_valid = false;	// is message valid?
	private DmsPgTime m_pgontime;		// pg on-time

	/** AWS message types */
	public enum AwsMsgType {BLANK, ONEPAGEMSG, TWOPAGEMSG, UNKNOWN}

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
			m_fontnumpg1 = parseFont(tok.nextToken());

			// #5, pg 2 font
			m_fontnumpg2 = parseFont(tok.nextToken());

			// #6 - #11, six rows of text
			m_textlines = new String[NUM_TEXT_ROWS];
			for(int i = 0; i < NUM_TEXT_ROWS; ++i) {
				String msgline = tok.nextToken().trim();
				m_textlines[i] = new MultiString(
					msgline).normalize();
			}

			// write AWS report
			appendAwsReport(m_textlines);

			// #12, on time: 0.0
			m_pgontime = createPgOnTime(tok.nextToken());

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

	/** Return the page on-time. If zero is read, the system 
	 *  default is returned. */
	protected DmsPgTime createPgOnTime(String pont) {
		if(pont == null || pont.isEmpty() || 
			SString.stringToDouble(pont) <= 0)
		{
			return DmsPgTime.getDefaultOn();
		}
		return new DmsPgTime((double)SString.stringToDouble(pont));
	}

	/** Return a MULTI string representation of the AWS message. */
	protected String createMultiString() {
		if(NUM_TEXT_ROWS != 6)
			Log.severe("Bogus number of rows dependency " +
				"in createMultiString()");
		MultiString m = new MultiString();

		// pg 1
		m.setFont(m_fontnumpg1, null);
		m.setPageTimes(m_pgontime.toTenths(), null);
		m.addSpan(m_textlines[0]);
		m.addLine();
		m.addSpan(m_textlines[1]);
		m.addLine();
		m.addSpan(m_textlines[2]);
		m.addLine();

		// pg 2
		if(m_textlines[3].length() + m_textlines[4].length() + 
			m_textlines[5].length() > 0) 
		{
			m.addPage();
			m.setFont(m_fontnumpg2, null);
			m.addSpan(m_textlines[3]);
			m.addLine();
			m.addSpan(m_textlines[4]);
			m.addLine();
			m.addSpan(m_textlines[5]);
			m.addLine();
		}
		return m.normalize().toString();
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
		if(SString.startsWithIgnoreCase(d, AWS_MDESC_BLANK)) {
			return AwsMsgType.BLANK;
		} else if(SString.startsWithIgnoreCase(d, AWS_MDESC_ONEPAGE)) {
			return AwsMsgType.ONEPAGEMSG;
		} else if(SString.startsWithIgnoreCase(d, AWS_MDESC_TWOPAGE)) {
			return AwsMsgType.TWOPAGEMSG;
		} else {
			Log.severe("AwsMsg.parseDescription: unknown AWS " +
				"message description (" + d + ").");
			return AwsMsgType.UNKNOWN;
		}
	}

	/** Return the IRIS font number to use given an AWS font name.
	 * @param f AWS font name.
	 * @return IRIS font number. */
	static protected int parseFont(String f) {
		int ret;
		if(f.equals(AWS_SINGLESTROKE)) {
			// FIXME: use DMSHelper.getDefaultFontNumber
			ret = 1;
		} else if(f.equals(AWS_DOUBLESTROKE)) {
			Font dsf = FontHelper.lookup(IRIS_DOUBLESTROKE);
			if(dsf == null) {
				Log.severe("Double stroke font (" + 
					IRIS_DOUBLESTROKE + ") not found.");
				// FIXME: use DMSHelper.getDefaultFontNumber
				ret = 1;
			}
			else
				ret = dsf.getNumber();
		} else {
			Log.severe("Unknown AWS font name received (" + f +
				") in AwsMsg.parseFont().");
			// FIXME: use DMSHelper.getDefaultFontNumber
			ret = 1;
		}
		return ret;
	}

	/**
	 * Activate the AWS message only if the DMS is
	 * currently blank or contains a message owned by AWS.
	 * @param dms Activate the message on this DMS.
	 */
	public void activate(DMSImpl dms) {
		Log.finest("-----AwsMsg.activate(" + dms +
			") called, msg=" + this);
		if(shouldSendMessage(dms)) {
			sendMessage(dms);
//FIXME: the sleep below is a temporary fix for a problem that disconnects
//       clients if AWS messages are sent without the sleep between them.
			STime.sleep(1000);
		}
	}

	/**
	 * Decide if aws message should be sent to a DMS.
	 * @params dms The associated DMS.
	 * @return true to send the message.
	 */
	protected boolean shouldSendMessage(DMSImpl dms) {
		if(dms == null)
			return false;

		// not allowed?
		if(!dms.getAwsAllowed()) {
			Log.finest("AwsMsg.shouldSendMessage(): " + 
				getIrisDmsId() + " allowed = false");
			return false;
		}
		// not controlled?
		if(!dms.getAwsControlled()) {
			Log.finest("AwsMsg.shouldSendMessage(): " + 
				getIrisDmsId() + " controlled = false");
			return false;
		}
		Log.finest("AwsMsg.shouldSendMessage(): DMS " +
			getIrisDmsId() + " is AWS allowed & controlled.");

		boolean isDeployed = DMSHelper.isAwsMessageDeployed(dms);
		Log.finest("AwsMsg.shouldSendMessage(" + dms.getName() + 
			") isDeployed="+isDeployed);
		String newmulti = createMultiString();
		Log.finest("AwsMsg.shouldSendMessage(" + dms.getName() + 
			") newMulti="+newmulti);

		// DMS already blank and sending a blank?
		if(!isDeployed) {
			if(new MultiString(newmulti).isBlank()) {
				Log.finest("AwsMsg.shouldSendMessage(" + 
					dms.getName() + "): sign and new " +
					"multi both blank, send=false.");
				return false;
			}
		}

		// Be safe and send the aws message by default, unless:
		//	1) AWS message deployed, and
		//	2) priority is AWS, and
		//	3) MULTI strings are equal.
		boolean send = true;
		if(isDeployed)
			if(equalsCurrentSignMessage(dms, newmulti))
				send = false;

		Log.finest("AwsMsg.shouldSendMessage(): should send="
			+ send);
		return send;
	}

	/** Is the current sign message equal to the specified MULT? */
	protected static boolean equalsCurrentSignMessage(DMSImpl dms, 
		String multi) 
	{
		if(dms == null || multi == null)
			return false;
		SignMessage cur = dms.getMessageCurrent();
		if(cur != null) {
			// comparison of normalized MULTI strings
			String curm = cur.getMulti();
			boolean eq = new MultiString(curm).equals(
				new MultiString(multi));
			Log.finest("cur="+curm+", new="+multi+", equal="+eq);
			Log.finest("cur normalized=" +
				new MultiString(curm).normalize());
			Log.finest("new normalized=" +
				new MultiString(multi).normalize());
			return eq; 
		}
		return false;
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
				dms.doSetMessageNext(toSignMessage(dms), 
					getAwsUserName());
			}
			catch(Exception e) {
				Log.warning("AwsMsg.sendMessage(): " +
					"exception:" + e);
			}
			break;
		case UNKNOWN:
			// for safety, change the message type and send it
			Log.severe("AwsMsg.sendMessage(): unknown AWS " +
				"message type."); 
			m_type = AwsMsgType.ONEPAGEMSG;
			sendMessage(dms);
			break;
		default:
			assert false;
			Log.severe("AwsMsg: unknown AwsMsgType.");
		}
	}

	/** Get the AWS user, which is assumed to be a user with the
	 *  same name as the I18N AWS abbrievation, else "AWS". */
	protected static User getAwsUserName() {
		User u = null;

		// check user named the same as the I18N AWS abbreviation
		u = IrisUserHelper.lookup(I18N.getSilent(
			"dms.aws.abbreviation"));

		// check for user AWS
		if(u == null)
			u = IrisUserHelper.lookup("AWS");

		return u;
	}

	/** Return the SignMessage duration. A null indicates an
	 *  indefinite duration.
	 *  @see DmsLitePoller.sendMessage() */
	protected Integer getSignMessageDuration() {
		if(m_type == AwsMsgType.BLANK)
			return 0;
		// all dmslite messages have an indefinite duration
		return null;
	}

	/** Build a SignMessage version of this message.
	 * @param dms The associated DMS.
	 * @return A SignMessage that contains the text of the message and
	 *         rendered bitmap(s). */
	public SignMessage toSignMessage(DMSImpl dms) throws SonarException,
		TMSException
	{
		String multi = createMultiString();
		return dms.createMessage(multi, DMSMessagePriority.AWS,
			DMSMessagePriority.AWS, getSignMessageDuration());
	}

	/** toString */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DMS ").append(m_dmsid).append(", ");
		sb.append("MultiString: ").append(
			createMultiString()).append(", ");
		sb.append("FontNum pg1: ").append(m_fontnumpg1).append(", ");
		sb.append("FontNum pg2: ").append(m_fontnumpg2).append(", ");
		sb.append("Pg On-time: ").append(m_pgontime.toString());
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
		if(false) {
			int totallinelen = 0;
			for(int i = 0; i < numrows; ++i)
				totallinelen += line[i].length();
			if(totallinelen <= 0)
				return;
		}

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
