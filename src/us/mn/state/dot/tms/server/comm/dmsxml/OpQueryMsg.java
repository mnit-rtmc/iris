/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.SignMsgPriority;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.SignMessageImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import static us.mn.state.dot.tms.server.comm.dmsxml.DmsXmlPoller.LOG;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.DECISECONDS;
import static us.mn.state.dot.tms.units.Interval.Units.MILLISECONDS;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Operation to query the current message on a DMS.
 *
 * @author Michael Darter
 * @author Douglas Lau
 * @author Travis Swanston
 */
class OpQueryMsg extends OpDms {

	/** device request */
	private DeviceRequest m_req;

	/** Indicates if this operation is the startup operation */
	private boolean m_startup;

	/** Constructor.
	 *  @param d DMS.
	 *  @param req Device request.
	 *  @param startup True to indicate this is the device startup
	 *	   request, which is ignored for DMS on dial-up lines. */
	OpQueryMsg(DMSImpl d, DeviceRequest req, boolean startup) {
		super(PriorityLevel.POLL_LOW, d, "Retrieving message");
		m_req = req;
		m_startup = startup;
	}

	/**
	 * Calculate message duration
	 * @param useont true to use on time
	 * @param useofft true to use off time else infinite message
	 * @param ontime message on time
	 * @param offtime message off time
	 * @return Duration in minutes; null indicates no expiration.
	 * @throws IllegalArgumentException if invalid args.
	 */
	private static Integer calcMsgDuration(boolean useont,
		boolean useofft, Calendar ontime, Calendar offtime)
	{
		if (!useont) {
			throw new IllegalArgumentException(
				"must have ontime in calcMsgDuration.");
		}
		if (!useofft)
			return null;
		if (ontime == null) {
			throw new IllegalArgumentException(
				"invalid null ontime in calcMsgDuration.");
		}
		if (offtime == null) {
			throw new IllegalArgumentException(
				"invalid null offtime in calcMsgDuration.");
		}

		// calc diff in mins
		long delta = offtime.getTimeInMillis() -
		             ontime.getTimeInMillis();
		long m = ((delta < 0) ? 0 : delta / 1000 / 60);
		return (int) m;
	}

	/** Create message MULTI string using a bitmap.
	 * A MULTI string must be created because the SensorServer can
	 * return a bitmap and no message text. IRIS requires both a
	 * bitmap and message text.
	 * @param pages Bitmap containing pages.
	 * @param pgOnTime DMS page on time.
	 * @return If bitmap is not blank, a MULTI indicating it is an
	 *         other system message. If bitmap is blank, then an
	 *         empty MULTI is returned. */
	private static String createMultiUsingBitmap(
		BitmapGraphic[] pages, Interval pgOnTime)
	{
		if (areBitmapsBlank(pages))
			return "";

		MultiBuilder multi = new MultiBuilder();

		// pg on-time read from controller
		multi.setPageTimes(pgOnTime.round(DECISECONDS), null);

		// default text if no bitmap, see comments in
		// method for why this is a hack.
		for (int i = 0; i < pages.length; i++) {
			if (i > 0)
				multi.addPage();
			multi.addSpan(DMSHelper.NOTXT_L1);
			multi.addLine(null);
			multi.addSpan(DMSHelper.NOTXT_L2);
			multi.addLine(null);
			multi.addSpan(DMSHelper.NOTXT_L3);
			multi.addLine(null);
		}
		return multi.toString();
	}

	/** Check if an array of bitmaps is blank */
	private static boolean areBitmapsBlank(BitmapGraphic[] pages) {
		for (int i = 0; i < pages.length; i++)
			if (pages[i].getLitCount() > 0)
				return false;
		return true;
	}

	/** Calculate the number of pages in a bitmap */
	private static int calcNumPages(byte[] bm) {
		return bm.length / BM_PGLEN_BYTES;
	}

	/** Extract a single page bitmap from a byte array.
	 * @param argbitmap Bitmap of all pages
	 * @param pg Page number to extract
	 * @return BitmapGraphic of requested page */
	private static BitmapGraphic extractBitmap(byte[] argbitmap,
		int pg)
	{
		byte[] pix = extractPage(argbitmap, pg);
		BitmapGraphic bm = new BitmapGraphic(BM_WIDTH, BM_HEIGHT);
		bm.setPixelData(pix);
		return bm;
	}

	/** Extract a single page from a byte array.
	 * @param argbitmap Bitmap of all pages
	 * @param pg Page number to extract
	 * @return Bitmap of requested page only */
	private static byte[] extractPage(byte[] argbitmap, int pg) {
		byte[] pix = new byte[BM_PGLEN_BYTES];
		System.arraycopy(argbitmap, pg * BM_PGLEN_BYTES, pix, 0,
			BM_PGLEN_BYTES);
		return pix;
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		if (dmsConfigured())
			return new PhaseQueryMsg();
		else {
			Phase phase2 = new PhaseQueryMsg();
			return new PhaseGetConfig(phase2);
		}
	}

	/**
	 * Create a SignMessage using a bitmap and no message text.
	 * @param sbitmap Bitmap as hexstring associated with message
	 *	  text. This bitmap is required to be a 96x25 bitmap
	 *        which dmsxml will always return.
	 * @param duration Message duration (in minutes).
	 * @param pgOnTime DMS page on time.
	 * @param rpri Sign message runtime priority.
	 * @return A SignMessage that contains the text of the message and
	 *         a rendered bitmap. */
	private SignMessageImpl createSignMessageWithBitmap(String sbitmap,
		Integer duration, Interval pgOnTime, SignMsgPriority rpri,
		String owner)
	{
		if (sbitmap == null)
			return null;
		byte[] argbitmap;
		try {
			argbitmap = HexString.parse(sbitmap);
		}
		catch (IllegalArgumentException e) {
			LOG.log("SEVERE: received invalid bitmap " +
				e.getMessage());
			return null;
		}
		if (argbitmap.length % BM_PGLEN_BYTES != 0) {
			LOG.log("SEVERE: received bogus bitmap " +
				"size: len=" + argbitmap.length +
				", BM_PGLEN_BYTES=" + BM_PGLEN_BYTES);
			return null;
		}
		LOG.log("OpQueryMsg.createSignMessageWithBitmap() " +
			"called: argbitmap.len=" + argbitmap.length + ".");

		int numpgs = calcNumPages(argbitmap);
		LOG.log("OpQueryMsg.createSignMessageWithBitmap(): "+
			"numpages=" + numpgs);
		if (numpgs <= 0)
			return null;

		BitmapGraphic[] pages = new BitmapGraphic[numpgs];
		for (int pg = 0; pg < numpgs; pg++)
			pages[pg] = extractBitmap(argbitmap, pg);

		String multi = createMultiUsingBitmap(pages, pgOnTime);
		LOG.log("OpQueryMsg.createSignMessageWithBitmap(): "+
			"multistring=" + multi);

		// priority is invalid, as expected
		if (rpri == SignMsgPriority.invalid)
			rpri = SignMsgPriority.medium_sys;

		return createMsg(multi, owner, rpri, duration);
	}

	/** Create a sign message */
	private SignMessageImpl createMsg(String multi, String owner,
		SignMsgPriority rpri, Integer duration)
	{
		return (SignMessageImpl) m_dms.createMsg(multi, owner, false,
			false, rpri, duration);
	}

	/** Return a MULTI with an updated page on-time with the value read
	 * from controller.
	 * @param multi MULTI string.
	 * @param pt Page on time, used to update returned MultiString.
	 * @return MULTI string containing updated page on time. */
	private String updatePageOnTime(String multi, Interval pt) {
		MultiString m = new MultiString(multi);
		int npgs = m.getNumPages();
		// if one page, use page on-time of zero.
		if (npgs <= 1)
			pt = new Interval(0);
		String ret = m.replacePageTime(pt.round(DECISECONDS), null);
		LOG.log("OpQueryMsg.updatePageOnTime(): " +
			"updated multi w/ page display time: " + ret);
		return ret;
	}

	/** Build XML element:
	 *	<DmsXml><SetBlankMsgReqMsg>
	 *		<Id></Id>
	 *		<Address>1</Address>
	 *	</SetBlankMsgReqMsg></DmsXml>
	 */
	private XmlElem buildXmlElem(String elemReqName, String elemResName) {
		XmlElem xrr = new XmlElem(elemReqName, elemResName);

		// request
		xrr.addReq("Id", generateId());
		xrr.addReq("Address", controller.getDrop());

		// response
		xrr.addRes("Id");
		xrr.addRes("IsValid");
		xrr.addRes("ErrMsg");
		xrr.addRes("MsgTextAvailable");
		xrr.addRes("MsgText");
		xrr.addRes("ActPriority");
		xrr.addRes("RunPriority");
		xrr.addRes("Owner");
		xrr.addRes("UseOnTime");
		xrr.addRes("OnTime");
		xrr.addRes("UseOffTime");
		xrr.addRes("OffTime");
		xrr.addRes("DisplayTimeMS");
		xrr.addRes("UseBitmap");
		xrr.addRes("Bitmap");

		return xrr;
	}

	/** Parse response.
	 *  @return True to retry the operation else false if done. */
	private boolean parseResponse(Message mess, XmlElem xrr)
		throws IOException
	{
		long id = 0;
		boolean txtavail = false;
		String msgtext = "";
		SignMsgPriority apri = SignMsgPriority.invalid;
		SignMsgPriority rpri = SignMsgPriority.invalid;
		String owner = "";
		boolean useont = false;
		Calendar ont = new GregorianCalendar();
		boolean useofft = false;
		Calendar offt = new GregorianCalendar();
		Interval pgOnTime = new Interval(0);
		boolean usebitmap = false;
		String bitmap = "";

		try {
			id = xrr.getResLong("Id");
			boolean valid = xrr.getResBoolean("IsValid");
			String errmsg = xrr.getResString("ErrMsg");
			if (!valid) {
				LOG.log(
					"OpQueryMsg: response received, " +
					"ignored, valid is false, " +
					"errmsg=" + errmsg
				);
				throw new ControllerException(errmsg);
			}
			txtavail = xrr.getResBoolean("MsgTextAvailable");
			msgtext = xrr.getResString("MsgText");
			apri = SignMsgPriority.fromOrdinal(
				xrr.getResInt("ActPriority"));
			if (apri == SignMsgPriority.invalid)
				apri = SignMsgPriority.high_1;
			rpri = SignMsgPriority.fromOrdinal(
				xrr.getResInt("RunPriority"));
			if (rpri == SignMsgPriority.invalid)
				rpri = SignMsgPriority.low_1;
			owner = xrr.getResString("Owner");
			useont = xrr.getResBoolean("UseOnTime");
			if (useont)
				ont.setTime(xrr.getResDate("OnTime"));
			useofft = xrr.getResBoolean("UseOffTime");
			if (useofft)
				offt.setTime(xrr.getResDate("OffTime"));
			int ms = xrr.getResInt("DisplayTimeMS");
			pgOnTime = new Interval(ms, MILLISECONDS);
			LOG.log("PhaseQueryMsg: ms=" + ms +
				", pgOnTime=" + pgOnTime.ms());
			usebitmap = xrr.getResBoolean("UseBitmap");
			bitmap = xrr.getResString("Bitmap");
			LOG.log(
				"OpQueryMsg() parsed msg values: " +
				"IsValid:" + valid +
				", MsgTextAvailable:" + txtavail +
				", MsgText:" + msgtext +
				", ActPriority:"  + apri +
				", RunPriority:"  + rpri +
				", Owner:"  + owner +
				", OnTime:"  + ont.getTime() +
				", OffTime:" + offt.getTime() +
				", pgOnTime:" + pgOnTime +
				", bitmap:" + bitmap
			);
		}
		catch (IllegalArgumentException ex) {
			LOG.log("SEVERE: Malformed XML received:" +
			    ex + ", id=" + id);
			throw new ParsingException(ex);
		}

		checkMsgOwner(owner);

		// have on time?  if not, create
		if (!useont) {
			useont = true;
			ont = new GregorianCalendar();
		}
		// error checking: valid off time?
		if (useont && useofft && offt.compareTo(ont) <= 0)
			useofft = false;
		Integer duramins = calcMsgDuration(useont,
			useofft, ont, offt);

		if (txtavail) {
			// update page on-time in MULTI with value read from
			// controller, which comes from the DisplayTimeMS XML
			// field, not the MULTI string.
			msgtext = updatePageOnTime(msgtext, pgOnTime);
			SignMessageImpl sm = createMsg(msgtext, owner,
				rpri, duramins);
			if (sm != null)
				m_dms.setMsgCurrentNotify(sm);
		} else {
			// don't have text
			SignMessageImpl sm = null;
			if (usebitmap) {
				sm = createSignMessageWithBitmap(
					bitmap, duramins, pgOnTime,
					rpri, owner);
				if (sm != null)
					m_dms.setMsgCurrentNotify(sm);
			}
			if (sm == null) {
				sm = createMsg("", owner, rpri, null);
				if (sm != null)
					m_dms.setMsgCurrentNotify(sm);
			}
		}
		return false;
	}

	/**
	 * Phase to get current message
	 * Note, the type of exception throw here determines
	 * if the messenger reopens the connection on failure.
	 *
	 * @see CommThread#doPoll()
	 */
	private class PhaseQueryMsg extends Phase {

		/** Query current message */
		protected Phase poll(CommMessage argmess)
			throws IOException
		{
			// ignore startup operations for DMS on dial-up lines
			if (m_startup && m_dms.isDialUpRequired())
				return null;

			LOG.log("OpQueryMsg.PhaseQueryMsg.poll(msg) " +
				"called, dms=" + m_dms.getName());

			Message mess = (Message) argmess;
			setMsgAttributes(mess);
			mess.setName(getOpName());
			XmlElem xrr = buildXmlElem("StatusReqMsg",
				"StatusRespMsg");
			mess.add(xrr);
			sendRead(mess);
			if (xrr.wasResRead() && parseResponse(mess, xrr))
				return this;
			else
				return null;
		}
	}
}
