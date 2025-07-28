/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.ParsingException;
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
	public OpMessage(DMSImpl d, SignMessage m) {
		super(PriorityLevel.COMMAND, d, "Sending new message");
		m_sm = m;
		BitmapGraphic[] bitmaps = SignMessageHelper.getBitmaps(m, d);
		m_npages = (bitmaps != null) ? bitmaps.length : 0;
		m_bitmaps = convertToHexString(bitmaps);
	}

	/** Operation equality test */
	public boolean equals(Object o) {
		if (o instanceof OpMessage) {
			OpMessage op = (OpMessage) o;
			return (m_dms == op.m_dms) && (m_sm == op.m_sm);
		} else
			return false;
	}

	/** Return the bitmap page as a hex string for all pages. */
	private String convertToHexString(BitmapGraphic[] bitmaps) {
		StringBuilder hs = new StringBuilder();
		if (bitmaps != null) {
			for (BitmapGraphic bg: bitmaps)
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
		m_dms.setMsgNext(m_sm);
		if (dmsConfigured())
			return createPhaseTwo();
		else {
			Phase phase2 = createPhaseTwo();
			return new PhaseGetConfig(phase2);
		}
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
		return (m_npages > 0) ? new PhaseSendMessage() : null;
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

	/** Calculate message off time, which is the start time + duration */
	protected Calendar calcMsgOffTime(Calendar ontime) {
		Long dur_ms = m_dms.getDurationMs();
		if (dur_ms != null) {
			Calendar offtime = (Calendar) ontime.clone();
			int dur_s = (int) (dur_ms / 1000);
			offtime.add(Calendar.SECOND, dur_s);
			return offtime;
		} else
			return null;
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

		xrr.addReq("Id", generateId());
		xrr.addReq("Address", controller.getDrop());
		xrr.addReq("MsgText", new MultiString(m_sm.getMulti())
			.normalize());
		xrr.addReq("UseOnTime", true);
		Calendar ontime = calcMsgOnTime();
		xrr.addReq("OnTime", STime.CalendarToXML(ontime));
		boolean useofftime = (m_dms.getDurationMs() != null);
		xrr.addReq("UseOffTime", useofftime);
		String offtime = (useofftime ?
			STime.CalendarToXML(calcMsgOffTime(ontime)) : "");
		xrr.addReq("OffTime", offtime);
		Interval pt = determinePageOnInterval(m_sm.getMulti());
		xrr.addReq("DisplayTimeMS", pt.round(MILLISECONDS));
		xrr.addReq("ActPriority", m_sm.getMsgPriority());
		xrr.addReq("RunPriority", m_sm.getMsgPriority());
		xrr.addReq("Owner", m_sm.getMsgOwner());
		xrr.addReq("Bitmap", m_bitmaps);

		// response
		xrr.addRes("Id");
		xrr.addRes("IsValid");
		xrr.addRes("ErrMsg");
		return xrr;
	}

	/** Parse response.
	 *  @return True to retry the operation else false if done. */
	private boolean parseResponse(Message mess, XmlElem xrr)
		throws IOException
	{
		long id = 0;
		try {
			id = xrr.getResLong("Id");
			boolean valid = xrr.getResBoolean("IsValid");
			String errmsg = xrr.getResString("ErrMsg");
			LOG.log("OpMessage.parseResponse(): parsed msg " +
				"values: IsValid:" + valid + ".");
			if (!valid) {
				LOG.log("OpMessage.parseResponse(): response " +
					"from SensorServer received, ignored " +
					"because Xml valid field is false, " +
					"errmsg=" + errmsg + ", id=" + id);
				throw new ControllerException(errmsg);
			}
		}
		catch (IllegalArgumentException ex) {
			LOG.log("OpMessage.parseResponse(): Malformed " +
				"XML received:" + ex+", id=" + id);
			throw new ParsingException(ex);
		}
		m_dms.setMsgCurrentNotify(m_sm, true);
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
			LOG.log("PhaseSendMessage.poll(msg) called.");
			Message mess = (Message) argmess;
			setMsgAttributes(mess);
			mess.setName(getOpName());
			XmlElem xrr = buildReqRes(getXmlReqName(),
				getXmlResName());
			mess.add(xrr);
			sendRead(mess);
			return parseResponse(mess, xrr) ? this : null;
		}
	}

	/** Cleanup the operation. Called by CommThread.doPoll(). */
	@Override
	public void cleanup() {
		m_dms.setMsgNext(null);
		super.cleanup();
	}
}
