/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

package us.mn.state.dot.tms.comm.dmslite;

import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.ntcip.DmsMessageMemoryType;
import us.mn.state.dot.tms.comm.ntcip.DmsMessageMultiString;
import us.mn.state.dot.tms.comm.ntcip.DmsMessageStatus;
import us.mn.state.dot.tms.comm.ntcip.DmsMessageTimeRemaining;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TreeMap;

/**
 * Operation to query the current message on a DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpQueryDms extends OpDms
{
	/** Create a new DMS query status object */
	public OpQueryDms(DMSImpl d) {
		super(DEVICE_DATA, d);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {

		System.err.println(
		    "dmslite.OpQueryDms.phaseOne() called.: m_dms.getSignWidthPixels()="
		    + m_dms.getSignWidthPixels());

		// has getConfig() been called yet? If not, don't do anything
		// FIXME: there must be a better way to check for this condition
		if(m_dms.getSignWidthPixels() <= 0) {
			return null;
		}

		return new PhaseQueryCurrentMessage();
	}

	/** return true if the bitmap is blank or null */
	public static boolean isBitmapBlank(byte[] b) {
		if (b==null)
			return true;
		for (int i=0; i<b.length; ++i)
			if (b[i]!=0)
				return false;
		return true;
	}

	/** calculate the number of pages in a bitmap */
	protected int calcNumPages(byte[] bm) {

		// calc size of 1 page
		int lenpg=m_dms.getSignWidthPixels()*m_dms.getSignHeightPixels()/8;
		if (lenpg<=0) {
			return(0);
		}

		// calculate number of pages based on bitmap length
		int npgs=bm.length/lenpg;
		//System.err.println("OpQueryDms.calcnumPages(): bm.length="+bm.length+",lenpg="+lenpg+",npgs="+npgs);
		if (npgs*lenpg!=bm.length) {
			return(0);
		}

		return npgs;
	}

	/**
	 * create message text given a bitmap.
	 * 
	 * @returns If bitmap is not blank: "[nl]???" for 1 page,
	 *          If bitmap is not blank: "[nl]???[np][nl]???" for 2 pages,
	 *          If bitmap is blank, then an empty string is returned.
	 */
	protected static String createMessageTextUsingBitmap(int numpages,byte[] bm) {

		/** text for a single page */
		final String UNKNOWN_PG_TEXT = "[nl]???[np]";

		// is bitmap blank or null?
		if (OpQueryDms.isBitmapBlank(bm)) {
			return("");
		}

		// build message
		String msg="";
		for (int i=0; i<numpages; ++i) {
			msg+=UNKNOWN_PG_TEXT;
		}
	
		return (msg);
	}

	/**
	 * Create a SignMessage using a bitmap.
	 *
	 * @params owner message owner.
	 * @params argmulti A String in the MultiString format.
	 * @params argbitmap Bitmap associated with message text.
	 * @params dura message duration in mins.
	 * @returns A SignMessage that contains the text of the message and a rendered bitmap.
	 */
	private SignMessage createSignMessageWithBitmap(String owner, byte[] argbitmap, int dura) 
	{
		//System.err.println(
		//    "OpQueryDms.createSignMessageWithBitmap() called: m_dms.width="
		//    + m_dms.getSignWidthPixels() + ", argbitmap.len="
		//    + argbitmap.length + ".");

		assert owner != null;
		assert argbitmap != null;

		// calc number of pages
		int numpgs=this.calcNumPages(argbitmap);
		System.err.println("OpQueryDms.createSignMessageWithBitmap(): numpages=" + numpgs);

		// create multistring
		MultiString multi = new MultiString(OpQueryDms.createMessageTextUsingBitmap(numpgs,argbitmap));

		// create multipage bitmap
		TreeMap<Integer, BitmapGraphic> bitmaps = new TreeMap<Integer, BitmapGraphic>();
		for (int pg=0; pg<numpgs; ++pg) {
			int pglen=m_dms.getSignHeightPixels() * m_dms.getSignWidthPixels()/8;
			byte[] nbm = new byte[pglen];
			System.arraycopy(argbitmap, pg*pglen, nbm, 0, pglen);
			BitmapGraphic bm=new BitmapGraphic(m_dms.getSignWidthPixels(),m_dms.getSignHeightPixels());
			bm.setBitmap(nbm);
			bitmaps.put(pg, bm);
		}

		// create SignMessage
		SignMessage sm = new SignMessage(owner, multi, bitmaps, dura);

		return sm;
	}

	/**
	 * Calculate message duration
	 *
	 * @params useont true to use on time
	 * @params useofft true to use off time else infinite message
	 * @params ontime message on time
	 * @params offtime message off time
	 * @returns The duration of the message in minutes, which is always >= 0.
	 * @throws IllegalArgumentException if invalid args.
	 */
	private static int calcMsgDuration(boolean useont, boolean useofft,
					   Calendar ontime, Calendar offtime) {

		if(ontime == null) {
			throw new IllegalArgumentException("invalid null ontime in calcMsgDuration.");
		}
		if(offtime == null) {
			throw new IllegalArgumentException("invalid null offtime in calcMsgDuration.");
		}
		if(!useont) {
			throw new IllegalArgumentException("must have ontime in calcMsgDuration.");
		}
		if(!useofft) {
			return SignMessage.DURATION_INFINITE;
		}

		// calc diff in mins
		int ms = offtime.compareTo(ontime);
		int m = ((ms < 0) ? 0 : ms / 1000 / 60);

		System.err.println(
		    "OpQueryDms.calcMsgDuration: duration (mins)=" + m
		    + ", ontime=" + ontime + ", offtime=" + offtime);
		return (m);
	}

	/** create a blank message */
	protected SignMessage createBlankMsg(String owner)
	{
		MultiString multi = new MultiString();
		BitmapGraphic bbm =
			new BitmapGraphic(
		    		m_dms.getSignWidthPixels(), 
				m_dms.getSignHeightPixels());
		SignMessage sm = new SignMessage(owner,multi,bbm,0);
		return(sm);
	}

	/**
	 * Phase to get current message
	 * Note, the type of exception throw here determines
	 * if the messenger reopens the connection on failure.
	 *
	 * @see MessagePoller#doPoll()
	 * @see Messenger#handleException()
	 * @see Messenger#shouldReopen()
	 */
	protected class PhaseQueryCurrentMessage extends Phase
	{
		/** Query current message */
		protected Phase poll(AddressedMessage argmess)
			throws IOException {
			System.err.println(
			    "OpQueryDms.PhaseQueryCurrentMessage.poll(msg) called.");
			assert argmess instanceof Message :
			       "wrong message type";

			Message mess = (Message) argmess;

			// set message attributes as a function of the operation
			setMsgAttributes(mess);

			// build req msg and expected response
			mess.setName("StatusReqMsg");
			mess.setReqMsgName("StatusReqMsg");
			mess.setRespMsgName("StatusRespMsg");
			String addr =
				new Integer((int) m_dms.getController()
					.getDrop()).toString();
			ReqRes rr = new ReqRes("Address", addr, new String[] {
				"IsValid", "MsgTextAvailable", "MsgText",
				"Owner", "UseOnTime", "OnTime", "UseOffTime",
				"OffTime", "UseBitmap", "Bitmap"
			});

			// send msg
			mess.add(rr);
			mess.getRequest();

			// parse resp msg
			boolean valid = false;
			boolean msgtextavailable = false;
			String msgtext = "";
			String owner = "";
			boolean useont = false;
			Calendar ont = new GregorianCalendar();
			boolean useofft = false;
			Calendar offt = new GregorianCalendar();
			boolean usebitmap = false;
			String bitmap = "";

			// get valid flag
			try {
				valid = new Boolean(rr.getResVal("IsValid"));
				if(valid) {
					msgtextavailable = new Boolean(
					    rr.getResVal("MsgTextAvailable"));
					msgtext = rr.getResVal("MsgText");
					owner = rr.getResVal("Owner");

					// ontime
					useont = new Boolean(
					    rr.getResVal("UseOnTime"));
					if(useont) {
						ont.setTime(Time.XMLtoDate(rr.getResVal("OnTime")));
					}

					// offtime
					useofft = new Boolean(
					    rr.getResVal("UseOffTime"));
					if(useofft) {
						offt.setTime(Time.XMLtoDate(rr.getResVal("OffTime")));
					}

					// bitmap
					usebitmap = new Boolean(rr.getResVal("UseBitmap"));
					bitmap = rr.getResVal("Bitmap");

					System.err.println(
					    "OpQueryDms.PhaseQueryCurrentMessage.poll(msg) parsed msg values: IsValid:"
					    + valid + ", MsgTextAvailable:"
					    + msgtextavailable + ", MsgText:"
					    + msgtext + ", OnTime:" + ont
					    + ", OffTime:" + offt + ", bitmap:"
					    + bitmap);
				}
			} catch (IllegalArgumentException ex) {
				System.err.println(
				    "OpQueryDms.PhaseQueryCurrentMessage: Malformed XML received:"
				    + ex);
				throw ex;
			}

			// process response
			if(valid) {
				// System.err.println("OpQueryDms: valid response from cmsserver received.");

				// error checking: have on time? if not, create new ontime
				if (!useont) {
					useont=true;
					ont=new GregorianCalendar();
					System.err.println("NOTE: DmsLite.OpQueryDms.PhaseQueryCurrentMessage():"+
						" no ontime specified, assuming now.");
				}

				// error checking: valid off time?
				if (useont && useofft && offt.compareTo(ont)<=0) {
					useofft=false;
					System.err.println("NOTE: DmsLite.OpQueryDms.PhaseQueryCurrentMessage():"+
						" offtime <= ontime, so off time ignored.");
				}

				// calc message duration
				int duramins=OpQueryDms.calcMsgDuration(useont, useofft, ont, offt);

				// have text
				if(msgtextavailable) {
					m_dms.setMessageFromController(msgtext, duramins);

				// don't have text
				} else {
					SignMessage sm;

					// have bitmap
					if(usebitmap) {
						byte[] bm=Convert.hexStringToByteArray(bitmap);
						// System.err.println("OpQueryDms: hex string length=" + bitmap.length() + ", byte[] length=" + bm.length);
						sm = createSignMessageWithBitmap(owner, bm, duramins);

					// don't have bitmap, therefore CMS is blank
					} else {
						sm=createBlankMsg(owner);
					}

					// set new message
					m_dms.setActiveMessage(sm);
				}

			} else {
				System.err.println(
				    "OpQueryDms: response from cmsserver received, ignored because Xml valid field is false.");
			}

			// this operation is complete
			return null;
		}
	}
}
