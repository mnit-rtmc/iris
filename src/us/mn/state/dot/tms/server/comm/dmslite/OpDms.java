/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2009  Minnesota Department of Transportation
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
import java.util.Random;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DmsPgTime;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.aws.AwsMsgs;
import us.mn.state.dot.tms.server.comm.aws.AwsPoller;
import us.mn.state.dot.tms.server.event.EventType;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.Log;
import us.mn.state.dot.tms.utils.SString;
import us.mn.state.dot.tms.utils.STime;

/**
 * Operation to be performed on a dynamic message sign
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
abstract public class OpDms extends OpDevice {

	/** failure message for unknown reasons */
	final static String FAILURE_UNKNOWN = "Failure, unknown reason";

	/** Bitmap width for dmslite protocol */
	static protected final int BM_WIDTH = 96;

	/** Bitmap height for dmslite protocol */
	static protected final int BM_HEIGHT = 25;

	/** Bitmap page length for dmslite protocol */
	static protected final int BM_PGLEN_BYTES = BM_WIDTH * BM_HEIGHT / 8;

	/** User who deployed the message */
	protected final User m_user;

	/** DMS to operate */
	protected final DMSImpl m_dms;

	/** operation description */
	private String m_opDesc = "";

	/** Create a new DMS operation */
	public OpDms(int p, DMSImpl d, String opDesc, User user) 
	{
		super(p, d);
		m_dms = d;
		m_opDesc = opDesc;
		m_user = user;
	}

	/** get operation name */
	public String getOpName() {
		return getClass().getName();
	}

	/** Get the error retry threshold for a given SignMessage. */
	public int getRetryThreshold(SignMessage sm) {
		// if message is from AWS, use different retry threshold
		if(DMSMessagePriority.fromOrdinal(sm.getRunTimePriority()) == 
		   DMSMessagePriority.AWS)
		{
			return AwsMsgs.getRetryThreshold();
		} else
			return super.getRetryThreshold();
	}

	/** Cleanup the operation, which is called by MessagePoller.doPoll() 
	 *  if an operation is successful. */
	public void cleanup() {
		if(success) {
			m_dms.requestConfigure();
		} else {
			// flag dms not configured
			m_dms.setConfigure(false);
		}
		super.cleanup();
	}

	/** sign access type */
	public enum SignAccessType {DIALUPMODEM, WIZARD, UNKNOWN};

	/** return DMS sign access type */
	public static SignAccessType getSignAccessType(DMSImpl dms) {
		assert dms != null;
		if(dms == null)
			return SignAccessType.UNKNOWN;
		String a = dms.getSignAccess();
		if(a == null)
			return SignAccessType.UNKNOWN;
		else if(a.toLowerCase().contains("modem"))
			return SignAccessType.DIALUPMODEM;
		else if(a.toLowerCase().contains("wizard"))
			return SignAccessType.WIZARD;
		// unknown sign type, this happens when the first 
		// OpQueryConfig message is being sent.
		return SignAccessType.UNKNOWN;
	}

	/** Return true if the message is owned by the AWS */
	public static boolean ownerIsAws(final String msg_owner) {
		if(msg_owner == null)
			return false;
		final String awsName = AwsPoller.awsName();
		return msg_owner.toLowerCase().equals(awsName.toLowerCase());
	}

	/** return the timeout for this operation */
	public int calcTimeoutMS() {
		return getTimeoutSecs() * 1000;
	}

	/** Get the timeout for this operation (seconds) */
	protected int getTimeoutSecs() {
		assert m_dms != null;
		SignAccessType at = getSignAccessType(m_dms);
		if(at == SignAccessType.DIALUPMODEM) {
			int secs = SystemAttrEnum.DMSLITE_MODEM_OP_TIMEOUT_SECS.
				getInt();
			Log.finest("connection type is modem" +
				", dms=" + m_dms.toString() +
				", timeout secs=" + secs);
			return secs;
		} else if(at == SignAccessType.WIZARD) {
			int secs = SystemAttrEnum.DMSLITE_OP_TIMEOUT_SECS.
				getInt();
			Log.finest("connection type is wizard" +
				", dms=" + m_dms.toString() +
				", timeout secs=" + secs);
			return secs;
		} else {
			// if unknown access type, this happens when the first 
			// OpQueryConfig message is being sent, so a default 
			// timeout should be used.
			return SystemAttrEnum.DMSLITE_OP_TIMEOUT_SECS.getInt();
		}
	}

	/** set message attributes which are a function of the 
	 *  operation, sign, etc. */
	public void setMsgAttributes(Message m) {
		m.setTimeoutMS(this.calcTimeoutMS());
	}

	/** Handle a failed operation.
	  * @param errmsg Error message
	  * @return true if the operation should be retried else false. */
	protected boolean flagFailureShouldRetry(String errmsg) {
	 	String msg = m_dms.getName();
		if(errmsg == null || errmsg.isEmpty())
			msg += " unknown error.";
		else
			msg += " " + errmsg;

		// trigger error handling, changes status if necessary
		// phase is set to null if no retry should be performed
		handleCommError(EventType.PARSING_ERROR, msg);
		return phase != null;
	}

	/** random number generator */
	static private Random m_rand = new Random(System.currentTimeMillis());

	/** generate a unique operation id, which is a long, returned as a string */
	public static String generateId() {
		return new Long(System.currentTimeMillis()+m_rand.nextInt()).toString();
	}

	/** update iris status, called after operation complete */
	public void complete(Message m) {
		m_dms.setUserNote(buildUserNote(m));
	}

	/** Build user note */
	public String buildUserNote(Message m) {
		StringBuilder note = new StringBuilder();
		note.append("Last operation at " +
			STime.getCurTimeShortString());
		String delta = SString.doubleToString((
			((double)m.getCompletionTimeMS()) / 1000), 2);
		note.append(" (").append(delta).append(" secs)");
		note.append(".");
		return note.toString();
	}

	/** return description of operation */
	public String getOperationDescription() {
		m_opDesc = (m_opDesc == null ? "Unnamed operation" : m_opDesc);
		if(m_user == null)
			return m_opDesc;
		return m_opDesc + " (" + m_user.getFullName() + ")";
	}

	/** return true if dms has been configured */
	public boolean dmsConfigured() {
		return m_dms.getConfigure();
	}

	/** Return the page on-time. If a value is not found in the MULTI
	 *  string, the system default value is returned. */
	protected DmsPgTime determinePageOnTime(String multi) {
		MultiString ms = new MultiString(multi);
		boolean singlepg = (ms.getNumPages() <= 1);
		// extract from 1st page of MULTI
		int[] pont = ms.getPageOnTimes(
			DmsPgTime.getDefaultOn(singlepg).toTenths());
		DmsPgTime ret;
		if(pont != null && pont.length > 0)
			ret = new DmsPgTime(pont[0]);
		else
			ret = DmsPgTime.getDefaultOn(singlepg);
		return DmsPgTime.validateOnTime(ret, singlepg);
	}

	/** Set an error message. The field errStatus is defined in
	 *  ControllerOperation and assigned to the controller in 
	 *  ControllerOperation.cleanup(). */
	protected void setErrorMsg(String msg) {
		errorStatus = msg;
	}

	/** Phase to query the dms config, which is used by subclasses */
	protected class PhaseGetConfig extends Phase
	{
		/** next phase to execute or null */
		private Phase m_next = null;

		/** constructor */
		protected PhaseGetConfig() {}

		/** 
		 *  constructor
		 *  @param next Phase to execute after this phase else null.
		 */
		protected PhaseGetConfig(Phase next) {
			m_next = next;
		}

		/**
		 * Get the DMS configuration. This phase is used by subclassed
		 * operations if the DMS configuration has not been requested.
		 * Note, the type of exception throw here determines
		 * if the messenger reopens the connection on failure.
		 * @see MessagePoller#doPoll()
		 * @see Messenger#handleCommError()
		 * @see Messenger#shouldReopen()
		 */
		protected Phase poll(AddressedMessage argmess)
			throws IOException {

			// Log.finest("dmslite.OpQueryConfig.PhaseGetConfig.poll(msg) called.");
			assert argmess instanceof Message : "wrong message type";

			Message mess = (Message) argmess;

			// set message attributes as a function of the operation
			setMsgAttributes(mess);

			// build req msg
			mess.setName(getOpName());
			mess.setReqMsgName("GetDmsConfigReqMsg");
			mess.setRespMsgName("GetDmsConfigRespMsg");

			String drop = Integer.toString(controller.getDrop());
			ReqRes rr0 = new ReqRes("Id", generateId(), new String[] {"Id"});
			ReqRes rr1 = new ReqRes("Address", drop, new String[] {
				"IsValid", "ErrMsg", "signAccess", "model", "make",
				"version", "type", "horizBorder", "vertBorder",
				"horizPitch", "vertPitch", "signHeight",
				"signWidth", "characterHeightPixels",
				"characterWidthPixels", "signHeightPixels",
				"signWidthPixels"
			});
			mess.add(rr0);
			mess.add(rr1);

			// send msg
            		mess.getRequest();	// throws IOException

			// parse resp msg
			long id = 0;
			boolean valid = false;
			String errmsg = "";
			String model = "";
			String signAccess = "";
			String make = "";
			String version = "";
			DMSType type = DMSType.VMS_FULL;
			int horizBorder = 0;
			int vertBorder = 0;
			int horizPitch = 0;
			int vertPitch = 0;
			int signHeight = 0;
			int signWidth = 0;
			int characterHeightPixels = 0;
			int characterWidthPixels = 0;
			int signHeightPixels = 0;
			int signWidthPixels = 0;

			try {
				// id
				id = new Long(rr0.getResVal("Id"));

				// valid flag
				valid = new Boolean(rr1.getResVal("IsValid"));

				// error message text
				errmsg = rr1.getResVal("ErrMsg");
				if(!valid && errmsg.length() <= 0)
					errmsg = FAILURE_UNKNOWN;

				// update 
				complete(mess);

				// valid message received?
				if(valid) {
					signAccess = rr1.getResVal("signAccess");
					model = rr1.getResVal("model");
					make = rr1.getResVal("make");
					version = rr1.getResVal("version");

					// determine matrix type
					String stype = rr1.getResVal("type");
					if(stype.toLowerCase().contains("full"))
						type = DMSType.VMS_FULL;
					else
						Log.severe("SEVERE: Unknown matrix type read ("+stype+")");

					horizBorder = SString.stringToInt(
						rr1.getResVal("horizBorder"));
					vertBorder = SString.stringToInt(
						rr1.getResVal("vertBorder"));
					horizPitch = SString.stringToInt(
						rr1.getResVal("horizPitch"));
					vertPitch = SString.stringToInt(
						rr1.getResVal("vertPitch"));
					signHeight = SString.stringToInt(
						rr1.getResVal("signHeight"));
					signWidth = SString.stringToInt(
						rr1.getResVal("signWidth"));
					characterHeightPixels = SString.stringToInt(
						rr1.getResVal(
							"characterHeightPixels"));
					characterWidthPixels = SString.stringToInt(
						rr1.getResVal(
							"characterWidthPixels"));
					signHeightPixels = SString.stringToInt(
						rr1.getResVal(
							"signHeightPixels"));
					signWidthPixels = SString.stringToInt(
						rr1.getResVal(
							"signWidthPixels"));

					// Log.finest("PhaseGetConfig.poll(msg) parsed msg values: valid:"+
					// valid+", model:"+model+", make:"+make+"...etc.");
				}
			} catch (IllegalArgumentException ex) {
				Log.severe("PhaseGetConfig: Malformed XML received:"+ex+", id="+id);
				valid = false;
				errmsg = ex.getMessage();
				handleCommError(EventType.PARSING_ERROR,errmsg);
			}

			// set config values
			// these values are displayed in the DMS dialog, Configuration tab
			if(valid) {
				setErrorMsg("");
				m_dms.setModel(model);
				m_dms.setSignAccess(signAccess);    // wizard, modem
				m_dms.setMake(make);
				m_dms.setVersion(version);
				m_dms.setDmsType(type);
				m_dms.setHorizontalBorder(horizBorder);    // in mm
				m_dms.setVerticalBorder(vertBorder);    // in mm
				m_dms.setHorizontalPitch(horizPitch);
				m_dms.setVerticalPitch(vertPitch);

				// values not set for these
				m_dms.setLegend("sign legend");
				m_dms.setBeaconType("beacon type");
				m_dms.setTechnology("sign technology");

				// note, these must be defined for comboboxes
				// in the "Compose message" control to appear
				m_dms.setFaceHeight(signHeight);    // mm
				m_dms.setFaceWidth(signWidth);      // mm
				m_dms.setHeightPixels(signHeightPixels);
				m_dms.setWidthPixels(signWidthPixels);
				// NOTE: these must be set last
				m_dms.setCharHeightPixels(characterHeightPixels);
				m_dms.setCharWidthPixels(characterWidthPixels);

			// failure
			} else {
				Log.warning(
					"PhaseGetConfig: response from SensorServer received, " +
					"ignored because Xml valid field is false, errmsg=" + errmsg);
				setErrorMsg(errmsg);

				// try again
				if(flagFailureShouldRetry(errmsg)) {
					Log.finest("PhaseGetConfig: will retry failed operation");
					return this;
				}
			}

			// if non-null, execute subsequent phase
			return m_next;
		}
	}
}
