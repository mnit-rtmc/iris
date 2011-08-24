/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2011  Minnesota Department of Transportation
 * Copyright (C) 2008-2010  AHMCT, University of California
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
package us.mn.state.dot.tms.server.comm.dmsxml;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DmsPgTime;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.utils.Log;
import us.mn.state.dot.tms.utils.STime;

/**
 * Operation to send a new message to a DMS.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
class OpMessage extends OpDms {

	/** Sign message */
	private final SignMessage m_sm;

	/** Number of pages in message */
	private final int m_npages;

	/** Create a new DMS command message object */
	public OpMessage(DMSImpl d, SignMessage m, User u) {
		super(PriorityLevel.COMMAND, d, "Sending new message", u);
		m_sm = m;
		m_npages = calcNumPages();
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
		byte[] bitmaps = SignMessageHelper.decodeBitmaps(m_sm);
		if(bitmaps == null)
			return "";
		BitmapGraphic oldbmg = DMSHelper.createBitmapGraphic(m_dms);
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

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		m_dms.setMessageNext(m_sm);

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
		byte[] bitmaps = SignMessageHelper.decodeBitmaps(m_sm);
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
		if(m_npages <= 0)
			return null;
		return new PhaseSendMessage();
	}

	/** Get the length of one bitmap page */
	protected int getPageLength() {
		BitmapGraphic b = DMSHelper.createBitmapGraphic(m_dms);
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
	 *	<dmsxml><elemname>
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
	 *	</elemname></dmsxml>
	 */
	private XmlElem buildReqRes(String elemReqName, String elemResName) {
		XmlElem xrr = new XmlElem(elemReqName, elemResName);

		// id
		xrr.addReq("Id", generateId());

		// address, etc.
		xrr.addReq("Address", controller.getDrop());

		// MsgText
		xrr.addReq("MsgText", m_sm.getMulti());

		// UseOnTime, always true
		xrr.addReq("UseOnTime", true);

		// OnTime
		Calendar ontime = calcMsgOnTime();
		xrr.addReq("OnTime", 
			STime.CalendarToXML(ontime));

		// UseOffTime
		boolean useofftime = (m_sm.getDuration() != null);
		xrr.addReq("UseOffTime", useofftime);

		// OffTime, only used if duration is not infinite
		String offtime= (useofftime ?
			STime.CalendarToXML(calcMsgOffTime(ontime)) : "");
		xrr.addReq("OffTime", offtime);

		// DisplayTimeMS: extract from 1st page of MULTI
		DmsPgTime pt = determinePageOnTime(m_sm.getMulti());
		xrr.addReq("DisplayTimeMS", pt.toMs());

		// activation priority
		xrr.addReq("ActPriority", 
			m_sm.getActivationPriority());

		// runtime priority
		xrr.addReq("RunPriority", 
			m_sm.getRunTimePriority());

		// Owner
		xrr.addReq("Owner", 
			m_user != null ? m_user.getName() : "");

		// bitmap
		xrr.addReq("Bitmap", getBitmapPage());

		// response
		xrr.addRes("Id");
		xrr.addRes("IsValid");
		xrr.addRes("ErrMsg");

		return xrr;
	}

	/** Parse response.
	 *  @return True to retry the operation else false if done. */
	private boolean parseResponse(Message mess, XmlElem xrr) {

		long id = 0;
		boolean valid = false;
		String errmsg = "";

		try {
			// id
			id = xrr.getResLong("Id");

			// isvalid
			valid = xrr.getResBoolean("IsValid");

			// error message text
			errmsg = xrr.getResString("ErrMsg");
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
			setErrorStatus("");
			m_dms.setMessageCurrent(m_sm, m_user);
		} else {
			Log.finest("OpMessage.parseResponse(): response " +
				"from SensorServer received, ignored " +
				"because Xml valid field is false, " +
				"errmsg=" + errmsg + ", id=" + id);
			setErrorStatus(errmsg);

			// try again
			if(flagFailureShouldRetry(errmsg)) {
				Log.finest("OpMessage: will retry " +
					"failed operation.");
				return true;

			// give up
			} else {
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
		return (m_npages <= 1 ? 
			"SetSnglPgReqMsg" : "SetMultPgReqMsg");
	}

	/** Return Xml request element name */
	private String getXmlResName() {
		return (m_npages <= 1 ? 
			"SetSnglPgRespMsg" : "SetMultPgRespMsg");
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
		 * Set the status to modify request. Called by 
		 * Operation.poll().
		 * @param argmess
		 * @return next Phase to execute else null.
		 * @throws IOException
		 */
		protected Phase poll(CommMessage argmess)
			throws IOException 
		{
			updateInterStatus("Starting operation", false);
			Log.finest("PhaseSendMessage.poll(msg) called.");

			Message mess = (Message) argmess;

			// set message attributes as a function of the op
			setMsgAttributes(mess);

			// build XML element with children
			mess.setName(getOpName());
			XmlElem xrr = buildReqRes(getXmlReqName(), 
				getXmlResName());

			// send request and read response
			mess.add(xrr);
			sendRead(mess);

			if(parseResponse(mess, xrr))
				return this;
			return null;
		}
	}

	/** Cleanup the operation. Called by MessagePoller.doPoll(). */
	public void cleanup() {
		m_dms.setMessageNext(null);
		super.cleanup();
	}
}
