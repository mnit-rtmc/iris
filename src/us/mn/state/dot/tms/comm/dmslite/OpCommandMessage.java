/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Operation to command a new message on a DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpCommandMessage extends OpDms {

	/** Maximum message priority */
	static protected final int MAX_MESSAGE_PRIORITY = 255;

	/** Flag to avoid phase loops */
	protected boolean modify = true;

	/** Sign message */
	protected final SignMessage m_message;

	/**
	 * Message CRC
	 *
	 * @param d
	 * @param m
	 */

	// protected int messageCRC;

	/** Create a new DMS command message object */
	public OpCommandMessage(DMSImpl d, SignMessage m) {
		super(COMMAND, d);
		m_message = m;
		System.err.println(
		    "dmslite.OpCommandMessage.OpCommandMessage() called. Msg="
		    + m + ",numpages=" + m_message.getNumPages());
	}

	/**
	 * Create the first real phase of the operation
	 *
	 * @return
	 */
	protected Phase phaseOne() {
		int np = m_message.getNumPages();

		if (np <= 0) {
			return null;
		} else if (np == 1) {
			return new PhaseSendOnePageMessage();
		} else if (np == 2) {
			return new PhaseSendTwoPageMessage();
		}

		System.err.println(
		    "WARNING: bogus number of pages (" + np
		    + ") in dmslite.OpCommandMessage.OpCommandMessage(). Ignored.");

		return null;
	}

	/** 
	  * Calculate message on time, which is the time now. In the future,
	  * if IRIS supports specifying a start time, this may be calculated 
	  * to be some future time.
	  *
	  * @return On time
	  */
	protected Calendar calcMsgOnTime()
	{
		return(new GregorianCalendar());
	}

	/** Calculate message off time, which is the start time + duration.
	 *  This method should not be called if duration is infinite.
	 */
	protected Calendar calcMsgOffTime(Calendar ontime)
	{
		int mins=this.m_message.getDuration();
		assert mins!=SignMessage.DURATION_INFINITE;
		Calendar offtime=(Calendar)ontime.clone();
		offtime.add(Calendar.MINUTE,mins);
		return(offtime);
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
		 * Set the status to modify request
		 *
		 * @param argmess
		 *
		 * @return
		 *
		 * @throws IOException
		 */
		protected Phase poll(AddressedMessage argmess)
			throws IOException {
			System.err.println(
			    "dmslite.OpCommandMessage.PhaseSendOnePageMessage.poll(msg) called.");
			assert argmess instanceof Message :
			       "wrong message type";

			Message mess = (Message) argmess;

			// sanity check
			if (m_message.getBitmap().getBitmap().length != 300) {
				System.err.println(
				    "WARNING: message wrong size in PhaseSendOnePageMessage.");

				return null;
			}

			/*  build req msg and expected response
			 *     <DmsLite>
			 *        <SetSnglPgReqMsg>
			 *           <Address>...</Address>
			 *           <MsgText>...</MsgText>             multistring cms message text
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

			mess.setName(reqname);
			mess.setReqMsgName(reqname);
			mess.setRespMsgName(resname);

			String addr =
				new Integer((int) m_dms.getController()
					.getDrop()).toString();
			ReqRes rr1 = new ReqRes("Address", addr,
						new String[] { "IsValid" });

			mess.add(rr1);

			// MsgText
			mess.add(new ReqRes("MsgText",
					    m_message.getMulti().toString()));

			// UseOnTime, always true
			mess.add(new ReqRes("UseOnTime",new Boolean(true).toString()));

			// OnTime
			Calendar ontime=calcMsgOnTime();
			mess.add(new ReqRes("OnTime",Time.CalendarToXML(ontime)));

			// UseOffTime
			boolean useofftime=m_message.getDuration()!=SignMessage.DURATION_INFINITE;
			mess.add(new ReqRes("UseOffTime",new Boolean(useofftime).toString()));

			// OffTime, only used if duration is not infinite
			String offtime= (useofftime ? Time.CalendarToXML(calcMsgOffTime(ontime)) : "");
			mess.add(new ReqRes("OffTime",offtime));

			// Owner
			mess.add(new ReqRes("Owner", m_message.getOwner()));

			// msg
			byte[] bitmaparray = m_message.getBitmap().getBitmap();
			String msg = prepareBitmap(bitmaparray);
			mess.add(new ReqRes("Msg", msg));

			// send msg
			mess.getRequest();

			// parse resp msg
			{

				// get valid flag
				boolean valid;

				try {
					valid = new Boolean(
					    rr1.getResVal("IsValid"));
					System.err.println(
					    "dmslite.OpCommandMessage.PhaseSendOnePageMessage.poll(msg): parsed msg values: IsValid:"
					    + valid + ".");
				} catch (IllegalArgumentException ex) {
					System.err.println(
					    "dmslite.OpCommandMessage.PhaseSendOnePageMessage.poll(msg): Malformed XML received in OpQueryDms:"
					    + ex);

					throw ex;
				}

				// parse rest of response
				if (valid) {

					// set new message
					m_dms.setActiveMessage(m_message);
				} else {
					System.err.println(
					    "OpQueryDms: invalid response from cmsserver received, ignored.");
				}
			}

			// this operation is complete
			return null;
		}
	}

	/** prepare a bitmap to send via xml */
	protected String prepareBitmap(byte[] a)
	{
		//String s=Convert.toHexString(Convert.reverseByte(a));
		String s=Convert.toHexString(a);
		return(s);
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
		 * Set the status to modify request
		 *
		 * @param argmess
		 *
		 * @return
		 *
		 * @throws IOException
		 */
		protected Phase poll(AddressedMessage argmess)
			throws IOException {
			System.err.println(
			    "dmslite.OpCommandMessage.PhaseSendTwoPageMessage.poll(msg) called.");
			assert argmess instanceof Message :
			       "wrong message type";

			Message mess = (Message) argmess;

			// sanity check
			if (m_message.getBitmap().getBitmap().length != 600) {
				System.err.println(
				    "WARNING: message wrong size in PhaseSendTwoPageMessage: m_message.length="
				    + m_message.getBitmap().getBitmap().length);

				// return null;
			}

			    /**
			     * Return a newly created SignViewOperation using a dmslite xml msg string.
			     * The xml string is expected to be in the following format.
			     *
			     *    <DmsLite>
			     *       <SetMultiplePageReqMsg>
			     *          <Address>...</Address>
			     *          <MsgText>...</MsgText>                  multistring cms message text
			     *          <UseOnTime>...</UseOnTime>         	true to use on time, else now
			     *          <OnTime>...</OnTime>             	message on time
			     *          <UseOffTime>...</UseOffTime>       	true to use off time, else indefinite
			     *          <OffTime>...</OffTime>           	message off time
			     *          <DisplayTimeMS>...<DisplayTimeMS>       message display time
			     *          <Owner>...</Owner>                      the message author
			     *          <Msg>...</Msg>
			     *       </SetMultiplePageReqMsg>
			     *    </DmsLite>
			     */


			ReqRes rr1;
			{
				final String reqname = "SetMultPgReqMsg";
				final String resname = "SetMultPgRespMsg";

				mess.setName(reqname);
				mess.setReqMsgName(reqname);
				mess.setRespMsgName(resname);

				String addr =
					new Integer((int) m_dms.getController()
						.getDrop()).toString();

				rr1 = new ReqRes("Address", addr,
						 new String[] { "IsValid" });
				mess.add(rr1);
			}

			// MsgText
			mess.add(new ReqRes("MsgText",
					    m_message.getMulti().toString()));

			// UseOnTime, always true
			mess.add(new ReqRes("UseOnTime",new Boolean(true).toString()));

			// OnTime
			Calendar ontime=calcMsgOnTime();
			mess.add(new ReqRes("OnTime",Time.CalendarToXML(ontime)));

			// UseOffTime
			boolean useofftime=m_message.getDuration()!=SignMessage.DURATION_INFINITE;
			mess.add(new ReqRes("UseOffTime",new Boolean(useofftime).toString()));

			// OffTime, only used if duration is not infinite
			String offtime= (useofftime ? Time.CalendarToXML(calcMsgOffTime(ontime)) : "");
			mess.add(new ReqRes("OffTime",offtime));

			// DisplayTimeMS
			mess.add(new ReqRes("DisplayTimeMS", "2000")); //FIXME 

			// Owner
			mess.add(new ReqRes("Owner", m_message.getOwner()));

			// msg (the bitmap)
			{
				// pg 1
				BitmapGraphic bg1 = m_message.getBitmap(0);
				assert bg1 != null;
				byte[] bitmaparraypg1 = bg1.getBitmap();
				String msgpg1 = prepareBitmap(bitmaparraypg1);

				// pg 2
				BitmapGraphic bg2 = m_message.getBitmap(1);
				assert bg2 != null;
				byte[] bitmaparraypg2 = bg2.getBitmap();
				String msgpg2 = prepareBitmap(bitmaparraypg2);

				mess.add(new ReqRes("Msg", msgpg1 + msgpg2));
			}

			// send msg
			mess.getRequest();

			// parse resp msg
			{

				// get valid flag
				boolean valid;

				try {
					valid = new Boolean(
					    rr1.getResVal("IsValid"));
					System.err.println(
					    "dmslite.OpCommandMessage.PhaseSendTwoPageMessage.poll(msg): parsed msg values: IsValid:"
					    + valid + ".");
				} catch (IllegalArgumentException ex) {
					System.err.println(
					    "dmslite.OpCommandMessage.PhaseSendTwoPageMessage.poll(msg): Malformed XML received in OpQueryDms:"
					    + ex);

					throw ex;
				}

				// parse rest of response
				if (valid) {

					// set new message
					m_dms.setActiveMessage(m_message);

				} else {
					System.err.println(
					    "OpQueryDms: invalid response from cmsserver received, ignored.");
				}
			}

			// this operation is complete
			return null;
		}
	}
}
