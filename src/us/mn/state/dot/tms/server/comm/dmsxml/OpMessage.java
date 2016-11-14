/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
 * Copyright (C) 2008-2014  AHMCT, University of California
 * Copyright (C) 2012 Iteris Inc.
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
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import static us.mn.state.dot.tms.server.comm.dmsxml.DmsXmlPoller.LOG;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.MILLISECONDS;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Operation to send a new message to a DMS.
 *
 * @author Michael Darter
 * @author Douglas Lau
 * @author Travis Swanston
 */
class OpMessage extends OpDms {

	/** Sign message */
	private final SignMessage m_sm;

	/** Number of pages in message */
	private final int m_npages;

	/** Bitmaps for all pages as a hex string */
	private final String m_bitmaps;

	/** Create a new DMS command message object */
	public OpMessage(DMSImpl d, SignMessage m, User u) {
		super(PriorityLevel.COMMAND, d, "Sending new message", u);
		m_sm = m;
		BitmapGraphic[] bitmaps = SignMessageHelper.getBitmaps(m, d);
		m_npages = bitmaps != null ? bitmaps.length : 0;
		m_bitmaps = convertToHexString(bitmaps);
	}

	/** Operation equality test */
	public boolean equals(Object o) {
		if(o instanceof OpMessage) {
			OpMessage op = (OpMessage)o;
			return m_dms == op.m_dms &&
			       SignMessageHelper.isEquivalent(m_sm, op.m_sm);
		} else
			return false;
	}

	/** Return the bitmap page as a hex string for all pages. */
	private String convertToHexString(BitmapGraphic[] bitmaps) {
		StringBuilder hs = new StringBuilder();
		if(bitmaps != null) {
			for(BitmapGraphic bg: bitmaps)
				hs.append(getBitmapPage(bg));
		}
		return hs.toString(); 
	}

	/** 
	 * Return the bitmap page as a hex string. The width of the 
	 * bitmap is adjusted as necessary.
	 */
	private String getBitmapPage(BitmapGraphic bg) {
		BitmapGraphic b = new BitmapGraphic(BM_WIDTH, BM_HEIGHT);
		b.copy(bg);
		return HexString.format(b.getPixelData());
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
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
		Interval zero = new Interval(0);
		Interval[] pont = new MultiString(multi).pageOnIntervals(zero);
		assert pont != null;
		return pont.length == 1 && !pont[0].equals(zero);
	}

	/** create 2nd phase */
	private Phase createPhaseTwo() {
		if(m_npages <= 0)
			return null;
		return new PhaseSendMessage();
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
		xrr.addReq("MsgText", new MultiString(m_sm.getMulti())
			.normalize());

		// UseOnTime, always true
		xrr.addReq("UseOnTime", true);

		// OnTime
		Calendar ontime = calcMsgOnTime();
		xrr.addReq("OnTime", STime.CalendarToXML(ontime));

		// UseOffTime
		boolean useofftime = (m_sm.getDuration() != null);
		xrr.addReq("UseOffTime", useofftime);

		// OffTime, only used if duration is not infinite
		String offtime= (useofftime ?
			STime.CalendarToXML(calcMsgOffTime(ontime)) : "");
		xrr.addReq("OffTime", offtime);

		// DisplayTimeMS: extract from 1st page of MULTI
		Interval pt = determinePageOnInterval(m_sm.getMulti());
		xrr.addReq("DisplayTimeMS", pt.round(MILLISECONDS));

		// activation priority
		xrr.addReq("ActPriority", 
			m_sm.getActivationPriority());

		// runtime priority
		xrr.addReq("RunPriority", 
			m_sm.getRunTimePriority());

		// Owner
		xrr.addReq("Owner", (m_user != null) ? m_user.getName() : "");

		// bitmap
		xrr.addReq("Bitmap", m_bitmaps);

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

			LOG.log("OpMessage.parseResponse(): parsed msg " +
				"values: IsValid:" + valid + ".");
		} catch (IllegalArgumentException ex) {
			LOG.log("OpMessage.parseResponse(): Malformed " +
				"XML received:" + ex+", id=" + id);
			valid = false;
			errmsg = ex.getMessage();
			handleCommError(EventType.PARSING_ERROR, errmsg);
		}

		// update 
		complete(mess);

		// parse rest of response
		updateMaintStatus("");
		if (valid) {
			setErrorStatus("");
			m_dms.setMessageCurrent(m_sm);
		} else {
			LOG.log("OpMessage.parseResponse(): response " +
				"from SensorServer received, ignored " +
				"because Xml valid field is false, " +
				"errmsg=" + errmsg + ", id=" + id);
			setErrorStatus(errmsg);

			// try again
			if(flagFailureShouldRetry(errmsg)) {
				LOG.log("OpMessage: will retry " +
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
	 * @see CommThread#doPoll()
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
			LOG.log("PhaseSendMessage.poll(msg) called.");

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

	/** Cleanup the operation. Called by CommThread.doPoll(). */
	@Override
	public void cleanup() {
		m_dms.setMessageNext(null);
		super.cleanup();
	}
}
