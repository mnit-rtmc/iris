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
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.DmsPgTime;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.IrisUserHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.SignMessageImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.utils.Log;
import us.mn.state.dot.tms.utils.SString;
import us.mn.state.dot.tms.utils.STime;

/**
 * Operation to query the current message on a DMS.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class OpQueryMsg extends OpDms {

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
	public OpQueryMsg(DMSImpl d, User u, DeviceRequest req, 
		boolean startup) 
	{
		super(DEVICE_DATA, d, "Retrieving message", u);
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
	static private Integer calcMsgDuration(boolean useont, 
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
	static protected String createMultiUsingBitmap(
		BitmapGraphic[] pages, DmsPgTime pgOnTime)
	{
		if(areBitmapsBlank(pages))
			return ""; 

		MultiString multi = new MultiString();

		// pg on-time read from controller
		multi.setPageTimes(pgOnTime.toTenths(), null);

		// default text if no bitmap, see comments in 
		// method for why this is a hack.
		for(int i = 0; i < pages.length; i++) {
			multi.addSpan(flagIgnoredSignLineHack("OTHER"));
			multi.addLine();
			multi.addSpan(flagIgnoredSignLineHack("SYSTEM"));
			multi.addLine();
			multi.addSpan(flagIgnoredSignLineHack("MESSAGE"));
			multi.addLine();
			if(i + 1 < pages.length)
				multi.addPage();
		}
		return multi.toString();
	}

	/** This is a hack.
	 * @see SignTextComboBoxModel.ignoreLineHack
	 */
	static protected String flagIgnoredSignLineHack(String line) {
		return "_" + line + "_";
	}

	/** Check if an array of bitmaps is blank */
	static protected boolean areBitmapsBlank(BitmapGraphic[] pages) {
		for(int i = 0; i < pages.length; i++)
			if(pages[i].getLitCount() > 0)
				return false;
		return true;
	}

	/** Calculate the number of pages in a bitmap */
	static protected int calcNumPages(byte[] bm) {
		return bm.length / BM_PGLEN_BYTES;
	}

	/** Extract a single page bitmap from a byte array.
	 * @param argbitmap Bitmap of all pages
	 * @param pg Page number to extract
	 * @return BitmapGraphic of requested page */
	static protected BitmapGraphic extractBitmap(byte[] argbitmap, int pg) {
		byte[] pix = extractPage(argbitmap, pg);
		BitmapGraphic bm = new BitmapGraphic(BM_WIDTH, BM_HEIGHT);
		bm.setPixels(pix);
		return bm;
	}

	/** Extract a single page from a byte array.
	 * @param argbitmap Bitmap of all pages
	 * @param pg Page number to extract
	 * @return Bitmap of requested page only */
	static protected byte[] extractPage(byte[] argbitmap, int pg) {
		byte[] pix = new byte[BM_PGLEN_BYTES];
		System.arraycopy(argbitmap, pg * BM_PGLEN_BYTES, pix, 0,
			BM_PGLEN_BYTES);
		return pix;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {

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
	 *        which dmslite will always return.
	 * @param duration Message duration (in minutes).
	 * @param pgOnTime DMS page on time.
	 * @param apri DMS message activation priority.
	 * @param rpri DMS message runtime priority.
	 * @return A SignMessage that contains the text of the message and 
	 *         a rendered bitmap. */
	private SignMessageImpl createSignMessageWithBitmap(String sbitmap,
		Integer duration, DmsPgTime pgOnTime, DMSMessagePriority apri,
		DMSMessagePriority rpri)
	{
		if(sbitmap == null)
			return null;
		byte[] argbitmap = new HexString(sbitmap).toByteArray();
		if(argbitmap.length % BM_PGLEN_BYTES != 0) {
			Log.severe("WARNING: received bogus bitmap " +
				"size: len=" + argbitmap.length +
				", BM_PGLEN_BYTES=" + BM_PGLEN_BYTES);
			return null;
		}
		Log.finest("OpQueryMsg.createSignMessageWithBitmap() " +
			"called: argbitmap.len=" + argbitmap.length + ".");

		int numpgs = calcNumPages(argbitmap);
		Log.finest("OpQueryMsg.createSignMessageWithBitmap(): "+
			"numpages=" + numpgs);
		if(numpgs <= 0)
			return null;

		BitmapGraphic[] pages = new BitmapGraphic[numpgs];
		for(int pg = 0; pg < numpgs; pg++)
			pages[pg] = extractBitmap(argbitmap, pg);

		String multi = createMultiUsingBitmap(pages, pgOnTime);
		Log.finest("OpQueryMsg.createSignMessageWithBitmap(): "+
			"multistring=" + multi);

		// priority is invalid, as expected
		if(apri == DMSMessagePriority.INVALID)
			apri = DMSMessagePriority.OTHER_SYSTEM;
		if(rpri == DMSMessagePriority.INVALID)
			rpri = DMSMessagePriority.OTHER_SYSTEM;

		return (SignMessageImpl)m_dms.createMessage(multi, pages, 
			apri, rpri, duration);
	}

	/** Return a MULTI with an updated page on-time with 
	 *  the value read from controller.
	 *  @param pt Page on time, used to update returned MultiString. 
	 *  @return MULTI string containing updated page on time. */
	private String updatePageOnTime(String multi, DmsPgTime pt) {
		int npgs = new MultiString(multi).getNumPages();
		// if one page, use page on-time of zero.
		if(npgs <= 1)
			pt = DmsPgTime.getDefaultOn(true);
		String ret = MultiString.replacePageOnTime(
			multi, pt.toTenths());
		Log.finest("OpQueryMsg.updatePageOnTime(): " +
			"updated multi w/ page display time: " + ret);
		return ret;
	}

	/** Build request message in this format:
	 *	<DmsLite><SetBlankMsgReqMsg>
	 *		<Id></Id>
	 *		<Address>1</Address>
	 *	</SetBlankMsgReqMsg></DmsLite>
	 */
	private XmlElem buildReqRes(String elemReqName, String elemResName) {
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
		DMSMessagePriority apri = DMSMessagePriority.INVALID;
		DMSMessagePriority rpri = DMSMessagePriority.INVALID;
		String owner = "";
		boolean useont = false;
		Calendar ont = new GregorianCalendar();
		boolean useofft = false;
		Calendar offt = new GregorianCalendar();
		DmsPgTime pgOnTime = new DmsPgTime(0);
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
				apri = DMSMessagePriority.fromOrdinal(
					xrr.getResInt("ActPriority"));

				// runtime priority
				rpri = DMSMessagePriority.fromOrdinal(
					xrr.getResInt("RunPriority"));

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
				pgOnTime = new DmsPgTime(
					DmsPgTime.MsToTenths(ms));
				Log.finest("PhaseQueryMsg: ms=" + ms +
					", pgOnTime="+pgOnTime.toMs());

				// bitmap
				usebitmap = xrr.getResBoolean("UseBitmap");
				bitmap = xrr.getResString("Bitmap");

				Log.finest(
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
			Log.severe("OpQueryMsg: Malformed XML received:" +
			    ex + ", id=" + id);
			valid=false;
			errmsg=ex.getMessage();
			handleCommError(EventType.PARSING_ERROR,errmsg);
		}

		// update 
		complete(mess);

		// user who created the message retrieved from the DMS
		User irisUser = null;

		// process response
		if(valid) {
			setErrorMsg("");

			// get user name via owner
			if(owner != null) {
				irisUser = IrisUserHelper.lookup(owner);
				String iuser = (irisUser == null ? 
					"null" : irisUser.getName());
				Log.finest("OpQueryMsg: owner read from " + 
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

				// this shouldn't happen if we have msg text
				if(apri == DMSMessagePriority.INVALID) {
					Log.warning("Received invalid " +
						"actpriority for id=" + id);
					apri = DMSMessagePriority.OPERATOR;
				}
				if(rpri == DMSMessagePriority.INVALID) {
					Log.warning("Received invalid " +
						"runpriority for id=" + id);
					rpri = DMSMessagePriority.OPERATOR;
				}
				SignMessageImpl sm = (SignMessageImpl)
					m_dms.createMessage(msgtext,
					apri, rpri, duramins);
				if(sm != null)
					m_dms.setMessageCurrent(sm, irisUser);

			// don't have text
			} else {

				SignMessageImpl sm = null;
				if(usebitmap) {
					sm = createSignMessageWithBitmap(
						bitmap, duramins, pgOnTime, 
						apri, rpri);
					if(sm != null) {
						m_dms.setMessageCurrent(sm, 
							irisUser);
					}
				}
				if(sm == null) {
					sm = (SignMessageImpl)m_dms.
						createMessage("",
						DMSMessagePriority.OVERRIDE, 
						DMSMessagePriority.OVERRIDE, 
						null);
					if(sm != null) {
						m_dms.setMessageCurrent(sm, 
							irisUser);
					}
				}
			}

		// valid flag is false
		} else {
			Log.finest("OpQueryMsg: response from SensorServer " +
				"received, ignored, Xml valid field is " +
				"false, errmsg=" + errmsg);
			setErrorMsg(errmsg);

			// try again
			if(flagFailureShouldRetry(errmsg)) {
				Log.finest("OpQueryMsg: will retry op.");
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
	 * @see MessagePoller#doPoll()
	 * @see Messenger#handleCommError()
	 * @see Messenger#shouldReopen()
	 */
	protected class PhaseQueryMsg extends Phase
	{
		/** Query current message */
		protected Phase poll(AddressedMessage argmess)
			throws IOException
		{
			// ignore startup operations for DMS on dial-up lines
			if(m_startup && 
				!DMSHelper.isPeriodicallyQueriable(m_dms)) {
				return null;
			}

			updateInterStatus("Starting operation", false);
			Log.finest("OpQueryMsg.PhaseQueryMsg.poll(msg) " +
				"called, dms=" + m_dms.getName());

			Message mess = (Message) argmess;

			// set message attributes as a function of the op
			setMsgAttributes(mess);

			// build xml request and expected response
			mess.setName(getOpName());
			XmlElem xrr = buildReqRes("StatusReqMsg", 
				"StatusRespMsg");

			// send request and read response
			mess.add(xrr);
			sendRead(mess);

			if(parseResponse(mess, xrr))
				return this;
			return null;
		}
	}
}
