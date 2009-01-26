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

package us.mn.state.dot.tms.comm.dmslite;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TreeMap;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.utils.SDMS;
import us.mn.state.dot.tms.utils.STime;

/**
 * Operation to query the current message on a DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpQueryMsg extends OpDms
{
	/**
	 * Calculate message duration
	 *
	 * @param useont true to use on time
	 * @param useofft true to use off time else infinite message
	 * @param ontime message on time
	 * @param offtime message off time
	 * @return Duration in minutes; null indicates no expiration.
	 * @throws IllegalArgumentException if invalid args.
	 */
	static private Integer calcMsgDuration(boolean useont, boolean useofft,
					   Calendar ontime, Calendar offtime)
	{
		if(!useont) {
			throw new IllegalArgumentException("must have ontime in calcMsgDuration.");
		}
		if(!useofft)
			return null;
		if(ontime == null) {
			throw new IllegalArgumentException("invalid null ontime in calcMsgDuration.");
		}
		if(offtime == null) {
			throw new IllegalArgumentException("invalid null offtime in calcMsgDuration.");
		}

		// calc diff in mins
		long delta = offtime.getTimeInMillis() -
		             ontime.getTimeInMillis();
		long m = ((delta < 0) ? 0 : delta / 1000 / 60);
		return (int)m;
	}

	/**
	 * Create message text given a bitmap.
	 * It is important to create message text for the message because
	 * the cmsserver returns a message containing a bitmap but with
	 * no message text. IRIS requires both a bitmap and message text,
	 * so this method constructs message text so IRIS will think it's a
	 * message, rather than a blank sign.
	 * 
	 * @return If bitmap is not blank, a page indicating it is an other
	 *         system message. If bitmap is blank, then "" is returned.
	 */
	static protected MultiString createMessageTextUsingBitmap(int numpages,
		byte[] bm)
	{
		MultiString multi = new MultiString();
		if(isBitmapBlank(bm))
			return multi; 

		// default text if no bitmap, see comments in method for why this is a hack
		final String TEXT1 = SDMS.flagIgnoredSignLineHack("OTHER");
		final String TEXT2 = SDMS.flagIgnoredSignLineHack("SYSTEM");
		final String TEXT3 = SDMS.flagIgnoredSignLineHack("MESSAGE");

		// build message
		for(int i = 0; i < numpages; i++) {
			multi.addText(TEXT1);
			multi.addLine();
			multi.addText(TEXT2);
			multi.addLine();
			multi.addText(TEXT3);
			multi.addPage();
		}
		return multi;
	}

	/** Check if a bitmap is blank or null */
	static protected boolean isBitmapBlank(byte[] b) {
		if(b == null)
			return true;
		for(int i = 0; i < b.length; i++)
			if(b[i] != 0)
				return false;
		return true;
	}

	/** Calculate the number of pages in a bitmap */
	static protected int calcNumPages(byte[] bm, int width, int height) {
		if(width<=0 || height<=0)
			return 0;

		// calc size of 1 page
		int lenpg = width * height / 8;
		if(lenpg <= 0)
			return 0;

		// calculate number of pages based on bitmap length
		int npgs = bm.length / lenpg;
		if(npgs * lenpg != bm.length)
			return 0;
		return npgs;
	}

	/** Create a new DMS query status object */
	public OpQueryMsg(DMSImpl d) {
		super(DEVICE_DATA, d, "OpQueryMsg");
	}

	/** return description of operation, which is displayed in the client */
	public String getOperationDescription() {
		return "Retrieving message";
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		// has getConfig() been called yet? If not, don't do anything
		// FIXME: there must be a better way to check for this condition
		if(m_dms.getWidthPixels() > 0)
			return new PhaseQueryCurrentMessage();
		else
			return null;
	}

	/**
	 * Create a SignMessage using a bitmap and no message text.
	 * @param owner message owner.
	 * @param sbitmap Bitmap as hexstring associated with message text.
	 * 	           This bitmap is assumed to be a 96x25 bitmap which
	 *                 dmslite will always return and is specific to 
	 *                 Caltrans. 
	 * @param dura message duration in mins.
	 * @return A SignMessage that contains the text of the message and 
	 *  a rendered bitmap.
	 */
	private SignMessage createSignMessageWithBitmap(String owner, 
		String sbitmap, int dura) 
	{
		// assumed bitmap page size, specific to Caltrans
		final int BM_WIDTH = 96;
		final int BM_HEIGHT = 25;
		final int BM_PGLEN_BYTES = BM_WIDTH * BM_HEIGHT / 8;
		final int BM_SIZE_HEXCHAR = BM_PGLEN_BYTES * 2;

		// sanity checks
		if(owner == null)
			owner = "unknown";
		if(sbitmap == null)
			return null;
		if(sbitmap.length() % BM_SIZE_HEXCHAR != 0 ) {
			System.err.println("WARNING: received bogus sbitmap size: len="+sbitmap.length());
			return null;
		}
		if(BM_HEIGHT != m_dms.getHeightPixels()) {
			assert false : "bogus bitmap size: " + BM_HEIGHT + ", " + m_dms.getHeightPixels();
			return null;
		}

		// convert bitmap to byte array
		byte[] argbitmap = new HexString(sbitmap).toByteArray();
		if(argbitmap == null) {
			assert false;
			return null;
		}

		System.err.println(
		    "OpQueryMsg.createSignMessageWithBitmap() called: m_dms.width="
		    + m_dms.getWidthPixels() + ", argbitmap.len="
		    + argbitmap.length + ", owner="+owner+".");

		// calc number of pages
		int numpgs = calcNumPages(argbitmap, BM_WIDTH, BM_HEIGHT);
		System.err.println("OpQueryMsg.createSignMessageWithBitmap(): numpages=" + numpgs);
		if(numpgs <= 0)
			return null;

		// create multistring
		MultiString multi = createMessageTextUsingBitmap(numpgs,
			argbitmap);
		System.err.println("OpQueryMsg.createSignMessageWithBitmap(): multistring=" + multi.toString());

		// create multipage bitmap
		TreeMap<Integer, BitmapGraphic> bitmaps = new TreeMap<Integer, BitmapGraphic>();
		for (int pg=0; pg<numpgs; ++pg) {

			// extract 1 page
			byte[] nbm = extractPage(pg, BM_PGLEN_BYTES, argbitmap);
			if(nbm==null)
				return null;
			BitmapGraphic bm = new BitmapGraphic(BM_WIDTH, BM_HEIGHT);
			bm.setBitmap(nbm);

			// resize to actual sign width
			BitmapGraphic bmgResize = new BitmapGraphic(
				m_dms.getWidthPixels(), bm.height);
			bmgResize.copy(bm);

			bitmaps.put(pg, bmgResize);
		}

		// create SignMessage
		return new SignMessage(owner, multi, bitmaps, dura);
	}

	/** extract a single page from a byte array */
	private byte[] extractPage(int pg, int pglen, byte[] argbitmap) {
		if(argbitmap == null || pg <0 || pglen <=0)
			return null;
		if(argbitmap.length % pglen != 0 ) {
			System.err.println("WARNING: extractPage() received bogus bitmap size: len=" +
				argbitmap.length + ", pglen=" + pglen);
			return null;
		}
		byte[] nbm = new byte[pglen];
		System.arraycopy(argbitmap, pg * pglen, nbm, 0, pglen);
		return nbm;
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
			//System.err.println(
			//    "OpQueryMsg.PhaseQueryCurrentMessage.poll(msg) called.");
			assert argmess instanceof Message :
			       "wrong message type";

			Message mess = (Message) argmess;

			// set message attributes as a function of the operation
			setMsgAttributes(mess);

			// build req msg and expected response
			mess.setName("StatusReqMsg");
			mess.setReqMsgName("StatusReqMsg");
			mess.setRespMsgName("StatusRespMsg");
			String addr = Integer.toString(controller.getDrop());
			ReqRes rr0 = new ReqRes("Id", generateId(), new String[] {"Id"});
			ReqRes rr1 = new ReqRes("Address", addr, new String[] {
				"IsValid", "ErrMsg", "MsgTextAvailable", "MsgText",
				"Owner", "UseOnTime", "OnTime", "UseOffTime",
				"OffTime", "UseBitmap", "Bitmap"});

			// send msg
			mess.add(rr0);
			mess.add(rr1);
            		mess.getRequest();	// throws IOException

			// parse resp msg
			long id = 0;
			boolean valid = false;
			String errmsg = "";
			boolean msgtextavailable = false;
			String msgtext = "";
			String owner = "";
			boolean useont = false;
			Calendar ont = new GregorianCalendar();
			boolean useofft = false;
			Calendar offt = new GregorianCalendar();
			boolean usebitmap = false;
			String bitmap = "";

			// parse
			try {
				// id
				id = new Long(rr0.getResVal("Id"));

				// valid flag
				valid = new Boolean(rr1.getResVal("IsValid"));

				// error message text
				errmsg = rr1.getResVal("ErrMsg");
				if (!valid && errmsg.length()<1)
					errmsg="request failed";

				if(valid) {
					msgtextavailable = new Boolean(
					    rr1.getResVal("MsgTextAvailable"));
					msgtext = rr1.getResVal("MsgText");
					owner = rr1.getResVal("Owner");

					// ontime
					useont = new Boolean(rr1.getResVal("UseOnTime"));
					if(useont) {
						ont.setTime(STime.XMLtoDate(rr1.getResVal("OnTime")));
					}

					// offtime
					useofft = new Boolean(rr1.getResVal("UseOffTime"));
					if(useofft) {
						offt.setTime(STime.XMLtoDate(rr1.getResVal("OffTime")));
					}

					// bitmap
					usebitmap = new Boolean(rr1.getResVal("UseBitmap"));
					bitmap = rr1.getResVal("Bitmap");

					System.err.println(
					    "OpQueryMsg.PhaseQueryCurrentMessage.poll(msg) parsed msg values: IsValid:"
					    + valid + ", MsgTextAvailable:"
					    + msgtextavailable + ", MsgText:"
					    + msgtext + ", OnTime:" + ont.getTime() 
					    + ", OffTime:" + offt.getTime() + ", bitmap:"
					    + bitmap);
				}
			} catch (IllegalArgumentException ex) {
				System.err.println("OpQueryMsg.PhaseQueryCurrentMessage: Malformed XML received:"
				    + ex+", id="+id);
				valid=false;
				errmsg=ex.getMessage();
				handleException(new IOException(errmsg));
			}

			// update 
			complete(mess);

			// process response
			if(valid) {
				// error checking: have on time? if not, create new ontime
				if (!useont) {
					useont=true;
					ont=new GregorianCalendar();
					//System.err.println("NOTE: DmsLite.OpQueryMsg.PhaseQueryCurrentMessage():"+
					//	" no ontime specified, assuming now.");
				}

				// error checking: valid off time?
				if (useont && useofft && offt.compareTo(ont)<=0) {
					useofft=false;
					//System.err.println("NOTE: DmsLite.OpQueryMsg.PhaseQueryCurrentMessage():"+
					//	" offtime <= ontime, so off time ignored.");
				}

				// calc message duration
				Integer duramins = calcMsgDuration(useont,
					useofft, ont, offt);

				// have text
				if(msgtextavailable) {
					SignMessageImpl sm = (SignMessageImpl)
						m_dms.createMessage(msgtext,
						DMSMessagePriority.OPERATOR);
					sm.setDuration(duramins);
					m_dms.setMessageCurrent(sm);

				// don't have text
				} else {
					SignMessage sm = null;
					if(usebitmap)
						sm = createSignMessageWithBitmap(owner, bitmap, duramins);
					if(sm == null)
						sm = m_dms.createMessage("", DMSMessagePriority.SCHEDULED);
					m_dms.setMessageCurrent(sm);
				}

			// valid flag is false
			} else {
				System.err.println(
				    "OpQueryMsg: response from cmsserver received, ignored because Xml valid field is false, errmsg="+errmsg);
				setDmsStatus(errmsg);

				// try again
				if (flagFailureShouldRetry(errmsg)) {
					System.err.println("OpQueryMsg: will retry failed operation.");
					return this;
				}
			}

			// this operation is complete
			return null;
		}
	}
}
