/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
 * Copyright (C) 2008-2010 AHMCT, University of California, Davis
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
	private static final String IRIS_SINGLESTROKE = "CT_Single_Stroke";
	private static final String IRIS_DOUBLESTROKE = "CT_Double_Stroke";

	/** AWS message fields */
	private int m_dmsid;			// DMS ID
	private Date m_date;			// message date and time
	private String m_desc;			// has predefined valid values
	private AwsMsgType m_type;		// message type
	private int m_fontnumpg1;		// IRIS font number
	private int m_fontnumpg2;		// IRIS font number
	private String[] m_textlines = 
		new String[NUM_TEXT_ROWS];	// lines of text
	private boolean m_valid = false;	// is message valid?
	private DmsPgTime m_pgontime =
		new DmsPgTime(0);		// pg on-time

	/** AWS message types */
	public enum AwsMsgType {BLANK, ONEPAGEMSG, TWOPAGEMSG, UNKNOWN}

	/** Constructor */
	public AwsMsg() {
		for(int i = 0; i < m_textlines.length; ++i)
			m_textlines[i] = "";
	}

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
			m_pgontime = createPgOnTime(tok.nextToken(), m_type);

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

	/** Return the page on-time. If a bogus value is read the system
	 *  default is returned as a funtion of the number of pages. Zero
	 *  is always returned for a single page message because flashing
	 *  is not supported as of yet. The page on-time for multipage 
	 *  messages is validated.
	 *  @param pont Page on-time in seconds, e.g. "2.5"
	 *  @param mtype AWS message type.
	 *  @return the validated page on-time. */
	private static DmsPgTime createPgOnTime(String pont, AwsMsgType mtype) {
		DmsPgTime ret;
		if(pont == null || pont.isEmpty()) {
			ret = DmsPgTime.getDefaultOn(true);
		} else if(mtype == mtype.BLANK) {
			ret = DmsPgTime.getDefaultOn(true);
		} else if(mtype == mtype.ONEPAGEMSG) {
			// single page messages can not have a non-zero 
			// page on-time (flashing).
			ret = DmsPgTime.getDefaultOn(true);
		} else if(mtype == mtype.TWOPAGEMSG) {
			double s = SString.stringToDouble(pont);
			// if zero is specified, use system default
			s = (s <= 0 ? 
				DmsPgTime.getDefaultOn(false).toSecs() : s);
			DmsPgTime pt = new DmsPgTime(s);
			ret = DmsPgTime.validateOnTime(pt, false);
		} else {
			Log.severe("AwsMsg.createPgOnTime(): bogus mtype.");
			ret = DmsPgTime.getDefaultOn(true);
		}
		return ret;
	}

	/** Return true if the 1st page contains text. */
	private boolean textPage1() {
		return (m_textlines[0].length() + 
			m_textlines[1].length() + 
			m_textlines[2].length()) > 0;
	}

	/** Return true if the 2nd page contains text. */
	private boolean textPage2() {
		return (m_textlines[3].length() + 
			m_textlines[4].length() + 
			m_textlines[5].length()) > 0;
	}

	/** Return a MULTI string representation of the AWS message. 
	 *  Pages may be rendered with the double stroke font, in which
	 *  case the maximum lines per page is 2. If more than 2 lines
	 *  per page are placed in the MULTI string with the DS font, 
	 *  the bitmap renders blank. This will only happen if the AWS
	 *  message file contains an error--3 lines per page w/ DS font. */
	protected MultiString toMultiString() {
		if(NUM_TEXT_ROWS != 6)
			Log.severe("Bogus number of rows dependency " +
				"in toMultiString()");
		MultiString m = new MultiString();
		boolean multipage = textPage2();
		// pg 1
		if(textPage1())
			m.setFont(m_fontnumpg1, null);
		if(multipage)
			m.setPageTimes(m_pgontime.toTenths(), null);
		addSpanLine(m_textlines[0], m, false);
		addSpanLine(m_textlines[1], m, false);
		addSpanLine(m_textlines[2], m, true);
		// pg 2
		if(multipage) {
			m.addPage();
			m.setFont(m_fontnumpg2, null);
			addSpanLine(m_textlines[3], m, false);
			addSpanLine(m_textlines[4], m, false);
			addSpanLine(m_textlines[5], m, true);
		}
		return m;
	}

	/** Add a span of text to the specified multistring. */
	private void addSpanLine(String span, MultiString ms, 
		boolean ignoreblankspan) 
	{
		if(ignoreblankspan) {
			if(!span.trim().isEmpty()) {
				ms.addSpan(span);
				ms.addLine();
			}
		} else {
			ms.addSpan(span);
			ms.addLine();
		}	
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

	/** Return the DMS or null if it doesn't exist. */
	private DMS getDms() {
		String dmsname = "V" + Integer.toString(m_dmsid);
		return DMSHelper.lookup(dmsname);
	}

	/** Return IRIS font name given the AWS font name. */
	private static String getIrisFontName(String awsFontName) {
		if(awsFontName.equals(AWS_SINGLESTROKE))
			return IRIS_SINGLESTROKE;
		else if(awsFontName.equals(AWS_DOUBLESTROKE))
			return IRIS_DOUBLESTROKE;
		else {
			Log.severe("Unknown AWS font name (" + awsFontName +
				") in AwsMsg.getIrisFontName().");
			return IRIS_SINGLESTROKE;
		}
	}

	/** Get the default DMS font number. */
	private int getDmsDefaultFontNum() {
		DMS dms = getDms();
		if(dms == null)
			return FontHelper.DEFAULT_FONT_NUM;
		else
			return DMSHelper.getDefaultFontNumber(dms);
	}

	/** Return the IRIS font number to use given an AWS font name.
	 * @param f AWS font name.
	 * @return IRIS font number. */
	private int parseFont(String f) {
		String irisfname = getIrisFontName(f);
		Font ifont = FontHelper.lookup(irisfname);
		int retfnum = FontHelper.DEFAULT_FONT_NUM;
		if(ifont == null) {
			Log.severe("Font doesn't exist (" + irisfname + ").");
			retfnum = getDmsDefaultFontNum();
		} else {
			retfnum = ifont.getNumber();
		}
		return retfnum;
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


	/** Decide if the AWS message should be sent to a DMS.
	 * @params dms The associated DMS.
	 * @return true to send the message. */
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

		// create multi to send
		MultiString newmulti = toMultiString();
		Log.finest("AwsMsg.shouldSendMessage(" + dms.getName() + 
			") newmulti="+newmulti);

		// true if sign has AWS message, regardless of error state
		boolean awsdeployed = DMSHelper.isAwsDeployed(dms);

		// sending a blank?
		if(newmulti.isBlank()) {
			// send if existing msg is non-blank AWS message, 
			// regardless of if the DMS is in an error state.
			boolean sendblank = awsdeployed;
			Log.finest("AwsMsg.shouldSendMessage(" + 
				dms.getName() + ") send blank=" + sendblank);
			return sendblank;
		}

		// send msg unless existing is an AWS msg and identical
		Log.finest("AwsMsg.shouldSendMessage(" + dms.getName() + 
			"): an existing AWS msg is deployed=" + awsdeployed);
		if(awsdeployed) {
			// send msg only if different from existing AWS msg
			boolean eq = equalsCurrentSignMessage(dms, newmulti);
			Log.finest("AwsMsg.shouldSendMessage(" + dms.getName()
				+ "): new msg is identical to exist=" + eq);
			if(eq) {
				Log.finest("AwsMsg.shouldSendMessage(" + 
					dms.getName() + "): will not send " +
					"new message because existing is " +
					"AWS and identical.");
				return false;
			}
		}
		Log.finest("AwsMsg.shouldSendMessage(" + dms.getName() + 
			"): will send new non-blank message.");
		return true;
	}

	/** Is the current sign message equal to the specified MULT? */
	protected static boolean equalsCurrentSignMessage(DMSImpl dms, 
		MultiString multi) 
	{
		if(dms == null || multi == null)
			return false;
		SignMessage cur = dms.getMessageCurrent();
		if(cur == null)
			return false;
		// comparison of tags and text in MULTI strings
		boolean eq = multi.equals(cur.getMulti());
		Log.finest("AwsMsg.equalsCurrentsignMessage(): cur=" + 
			cur.getMulti() + ", new=" + multi + ", equal=" + eq);
		return eq; 
	}

	/** Send a message to the specified DMS.
	 * @params dms The associated DMS. */
	protected void sendMessage(DMSImpl dms) {
		if(m_type == null) {
			assert false;
			Log.severe("sendMessage(): arg is null.");
		} else if(m_type == AwsMsgType.BLANK || 
			m_type == AwsMsgType.ONEPAGEMSG || 
			m_type == AwsMsgType.TWOPAGEMSG ) 
		{
			Log.finest("AwsMsg.sendMessage(" + dms.getName() + 
				"): will activate:" + this + ", user=" + 
				getAwsUserName());
			try {
				dms.doSetMessageNext(toSignMessage(dms), 
					getAwsUserName());
			} catch(Exception e) {
				Log.warning("AwsMsg.sendMessage(): " +
					"exception:" + e);
			}
		} else if( m_type == AwsMsgType.UNKNOWN) {
			// for safety, change the message type and send it
			Log.severe("AwsMsg.sendMessage(): unknown AWS " +
				"message type."); 
			m_type = AwsMsgType.ONEPAGEMSG;
			sendMessage(dms);
		} else {
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
		MultiString multi = toMultiString();
		DMSMessagePriority runp = (multi.isBlank() ? 
			DMSMessagePriority.BLANK : DMSMessagePriority.AWS);
		DMSMessagePriority actp = DMSMessagePriority.AWS;
		Log.finest("AwsMsg.toSignMessage(): creating sign message " +
			"with actp=" + actp + ", runp = " + runp + 
			", multi=" + multi);
		SignMessage sm = dms.createMessage(multi.toString(), actp, 
			runp, getSignMessageDuration());
		return sm;
	}

	/** toString */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DMS ").append(m_dmsid).append(", ");
		sb.append("MultiString: ").append(
			toMultiString().toString()).append(", ");
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
