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
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.utils.STime;

/**
 * Operation to send a new message to a DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpMessage extends OpDms {

	/** Maximum message priority */
	static protected final int MAX_MESSAGE_PRIORITY = 255;

	/** Flag to avoid phase loops */
	protected boolean modify = true;

	/** Sign message */
	protected final SignMessage m_signMessage;

	/** Create a new DMS command message object */
	public OpMessage(DMSImpl d, SignMessage m, User u) {
		super(COMMAND, d, "Sending new message", u);
		m_signMessage = m;
	}

	/** 
	 * Return the bitmap page as a hex string. The width of the 
	 * bitmap is adjusted as necessary.
	 */
	public String getBitmapPage(int pg) {
		if(m_signMessage == null)
			return "";
		byte[] bitmaps = getBitmaps();
		if(bitmaps == null)
			return "";
		BitmapGraphic oldbmg = createBitmap();
		if(oldbmg == null)
			return "";
		int blen = oldbmg.getBitmap().length;
		if(bitmaps.length % blen != 0)
			return "";
		int pages = bitmaps.length / blen;
		if(pg < 0 || pg >= pages)
			return "";
		byte[] bmap = new byte[blen];
		System.arraycopy(bitmaps, pg * blen, bmap, 0, blen);
		oldbmg.setBitmap(bmap);
		BitmapGraphic newbmg = new BitmapGraphic(BM_WIDTH, BM_HEIGHT);
		newbmg.copy(oldbmg);
		return new HexString(newbmg.getBitmap()).toString();
	}

	/** Get the sign message bitmaps */
	protected byte[] getBitmaps() {
		try {
			return Base64.decode(m_signMessage.getBitmaps());
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

	/** create 2nd phase */
	private Phase createPhaseTwo()
	{
		if(!m_dms.checkPriority(m_signMessage.getPriority()))
			return null;
		byte[] bitmaps = getBitmaps();
		if(bitmaps == null)
			return null;
		int blen = getPageLength();
		if(blen == 0 || bitmaps.length % blen != 0)
			return null;
		int np = bitmaps.length / blen;
		if(np <= 0)
			return null;
		else if(np == 1)
			return new PhaseSendOnePageMessage();
		else if(np == 2)
			return new PhaseSendTwoPageMessage();
		System.err.println("WARNING: bogus number of pages (" + np +
			") in dmslite.OpMessage.OpMessage(). Ignored.");
		return null;
	}

	/** Get the length of one bitmap page */
	protected int getPageLength() {
		BitmapGraphic b = createBitmap();
		if(b != null)
			return b.getBitmap().length;
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
	 *  This method should not be called if duration is infinite.
	 */
	protected Calendar calcMsgOffTime(Calendar ontime) {
		Integer mins = m_signMessage.getDuration();
		assert mins != null;
		Calendar offtime = (Calendar)ontime.clone();
		offtime.add(Calendar.MINUTE, mins);
		return offtime;
	}

	/**
	 * Phase to send a one page message.
	 * Note, the type of exception throw here determines
	 * if the messenger reopens the connection on failure.
	 *
	 * @see MessagePoller#doPoll()
	 * @see Messenger#handleException()
	 * @see Messenger#shouldReopen()
	 */
	protected class PhaseSendOnePageMessage extends Phase {

		/**
		 * Set the status to modify request. Called by Operation.poll().
		 * @param argmess
		 * @return next Phase to execute else null.
		 * @throws IOException
		 */
		protected Phase poll(AddressedMessage argmess)
			throws IOException 
		{
			System.err.println(
			    "dmslite.OpMessage.PhaseSendOnePageMessage.poll(msg) called.");
			assert argmess instanceof Message :
			       "wrong message type";

			Message mess = (Message) argmess;

			// set message attributes as a function of the operation
			setMsgAttributes(mess);

			/*  build req msg and expected response
			 *     <DmsLite>
			 *        <SetSnglPgReqMsg>
			 *           <Id>...</Id>
			 *           <Address>...</Address>
			 *           <MsgText>...</MsgText>             multistring cms message text
			 *	     <FontName>...</FontName>		font name
			 *           <UseOnTime>...</UseOnTime>         true to use on time, else now
			 *           <OnTime>...</OnTime>             	message on time
			 *           <UseOffTime>...</UseOffTime>       true to use off time, else indefinite
		 	 *           <OffTime>...</OffTime>           	message off time
			 *           <Owner>...</Owner>                 the message author
			 *           <Msg>...</Msg>                     this is the bitmap
			 *        </SetSnglPgReqMsg>
			 *     </DmsLite>
			 */

			final String reqname = "SetSnglPgReqMsg";
			final String resname = "SetSnglPgRespMsg";

			mess.setName(getOpName());
			mess.setReqMsgName(reqname);
			mess.setRespMsgName(resname);

			String addr = Integer.toString(controller.getDrop());
			ReqRes rr0 = new ReqRes("Id", generateId(), 
				new String[] {"Id"});
			ReqRes rr1 = new ReqRes("Address", addr,
				new String[] { "IsValid", "ErrMsg" });
			mess.add(rr0);
			mess.add(rr1);

			// MsgText
			mess.add(new ReqRes("MsgText",m_signMessage.getMulti().toString()));

			// FontName
			// FIXME: mtod from r8p9 merge
			String fn = "fontname"; //m_signMessage.getFontName();
			mess.add(new ReqRes("FontName", (fn == null ? "" : fn)));

			// UseOnTime, always true
			mess.add(new ReqRes("UseOnTime",new Boolean(true).toString()));

			// OnTime
			Calendar ontime=calcMsgOnTime();
			mess.add(new ReqRes("OnTime",STime.CalendarToXML(ontime)));

			// UseOffTime
			boolean useofftime = m_signMessage.getDuration() !=null;
			mess.add(new ReqRes("UseOffTime",new Boolean(useofftime).toString()));

			// OffTime, only used if duration is not infinite
			String offtime= (useofftime ? STime.CalendarToXML(calcMsgOffTime(ontime)) : "");
			mess.add(new ReqRes("OffTime",offtime));

			// Owner
			String owner = m_user != null ? m_user.getFullName() : "";
			mess.add(new ReqRes("Owner", owner));

			// bitmap
			mess.add(new ReqRes("Msg", getBitmapPage(0)));

			// send msg to field controller
            		mess.getRequest();	// throws IOException

			// parse resp msg
			{
				// get valid flag
				long id = 0;
				boolean valid = false;
				String errmsg = "";

				try {
					// id
					id = new Long(rr0.getResVal("Id"));

					// valid flag
					valid = new Boolean(rr1.getResVal("IsValid"));
					System.err.println(
					    "dmslite.OpMessage.PhaseSendOnePageMessage.poll(msg): parsed msg values: IsValid:"
					    + valid + ".");

					// error message text
					errmsg = rr1.getResVal("ErrMsg");
					if(!valid && errmsg.length() <= 0)
						errmsg = FAILURE_UNKNOWN;

				} catch (IllegalArgumentException ex) {
					System.err.println(
					    "dmslite.OpMessage.PhaseSendOnePageMessage.poll(msg): Malformed XML received:"
					    + ex+", id="+id);
					valid=false;
					errmsg=ex.getMessage();
					handleException(new IOException(errmsg));
				}

				// update 
				complete(mess);

				// parse rest of response
				if (valid) {
					// set new message
					m_dms.setMessageCurrent(m_signMessage,
						m_user);
				} else {
					System.err.println(
					    "OpMessage: cmsserver response received, IsValid is false, errmsg="+
					    errmsg+", id="+id);
					errorStatus = errmsg;

					// try again
					if (flagFailureShouldRetry(errmsg)) {
						System.err.println("OpMessage: will retry failed operation.");
						return this;

					// give up
					} else {
						// if caws failure, handle it
						if(mess.checkAwsFailure())
							mess.handleAwsFailure("was sending a message.");						
					}
				}
			}

			// this operation is complete
			return null;
		}
	}

	/**
	 * Phase to send a two page message.
	 * Note, the type of exception throw here determines
	 * if the messenger reopens the connection on failure.
	 *
	 * @see MessagePoller#doPoll()
	 * @see Messenger#handleException()
	 * @see Messenger#shouldReopen()
	 */
	protected class PhaseSendTwoPageMessage extends Phase {

		/**
		 * Set the status to modify request. Called by Operation.poll().
		 * @param argmess
		 * @return next Phase to execute else null.
		 * @throws IOException
		 */
		protected Phase poll(AddressedMessage argmess)
			throws IOException {
			System.err.println(
			    "dmslite.OpMessage.PhaseSendTwoPageMessage.poll(msg) called.");
			assert argmess instanceof Message : "wrong message type";

			Message mess = (Message) argmess;

			// set message attributes as a function of the operation
			setMsgAttributes(mess);

			/*
 			 * Return a newly created SignViewOperation using a dmslite xml msg string. 
			 * The xml string is expected to be in the following format. 
			 *
			 *    <DmsLite>
			 *       <SetMultiplePageReqMsg>
			 *          <Id>...</Id>
			 *          <Address>...</Address>
			 *          <MsgText>...</MsgText>               multistring cms message text
			 *	    <FontName>...</FontName>		 font name
			 *          <UseOnTime>...</UseOnTime>         	 true to use on time, else now
			 *          <OnTime>...</OnTime>             	 message on time
			 *          <UseOffTime>...</UseOffTime>       	 true to use off time, else indefinite
			 *          <OffTime>...</OffTime>           	 message off time
			 *          <DisplayTimeMS>...<DisplayTimeMS>    message display time
			 *          <Owner>...</Owner>                   the message author
			 *          <Msg>...</Msg>
			 *       </SetMultiplePageReqMsg>
			 *    </DmsLite>
			 */

			ReqRes rr0;
			ReqRes rr1;
			{
				mess.setName(getOpName());
				mess.setReqMsgName("SetMultPgReqMsg");
				mess.setRespMsgName("SetMultPgRespMsg");

				// id
				rr0 = new ReqRes("Id", generateId(), 
					new String[] {"Id"});
				mess.add(rr0);

				// drop
				String addr = Integer.toString(
					controller.getDrop());
				rr1 = new ReqRes("Address", addr,new 
					String[] { "IsValid", "ErrMsg" });
				mess.add(rr1);
			}

			// MsgText
			mess.add(new ReqRes("MsgText",m_signMessage.getMulti().toString()));

			// FontName
			// FIXME: mtod from r8p9 merge
			String fn = "fontname"; //m_signMessage.getFontName();
			mess.add(new ReqRes("FontName", (fn == null ? "" : fn)));

			// UseOnTime, always true
			mess.add(new ReqRes("UseOnTime",new Boolean(true).toString()));

			// OnTime
			Calendar ontime=calcMsgOnTime();
			mess.add(new ReqRes("OnTime",STime.CalendarToXML(ontime)));

			// UseOffTime
			boolean useofftime = m_signMessage.getDuration() !=null;
			mess.add(new ReqRes("UseOffTime",new Boolean(useofftime).toString()));

			// OffTime, only used if duration is not infinite
			String offtime= (useofftime ? STime.CalendarToXML(calcMsgOffTime(ontime)) : "");
			mess.add(new ReqRes("OffTime",offtime));

			// DisplayTimeMS
			int dt = (int)(SystemAttrEnum.DMS_PAGE_ON_SECS.getFloat() * 1000);
			mess.add(new ReqRes("DisplayTimeMS", new Integer(dt).toString()));

			// Owner
			String owner = m_user != null ? m_user.getFullName() : "";
			mess.add(new ReqRes("Owner", owner));

			// bitmap
			mess.add(new ReqRes("Msg", getBitmapPage(0) + getBitmapPage(1)));

			// send msg
            		mess.getRequest();	// throws IOException

			// parse resp msg
			{
				// get valid flag
				long id=0;
				boolean valid=false;
				String errmsg="";

				try {
					// id
					id = new Long(rr0.getResVal("Id"));

					// isvalid
					valid = new Boolean(rr1.getResVal("IsValid"));

					// error message text
					errmsg = rr1.getResVal("ErrMsg");
					if(!valid && errmsg.length() <= 0)
						errmsg = FAILURE_UNKNOWN;

					System.err.println(
					    "dmslite.OpMessage.PhaseSendTwoPageMessage.poll(msg): parsed msg values: IsValid:"
					    + valid + ".");
				} catch (IllegalArgumentException ex) {
					System.err.println("OpMessage.PhaseSendTwoPageMessage: Malformed XML received:"
					    + ex+", id="+id);
					valid=false;
					errmsg=ex.getMessage();
					handleException(new IOException(errmsg));
				}

				// update 
				complete(mess);

				// parse rest of response
				if (valid) {
					// set new message
					m_dms.setMessageCurrent(
						m_signMessage, m_user);
				} else {
					System.err.println(
					    "OpMessage: response from cmsserver received, ignored because Xml valid field is false, errmsg="+
					    errmsg+",id="+id);
					errorStatus = errmsg;

					// try again
					if (flagFailureShouldRetry(errmsg)) {
						System.err.println("OpMessage: will retry failed operation.");
						return this;

					// give up
					} else {
						// if caws failure, handle it
						if(mess.checkAwsFailure())
							mess.handleAwsFailure("was sending a message.");						
					}
				}
			}

			// this operation is complete
			return null;
		}
	}
}
