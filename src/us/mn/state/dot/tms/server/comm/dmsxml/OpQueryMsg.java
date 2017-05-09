/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsMsgPriority;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.IrisUserHelper;
import us.mn.state.dot.tms.SignMsgSource;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.SignMessageImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
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

	/** External operator source */
	static private final int EXT_OPER = SignMsgSource.toBits(
		SignMsgSource.external, SignMsgSource.operator);

	/** device request */
	private DeviceRequest m_req;

	/** Indicates if this operation is the startup operation */
	private boolean m_startup;

	/** Constructor.
	 *  @param d DMS.
	 *  @param u User originating the request or null for IRIS.
	 *  @param req Device request.
	 *  @param startup True to indicate this is the device startup
	 *	   request, which is ignored for DMS on dial-up lines. */
	OpQueryMsg(DMSImpl d, User u, DeviceRequest req,
		boolean startup)
	{
		super(PriorityLevel.DEVICE_DATA, d, "Retrieving message", u);
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
		if(!useont) {
			throw new IllegalArgumentException(
				"must have ontime in calcMsgDuration.");
		}
		if(!useofft)
			return null;
		if(ontime == null) {
			throw new IllegalArgumentException(
				"invalid null ontime in calcMsgDuration.");
		}
		if(offtime == null) {
			throw new IllegalArgumentException(
				"invalid null offtime in calcMsgDuration.");
		}

		// calc diff in mins
		long delta = offtime.getTimeInMillis() -
		             ontime.getTimeInMillis();
		long m = ((delta < 0) ? 0 : delta / 1000 / 60);
		return (int)m;
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
		if(areBitmapsBlank(pages))
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
		for(int i = 0; i < pages.length; i++)
			if(pages[i].getLitCount() > 0)
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

		// already have dms configuration
		if(dmsConfigured())
			return new PhaseQueryMsg();

		// dms not configured
		Phase phase2 = new PhaseQueryMsg();
		Phase phase1 = new PhaseGetConfig(phase2);
		return phase1;
	}

	/**
	 * Create a SignMessage using a bitmap and no message text.
	 * @param sbitmap Bitmap as hexstring associated with message
	 *	  text. This bitmap is required to be a 96x25 bitmap
	 *        which dmsxml will always return.
	 * @param duration Message duration (in minutes).
	 * @param pgOnTime DMS page on time.
	 * @param apri DMS message activation priority.
	 * @param rpri DMS message runtime priority.
	 * @return A SignMessage that contains the text of the message and
	 *         a rendered bitmap. */
	private SignMessageImpl createSignMessageWithBitmap(String sbitmap,
		Integer duration, Interval pgOnTime, DmsMsgPriority apri,
		DmsMsgPriority rpri)
	{
		if(sbitmap == null)
			return null;
		byte[] argbitmap;
		try {
			argbitmap = HexString.parse(sbitmap);
		}
		catch(IllegalArgumentException e) {
			LOG.log("SEVERE: received invalid bitmap " +
				e.getMessage());
			return null;
		}
		if(argbitmap.length % BM_PGLEN_BYTES != 0) {
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
		if(numpgs <= 0)
			return null;

		BitmapGraphic[] pages = new BitmapGraphic[numpgs];
		for(int pg = 0; pg < numpgs; pg++)
			pages[pg] = extractBitmap(argbitmap, pg);

		String multi = createMultiUsingBitmap(pages, pgOnTime);
		LOG.log("OpQueryMsg.createSignMessageWithBitmap(): "+
			"multistring=" + multi);

		// priority is invalid, as expected
		if (apri == DmsMsgPriority.INVALID)
			apri = DmsMsgPriority.OTHER_SYSTEM;
		if (rpri == DmsMsgPriority.INVALID)
			rpri = DmsMsgPriority.OTHER_SYSTEM;

		return (SignMessageImpl) m_dms.createMsgRendered(multi, false,
			pages, apri, rpri, duration);
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
	private boolean parseResponse(Message mess, XmlElem xrr) {
		long id = 0;
		boolean valid = false;
		String errmsg = "";
		boolean txtavail = false;
		String msgtext = "";
		DmsMsgPriority apri = DmsMsgPriority.INVALID;
		DmsMsgPriority rpri = DmsMsgPriority.INVALID;
		String owner = "";
		boolean useont = false;
		Calendar ont = new GregorianCalendar();
		boolean useofft = false;
		Calendar offt = new GregorianCalendar();
		Interval pgOnTime = new Interval(0);
		boolean usebitmap = false;
		String bitmap = "";

		// parse response
		try {
			// id
			id = xrr.getResLong("Id");

			// valid flag
			valid = xrr.getResBoolean("IsValid");

			// error message text
			errmsg = xrr.getResString("ErrMsg");
			if(!valid && errmsg.length() <= 0)
				errmsg = FAILURE_UNKNOWN;

			if(valid) {
				// msg text available
				txtavail = xrr.getResBoolean(
					"MsgTextAvailable");

				// msg text
				msgtext = xrr.getResString("MsgText");

				// activation priority
				apri = DmsMsgPriority.fromOrdinal(
					xrr.getResInt("ActPriority"));
				apri = (apri == DmsMsgPriority.INVALID) ?
					DmsMsgPriority.OPERATOR : apri;

				// runtime priority
				rpri = DmsMsgPriority.fromOrdinal(
					xrr.getResInt("RunPriority"));
				rpri = (rpri == DmsMsgPriority.INVALID) ?
					DmsMsgPriority.BLANK : rpri;

				// owner
				owner = xrr.getResString("Owner");

				// ontime
				useont = xrr.getResBoolean("UseOnTime");
				if(useont)
					ont.setTime(xrr.getResDate("OnTime"));

				// offtime
				useofft = xrr.getResBoolean("UseOffTime");
				if(useofft)
					offt.setTime(xrr.getResDate("OffTime"));

				// display time (pg on-time)
				int ms = xrr.getResInt("DisplayTimeMS");
				pgOnTime = new Interval(ms, MILLISECONDS);
				LOG.log("PhaseQueryMsg: ms=" + ms +
					", pgOnTime=" + pgOnTime.ms());

				// bitmap
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
					", bitmap:" + bitmap);
			}
		} catch (IllegalArgumentException ex) {
			LOG.log("SEVERE: Malformed XML received:" +
			    ex + ", id=" + id);
			valid=false;
			errmsg=ex.getMessage();
			handleCommError(EventType.PARSING_ERROR,errmsg);
		}

		// update
		complete(mess);

		// process response
		if(valid) {
			setErrorStatus("");
			if (updateMaintStatus(owner))
				sendMaintenanceEmail();

			String iuser = null;

			// get user name via owner
			if (owner != null) {
				User irisUser = IrisUserHelper.lookup(owner);
				iuser = (irisUser == null) ?
					null : irisUser.getName();
				LOG.log("OpQueryMsg: owner read from " +
					"sensorserver=" + owner +
					", Iris user lookup=" + iuser);
			}

			// have on time? if not, create
			if (!useont) {
				useont=true;
				ont=new GregorianCalendar();
			}

			// error checking: valid off time?
			if (useont && useofft && offt.compareTo(ont)<=0) {
				useofft=false;
			}

			// calc message duration
			Integer duramins = calcMsgDuration(useont,
				useofft, ont, offt);

			// have text
			if(txtavail) {
				// update page on-time in MULTI with value
				// read from controller, which comes from
				// the DisplayTimeMS XML field, not the
				// MULTI string.
				msgtext = updatePageOnTime(msgtext, pgOnTime);
				SignMessageImpl sm = (SignMessageImpl)
					m_dms.createMsg(msgtext, false, apri,
					rpri, EXT_OPER, iuser, duramins);
				if (sm != null)
					m_dms.setMsgCurrentNotify(sm);

			// don't have text
			} else {

				SignMessageImpl sm = null;
				if (usebitmap) {
					sm = createSignMessageWithBitmap(
						bitmap, duramins, pgOnTime,
						apri, rpri);
					if (sm != null)
						m_dms.setMsgCurrentNotify(sm);
				}
				if (sm == null) {
					sm = (SignMessageImpl) m_dms.
						createMsg("", false, apri, rpri,
						EXT_OPER, iuser, null);
					if (sm != null)
						m_dms.setMsgCurrentNotify(sm);
				}
			}

		// valid flag is false
		} else {
			LOG.log("OpQueryMsg: response from SensorServer " +
				"received, ignored, Xml valid field is " +
				"false, errmsg=" + errmsg);
			setErrorStatus(errmsg);

			// try again
			if(flagFailureShouldRetry(errmsg)) {
				LOG.log("OpQueryMsg: will retry op.");
				return true;
			}
		}

		// this operation is complete
		return false;
	}

	/**
	 * Phase to get current message
	 * Note, the type of exception throw here determines
	 * if the messenger reopens the connection on failure.
	 *
	 * @see CommThread#doPoll()
	 * @see Messenger#handleCommError()
	 * @see Messenger#shouldReopen()
	 */
	private class PhaseQueryMsg extends Phase
	{
		/** Query current message */
		protected Phase poll(CommMessage argmess)
			throws IOException
		{
			// ignore startup operations for DMS on dial-up lines
			if(m_startup && !m_dms.isPeriodicallyQueriable())
				return null;

			LOG.log("OpQueryMsg.PhaseQueryMsg.poll(msg) " +
				"called, dms=" + m_dms.getName());

			Message mess = (Message) argmess;

			// set message attributes as a function of the op
			setMsgAttributes(mess);

			// build xml request and expected response
			mess.setName(getOpName());
			XmlElem xrr = buildXmlElem("StatusReqMsg",
				"StatusRespMsg");

			// send request and read response
			mess.add(xrr);
			sendRead(mess);
			if(xrr.wasResRead())
				if(parseResponse(mess, xrr))
					return this;
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			m_dms.msgQueried();
		super.cleanup();
	}
}
