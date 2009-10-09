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
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.DmsPgTime;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.aws.AwsMsgs;
import us.mn.state.dot.tms.server.comm.aws.AwsPoller;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.Log;
import us.mn.state.dot.tms.utils.SString;
import us.mn.state.dot.tms.utils.STime;

/**
 * Operation to be performed on a dynamic message sign
 *
 * @author Michael Darter
 * @author Douglas Lau
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

	/** Number of times this operation has been previously attempted */
	protected int m_retry = 0;

	/** Create a new DMS operation */
	public OpDms(int p, DMSImpl d, String opDesc, User user) 
	{
		super(p, d);
		m_dms = d;
		m_opDesc = opDesc;
		m_user = user;
	}

	/** Get the OpDms */
	public OpDms getOpDms() {
		return this;
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
		boolean retry = (phase != null);
		if(retry)
			++m_retry;
		return retry;
	}

	/** random number generator */
	static private Random m_rand = new Random(System.currentTimeMillis());

	/** generate a unique operation id, which is a long, 
	 *  returned as a string. */
	public static String generateId() {
		return new Long(System.currentTimeMillis() + 
			m_rand.nextInt()).toString();
	}

	/** update iris status, called after operation complete */
	public void complete(Message m) {
		updateInterStatus(buildOpStatusCompletionNote(m), true);
	}

	/** Build operation status completion note. */
	public String buildOpStatusCompletionNote(Message m) {
		StringBuilder note = new StringBuilder();
		note.append("Last message at " +
			STime.getCurTimeShortString());
		String delta = SString.doubleToString((
			((double)m.getCompletionTimeMS()) / 1000), 2);
		note.append(" (").append(delta).append(" secs)");
		note.append(".");
		if(m_retry > 0) {
			note.append(String.valueOf(m_retry + 1));
			note.append(" attempts.");
		}
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

	/** Update operation intermediate status in the client.
	 *  @param is String to display.
	 *  @param last True for completion message else false. */
	protected void updateInterStatus(String is, boolean last) {
		if(m_retry > 0 && !last)
			is = "(attempt " + String.valueOf(m_retry + 1) + 
				") " + is;
		ControllerHelper.updateInterStatus(controller, is);
	}

	/** Sends a request to the field controller and reads the response. */
	protected void sendRead(Message mess) throws IOException {
		mess.getRequest(getOpDms());	// throws IOException

		// At this point, either a complete response or an 
		// intermediate status update XML response was read.

		// intermediate status update?
		if(null != mess.searchForReqResItem("InterStatus")) {
			String istatus = mess.searchForReqResItem("Msg");
			if(istatus != null)
				updateInterStatus(istatus, false);
			STime.sleep(2000);
		}
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
			throws IOException 
		{

			// Log.finest("dmslite.OpQueryConfig.PhaseGetConfig.poll(msg) called.");
			assert argmess instanceof Message : "wrong message type";

			Message mess = (Message) argmess;

			// set message attributes as a function of the operation
			setMsgAttributes(mess);

			// build xml request and expected response			
			XmlReqRes xrr = new XmlReqRes("GetDmsConfigReqMsg", 
				"GetDmsConfigRespMsg");
			mess.setName(getOpName());

			String drop = Integer.toString(controller.getDrop());
			xrr.add(new ReqRes("Id", generateId(), new String[] {"Id"}));
			xrr.add(new ReqRes("Address", drop, new String[] {
				"IsValid", "ErrMsg", "signAccess", "model", "make",
				"version", "type", "horizBorder", "vertBorder",
				"horizPitch", "vertPitch", "signHeight",
				"signWidth", "characterHeightPixels",
				"characterWidthPixels", "signHeightPixels",
				"signWidthPixels"
			}));

			// send request and read response
			mess.add(xrr);
			sendRead(mess);

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
				id = new Long(xrr.getResValue("Id"));

				// valid flag
				valid = new Boolean(xrr.getResValue("IsValid"));

				// error message text
				errmsg = xrr.getResValue("ErrMsg");
				if(!valid && errmsg.length() <= 0)
					errmsg = FAILURE_UNKNOWN;

				// update 
				complete(mess);

				// valid message received?
				if(valid) {
					signAccess = xrr.getResValue("signAccess");
					model = xrr.getResValue("model");
					make = xrr.getResValue("make");
					version = xrr.getResValue("version");

					// determine matrix type
					String stype = xrr.getResValue("type");
					if(stype.toLowerCase().contains("full"))
						type = DMSType.VMS_FULL;
					else
						Log.severe("SEVERE: Unknown matrix type read ("+stype+")");

					horizBorder = SString.stringToInt(
						xrr.getResValue("horizBorder"));
					vertBorder = SString.stringToInt(
						xrr.getResValue("vertBorder"));
					horizPitch = SString.stringToInt(
						xrr.getResValue("horizPitch"));
					vertPitch = SString.stringToInt(
						xrr.getResValue("vertPitch"));
					signHeight = SString.stringToInt(
						xrr.getResValue("signHeight"));
					signWidth = SString.stringToInt(
						xrr.getResValue("signWidth"));
					characterHeightPixels = SString.stringToInt(
						xrr.getResValue(
							"characterHeightPixels"));
					characterWidthPixels = SString.stringToInt(
						xrr.getResValue(
							"characterWidthPixels"));
					signHeightPixels = SString.stringToInt(
						xrr.getResValue(
							"signHeightPixels"));
					signWidthPixels = SString.stringToInt(
						xrr.getResValue(
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
