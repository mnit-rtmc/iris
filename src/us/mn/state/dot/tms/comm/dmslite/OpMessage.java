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
import us.mn.state.dot.tms.MsgActPriority;
import us.mn.state.dot.tms.MsgActPriorityD10;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.utils.STime;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Operation to send a new message to a DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpMessage extends OpDms {

	static final String OPNAME="OpMessage";

	/** Maximum message priority */
	static protected final int MAX_MESSAGE_PRIORITY = 255;

	/** Flag to avoid phase loops */
	protected boolean modify = true;

	/** Sign message */
	protected final SignMessage m_signMessage;

	/** Create a new DMS command message object */
	public OpMessage(DMSImpl d, SignMessage m) {
		super(COMMAND, d);
		m_signMessage = m;
		System.err.println(
		    "dmslite.OpMessage.OpMessage() called. Msg="
		    + m + ",numpages=" + m_signMessage.getNumPages());
	}

	/** return description of operation, which is displayed in the client */
	public String getOperationDescription() {
		return "Sending new message";
	}

	/**
	 * Create the first real phase of the operation
	 *
	 * @return
	 */
	protected Phase phaseOne() {
		int np = m_signMessage.getNumPages();

		if (np <= 0) {
			return null;
		} else if (np == 1) {
			return new PhaseSendOnePageMessage();
		} else if (np == 2) {
			return new PhaseSendTwoPageMessage();
		}

		System.err.println(
		    "WARNING: bogus number of pages (" + np
		    + ") in dmslite.OpMessage.OpMessage(). Ignored.");

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
		int mins=this.m_signMessage.getDuration();
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
		 * Set the status to modify request. Called by Operation.poll().
		 * @param argmess
		 * @return next Phase to execute else null.
		 * @throws IOException
		 */
		protected Phase poll(AddressedMessage argmess)
			throws IOException {
			System.err.println(
			    "dmslite.OpMessage.PhaseSendOnePageMessage.poll(msg) called.");
			assert argmess instanceof Message :
			       "wrong message type";

			Message mess = (Message) argmess;

			// sanity check
			if (m_signMessage.getBitmap(0).length()!=300) {
				System.err.println(
				    "WARNING: bitmap pg 1 wrong size in PhaseSendOnePageMessage: m_signMessage.length="
				    + m_signMessage.getBitmap(0).length()+", msg="+m_signMessage.toString());
			}

			// set message attributes as a function of the operation
			setMsgAttributes(mess);

			/*  build req msg and expected response
			 *     <DmsLite>
			 *        <SetSnglPgReqMsg>
			 *           <Id>...</Id>
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

			String addr = Integer.toString(controller.getDrop());
			ReqRes rr0 = new ReqRes("Id", generateId(), new String[] {"Id"});
			ReqRes rr1 = new ReqRes("Address", addr,new String[] { "IsValid", "ErrMsg" });
			mess.add(rr0);
			mess.add(rr1);

			// MsgText
			mess.add(new ReqRes("MsgText",m_signMessage.getMulti().toString()));

			// UseOnTime, always true
			mess.add(new ReqRes("UseOnTime",new Boolean(true).toString()));

			// OnTime
			Calendar ontime=calcMsgOnTime();
			mess.add(new ReqRes("OnTime",STime.CalendarToXML(ontime)));

			// UseOffTime
			boolean useofftime=m_signMessage.getDuration()!=SignMessage.DURATION_INFINITE;
			mess.add(new ReqRes("UseOffTime",new Boolean(useofftime).toString()));

			// OffTime, only used if duration is not infinite
			String offtime= (useofftime ? STime.CalendarToXML(calcMsgOffTime(ontime)) : "");
			mess.add(new ReqRes("OffTime",offtime));

			// Owner
			mess.add(new ReqRes("Owner", m_signMessage.getOwner()));

			// msg
			byte[] bitmaparray = m_signMessage.getBitmap().getBitmap();
			String msg = prepareBitmap(bitmaparray);
			mess.add(new ReqRes("Msg", msg));

			// send msg to field controller
            		mess.getRequest();	// throws IOException

			// parse resp msg
			{

				// get valid flag
				long id = 0;
				boolean valid=false;
				String errmsg="";

				try {
					// id
					id = new Long(rr0.getResVal("Id"));

					// valid flag
					valid = new Boolean(rr1.getResVal("IsValid"));
					System.err.println(
					    "dmslite.OpMessage.PhaseSendOnePageMessage.poll(msg): parsed msg values: IsValid:"
					    + valid + ".");

					// error message text
					rr1.getResVal("ErrMsg");
					if (!valid && errmsg.length()<1)
						errmsg="request failed";

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
					m_dms.setActiveMessage(m_signMessage);
				} else {
					System.err.println(
					    "OpMessage: cmsserver response received, IsValid is false, errmsg="+
					    errmsg+", id="+id);
					m_dms.setStatus(OPNAME+": "+errmsg);

					// try again
					if (flagFailureShouldRetry(errmsg)) {
						System.err.println("OpMessage: will retry failed operation.");
						return this;

					// give up
					} else {
						// if caws failure, handle it
						if( mess.checkCAWSFailure() )
							mess.handleCAWSFailure("was sending a message.");						
					}
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

			// sanity check
			if (m_signMessage.getBitmap(0).length()!=300) {
				System.err.println(
				    "WARNING: bitmap pg 1 wrong size in PhaseSendTwoPageMessage: m_signMessage.length="
				    + m_signMessage.getBitmap(0).length()+", msg="+m_signMessage.toString());
			}
			if (m_signMessage.getBitmap(1).length()!=300) {
				System.err.println(
				    "WARNING: bitmap pg 2 wrong size in PhaseSendTwoPageMessage: m_signMessage.length="
				    + m_signMessage.getBitmap(1).length()+", msg="+m_signMessage.toString());
			}

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
				final String reqname = "SetMultPgReqMsg";
				final String resname = "SetMultPgRespMsg";

				mess.setName(reqname);
				mess.setReqMsgName(reqname);
				mess.setRespMsgName(resname);

				// id
				rr0 = new ReqRes("Id", generateId(), new String[] {"Id"});
				mess.add(rr0);

				// drop
				String addr = Integer.toString(controller.getDrop());
				rr1 = new ReqRes("Address", addr,new String[] { "IsValid", "ErrMsg" });
				mess.add(rr1);
			}

			// MsgText
			mess.add(new ReqRes("MsgText",m_signMessage.getMulti().toString()));

			// UseOnTime, always true
			mess.add(new ReqRes("UseOnTime",new Boolean(true).toString()));

			// OnTime
			Calendar ontime=calcMsgOnTime();
			mess.add(new ReqRes("OnTime",STime.CalendarToXML(ontime)));

			// UseOffTime
			boolean useofftime=m_signMessage.getDuration()!=SignMessage.DURATION_INFINITE;
			mess.add(new ReqRes("UseOffTime",new Boolean(useofftime).toString()));

			// OffTime, only used if duration is not infinite
			String offtime= (useofftime ? STime.CalendarToXML(calcMsgOffTime(ontime)) : "");
			mess.add(new ReqRes("OffTime",offtime));

			// DisplayTimeMS
			int MSG_DISPLAY_MSG_TIME_MS = 2000; //FIXME: use system value specified via dialog box
			mess.add(new ReqRes("DisplayTimeMS", new Integer(MSG_DISPLAY_MSG_TIME_MS).toString()));

			// Owner
			mess.add(new ReqRes("Owner", m_signMessage.getOwner()));

			// msg (the bitmap)
			{
				// pg 1
				BitmapGraphic bg1 = m_signMessage.getBitmap(0);
				assert bg1 != null;
				byte[] bitmaparraypg1 = bg1.getBitmap();
				String msgpg1 = prepareBitmap(bitmaparraypg1);

				// pg 2
				BitmapGraphic bg2 = m_signMessage.getBitmap(1);
				assert bg2 != null;
				byte[] bitmaparraypg2 = bg2.getBitmap();
				String msgpg2 = prepareBitmap(bitmaparraypg2);

				mess.add(new ReqRes("Msg", msgpg1 + msgpg2));
			}

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
					errmsg=rr1.getResVal("ErrMsg");
					if (!valid && errmsg.length()<1)
						errmsg="request failed";

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
					m_dms.setActiveMessage(m_signMessage);

				} else {
					System.err.println(
					    "OpMessage: response from cmsserver received, ignored because Xml valid field is false, errmsg="+
					    errmsg+",id="+id);
					m_dms.setStatus(OPNAME+": "+errmsg);

					// try again
					if (flagFailureShouldRetry(errmsg)) {
						System.err.println("OpMessage: will retry failed operation.");
						return this;

					// give up
					} else {
						// if caws failure, handle it
						if( mess.checkCAWSFailure() )
							mess.handleCAWSFailure("was sending a message.");						
					}
				}
			}

			// this operation is complete
			return null;
		}
	}
}
