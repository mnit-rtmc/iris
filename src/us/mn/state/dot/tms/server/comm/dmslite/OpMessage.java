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
package us.mn.state.dot.tms.server.comm.dmslite;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DmsPgTime;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.utils.Log;
import us.mn.state.dot.tms.utils.STime;

/**
 * Operation to send a new message to a DMS.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class OpMessage extends OpDms {

	/** Maximum message priority */
	static protected final int MAX_MESSAGE_PRIORITY = 255;

	/** Flag to avoid phase loops */
	protected boolean modify = true;

	/** Sign message */
	protected final SignMessage m_sm;

	/** Number of pages in message */
	private int m_npages;

	/** Create a new DMS command message object */
	public OpMessage(DMSImpl d, SignMessage m, User u) {
		super(COMMAND, d, "Sending new message", u);
		m_sm = m;
	}

	/** Return the bitmap page as a hex string for all pages. */
	public String getBitmapPage() {
		StringBuilder p = new StringBuilder();
		for(int i = 0; i < m_npages; ++i)
			p.append(getBitmapPage(i));
		return p.toString(); 
	}

	/** 
	 * Return the bitmap page as a hex string. The width of the 
	 * bitmap is adjusted as necessary.
	 */
	public String getBitmapPage(int pg) {
		if(m_sm == null)
			return "";
		byte[] bitmaps = getBitmaps();
		if(bitmaps == null)
			return "";
		BitmapGraphic oldbmg = createBitmap();
		if(oldbmg == null)
			return "";
		int blen = oldbmg.length();
		if(bitmaps.length % blen != 0)
			return "";
		int pages = bitmaps.length / blen;
		if(pg < 0 || pg >= pages)
			return "";
		byte[] pix = new byte[blen];
		System.arraycopy(bitmaps, pg * blen, pix, 0, blen);
		oldbmg.setPixels(pix);
		BitmapGraphic newbmg = new BitmapGraphic(BM_WIDTH, BM_HEIGHT);
		newbmg.copy(oldbmg);
		return new HexString(newbmg.getPixels()).toString();
	}

	/** Get the sign message bitmaps */
	protected byte[] getBitmaps() {
		try {
			return Base64.decode(m_sm.getBitmaps());
		}
		catch(IOException e) {
			return null;
		}
	}

	/** Create a bitmap matching the sign dimensions */
	protected BitmapGraphic createBitmap() {
		Integer w = m_dms.getWidthPixels();
		if(w == null || w < 1)
			return null;
		Integer h = m_dms.getHeightPixels();
		if(h == null || h < 1)
			return null;
		return new BitmapGraphic(w, h);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		// dms is configured
		if(dmsConfigured())
			return createPhaseTwo();

		// dms not configured
		Phase phase2 = createPhaseTwo();
		Phase phase1 = new PhaseGetConfig(phase2);
		return phase1;
	}

	/** Determine if single page message is flashing or not. */
	public static boolean isFlashing(String multi) {
		int[] pont = new MultiString(multi).getPageOnTimes(0);
		boolean flashing = false;
		if(pont != null && pont.length == 1)
			flashing = (pont[0] != 0);
		return flashing;
	}

	/** Calculate the number of pages.
	 *  @return If a fatal error occurred 0, else the number of pages. */
	private int calcNumPages() {
		byte[] bitmaps = getBitmaps();
		if(bitmaps == null)
			return 0;
		int blen = getPageLength();
		if(blen == 0 || bitmaps.length % blen != 0)
			return 0;
		int np = bitmaps.length / blen;
		if(np <= 0)
			return 0;
		return np;
	}

	/** create 2nd phase */
	private Phase createPhaseTwo() {
		m_npages = calcNumPages();
		if(m_npages <= 0)
			return null;
		return new PhaseSendMessage();
	}

	/** Get the length of one bitmap page */
	protected int getPageLength() {
		BitmapGraphic b = createBitmap();
		if(b != null)
			return b.length();
		else
			return 0;
	}

	/** 
	  * Calculate message on time, which is the time now. In the future,
	  * if IRIS supports specifying a start time, this may be calculated 
	  * to be some future time.
	  *
	  * @return On time
	  */
	protected Calendar calcMsgOnTime() {
		return(new GregorianCalendar());
	}

	/** Calculate message off time, which is the start time + duration.
	 *  This method should not be called if duration is infinite. */
	protected Calendar calcMsgOffTime(Calendar ontime) {
		Integer mins = m_sm.getDuration();
		if(mins == null)
			return null;
		Calendar offtime = (Calendar)ontime.clone();
		offtime.add(Calendar.MINUTE, mins);
		return offtime;
	}

	/** Return a validated MULTI string for a non-flashing single page 
	 *  message, which by definition must have a page on-time of zero. */
	protected String validateMultiOnePageMessage(String ms) {
		String ret = MultiString.replacePageOnTime(ms, 0);
		return ret;
	}

	/** Build request message in this format:
	 *	<DmsLite><elemname>
	 *		<Id>...</Id>
	 *		<Address>...</Address>
	 *		<MsgText>...</MsgText>
	 *		<UseOnTime>...</UseOnTime>
	 *		<OnTime>...</OnTime>
	 *		<UseOffTime>...</UseOffTime>
 	 *		<OffTime>...</OffTime>
	 *		<DisplayTimeMS>...</DisplayTimeMS>
	 *		<ActPriority>...</ActPriority>
	 *		<RunPriority>...</RunPriority>
	 *		<Owner>...</Owner>
	 *		<Msg>...</Msg>
	 *	</elemname></DmsLite>
	 */
	private XmlReqRes buildReqRes(String elemReqName, String elemResName) {
		XmlReqRes xrr = new XmlReqRes(elemReqName, elemResName);

		// id
		xrr.add(new ReqRes("Id", generateId(), 
			new String[] {"Id"}));

		// address, etc.
		xrr.add(new ReqRes("Address", controller.getDrop(),
			new String[] { "IsValid", "ErrMsg" }));

		// MsgText
		xrr.add(new ReqRes("MsgText", m_sm.getMulti()));

		// UseOnTime, always true
		xrr.add(new ReqRes("UseOnTime", true));

		// OnTime
		Calendar ontime = calcMsgOnTime();
		xrr.add(new ReqRes("OnTime", 
			STime.CalendarToXML(ontime)));

		// UseOffTime
		boolean useofftime = (m_sm.getDuration() != null);
		xrr.add(new ReqRes("UseOffTime", useofftime));

		// OffTime, only used if duration is not infinite
		String offtime= (useofftime ?
			STime.CalendarToXML(calcMsgOffTime(ontime)) :
			"");
		xrr.add(new ReqRes("OffTime", offtime));

		// DisplayTimeMS: extract from 1st page of MULTI
		DmsPgTime pt = determinePageOnTime(m_sm.getMulti());
		xrr.add(new ReqRes("DisplayTimeMS", pt.toMs()));

		// activation priority
		xrr.add(new ReqRes("ActPriority", 
			m_sm.getActivationPriority(), new String[0]));

		// runtime priority
		xrr.add(new ReqRes("RunPriority", 
			m_sm.getRunTimePriority(), new String[0]));

		// Owner
		xrr.add(new ReqRes("Owner", 
			m_user != null ? m_user.getName() : ""));

		// bitmap
		xrr.add(new ReqRes("Bitmap", getBitmapPage()));

		return xrr;
	}

	/** Parse response.
	 *  @return True to retry the operation else false if done. */
	private boolean parseResponse(Message mess, XmlReqRes xrr) {

		long id = 0;
		boolean valid = false;
		String errmsg = "";

		try {
			// id
			id = new Long(xrr.getResValue("Id"));

			// isvalid
			valid = new Boolean(xrr.getResValue("IsValid"));

			// error message text
			errmsg = xrr.getResValue("ErrMsg");
			if(!valid && errmsg.length() <= 0)
				errmsg = FAILURE_UNKNOWN;

			Log.finest("OpMessage.parseResponse(): parsed msg " +
				"values: IsValid:" + valid + ".");
		} catch (IllegalArgumentException ex) {
			Log.severe("OpMessage.parseResponse(): Malformed " +
				"XML received:" + ex+", id=" + id);
			valid = false;
			errmsg = ex.getMessage();
			handleCommError(EventType.PARSING_ERROR, errmsg);
		}

		// update 
		complete(mess);

		// parse rest of response
		if(valid) {
			setErrorMsg("");
			m_dms.setMessageCurrent(m_sm, m_user);
		} else {
			Log.finest("OpMessage.parseResponse(): response " +
				"from SensorServer received, ignored " +
				"because Xml valid field is false, " +
				"errmsg=" + errmsg + ", id=" + id);
			setErrorMsg(errmsg);

			// try again
			if(flagFailureShouldRetry(errmsg)) {
				Log.finest("OpMessage: will retry " +
					"failed operation.");
				return true;

			// give up
			} else {
				// if AWS failure, handle it
				if(mess.checkAwsFailure()) {
					mess.handleAwsFailure(
						"was sending a message.");
				}
			}
		}

		// done
		return false;
	}

	/** Return Xml request element name */
	private String getXmlReqName() {
		return (m_npages <= 1 ? "SetSnglPgReqMsg" : "SetMultPgReqMsg");
	}

	/** Return Xml request element name */
	private String getXmlResName() {
		return (m_npages <= 1 ? "SetSnglPgRespMsg" : "SetMultPgRespMsg");
	}

	/**
	 * Phase to send a the message. The type of exception thrown 
	 * here determines if the messenger reopens the connection on 
	 * failure.
	 *
	 * @see MessagePoller#doPoll()
	 * @see Messenger#handleCommError()
	 * @see Messenger#shouldReopen()
	 */
	protected class PhaseSendMessage extends Phase {

		/**
		 * Set the status to modify request. Called by Operation.poll().
		 * @param argmess
		 * @return next Phase to execute else null.
		 * @throws IOException
		 */
		protected Phase poll(AddressedMessage argmess)
			throws IOException 
		{
			updateInterStatus("Starting operation", false);
			Log.finest(
			    "PhaseSendMessage.poll(msg) called.");

			Message mess = (Message) argmess;

			// set message attributes as a function of the operation
			setMsgAttributes(mess);

			// build request response
			mess.setName(getOpName());
			XmlReqRes xrr = buildReqRes(getXmlReqName(), 
				getXmlResName());

			// send request and read response
			mess.add(xrr);
			sendRead(mess);

			if(parseResponse(mess, xrr))
				return this;
			return null;
		}
	}
}
