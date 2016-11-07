/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2016  Minnesota Department of Transportation
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
import java.util.Random;
import javax.mail.MessagingException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.PageTimeHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import static us.mn.state.dot.tms.server.comm.dmsxml.DmsXmlPoller.LOG;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.DECISECONDS;
import us.mn.state.dot.tms.utils.Emailer;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.SString;

/**
 * Operation to be performed on a dynamic message sign
 *
 * @author Michael Darter
 * @author Douglas Lau
 * @author Travis Swanston
 */
abstract class OpDms extends OpDevice {

	/** failure message for unknown reasons */
	final static String FAILURE_UNKNOWN = "Failure, unknown reason";

	/** Bitmap width for dmsxml protocol */
	static final int BM_WIDTH = 96;

	/** Bitmap height for dmsxml protocol */
	static final int BM_HEIGHT = 25;

	/** Bitmap page length for dmsxml protocol */
	static final int BM_PGLEN_BYTES = BM_WIDTH * BM_HEIGHT / 8;

	/** User who deployed the message */
	final User m_user;

	/** DMS to operate */
	final DMSImpl m_dms;

	/** operation description */
	private String m_opDesc = "";

	/** Number of times this operation has been previously attempted */
	private int m_retry = 0;

	/** Create a new DMS operation */
	OpDms(PriorityLevel p, DMSImpl d, String opDesc, User user) {
		super(p, d);
		m_dms = d;
		m_opDesc = opDesc;
		m_user = user;
	}

	/** Cleanup the operation. Called by CommThread.doPoll(). */
	@Override
	public void cleanup() {
		if (isSuccess()) {
			m_dms.requestConfigure();
		} else {
			// flag dms not configured
			m_dms.setConfigure(false);
		}
		super.cleanup();
	}

	/** Sign access enumerated type, which is based on the sign 
	 * access field specified in the DMS. */
	enum SignAccess {
		DIALUP_MODEM("dialup", SystemAttrEnum.
			DMSXML_MODEM_OP_TIMEOUT_SECS),
		IP("ip", SystemAttrEnum.DMSXML_OP_TIMEOUT_SECS), 
		UNKNOWN("", SystemAttrEnum.DMSXML_OP_TIMEOUT_SECS);

		/** Parse string for id of type. Assumed to be lowercase. */
		private final String id_desc;

		/** Timeout system attribute, never null. */
		private SystemAttrEnum timeout_sa;

		/** Constructor */
		SignAccess(String d, SystemAttrEnum to) {
			id_desc = d.toLowerCase();
			timeout_sa = to;
		}

		/** Return a sign access type given a string description */
		static SignAccess parse(String d) {
			if(d == null)
				return UNKNOWN;
			d = d.toLowerCase();
			if(d.contains(DIALUP_MODEM.id_desc))
				return DIALUP_MODEM;
			else if(d.contains(IP.id_desc))
				return IP;
			// unknown sign type, this happens when the first 
			// OpQueryConfig message is being sent.
			else
				return UNKNOWN;
		}

		/** Get the timeout (seconds) */
		int timeoutSecs() {
			if(timeout_sa == null)
				return UNKNOWN.timeout_sa.getInt();
			else
				return timeout_sa.getInt();
		}

		/** Return DMS sign access */
		static SignAccess get(DMSImpl d) {
			assert d != null;
			if(d == null)
				return UNKNOWN;
			else
				return parse(d.getSignAccess());
		}
	};

	/** Return name of AWS system */
	public static String awsName() {
		return I18N.get("dms.aws.abbreviation");
	}

	/** Return true if the message is owned by the AWS */
	static boolean ownerIsAws(final String msg_owner) {
		if(msg_owner == null)
			return false;
		return msg_owner.toLowerCase().equals(awsName().toLowerCase());
	}

	/** set message attributes which are a function of the 
	 * operation, sign, etc. */
	void setMsgAttributes(Message m) {
		m.setTimeoutMS(calcTimeoutMS());
	}

	/** Get the timeout for this operation. */
	private int calcTimeoutMS() {
		SignAccess a = SignAccess.get(m_dms);
		int s = a.get(m_dms).timeoutSecs();
		LOG.log("Op timeout is " + s + " secs, SignAccess=" + a + 
			", dms=" + m_dms);
		return s * 1000;
	}

	/** Handle a failed operation.
	  * @param errmsg Error message
	  * @return true if the operation should be retried else false. */
	boolean flagFailureShouldRetry(String errmsg) {
	 	String msg = m_dms.getName();
		if(errmsg == null || errmsg.isEmpty())
			msg += " unknown error.";
		else
			msg += " " + errmsg;

		// trigger error handling, changes status if necessary
		// phase is set to null if no retry should be performed
		handleCommError(EventType.PARSING_ERROR, msg);
		boolean retry = !isDone();
		if(retry)
			++m_retry;
		return retry;
	}

	/** random number generator */
	private static Random m_rand = new Random(System.currentTimeMillis());

	/** generate a unique operation id, which is a long, 
	 *  returned as a string. */
	static String generateId() {
		return new Long(System.currentTimeMillis() + 
			m_rand.nextInt()).toString();
	}

	/** update iris status, called after operation complete */
	void complete(Message m) {
		updateInterStatus(buildOpStatusCompletionNote(m), true);
	}

	/** Build operation status completion note. */
	private String buildOpStatusCompletionNote(Message m) {
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
		m_opDesc = (m_opDesc == null ? 
			"Unnamed operation" : m_opDesc);
		if(m_user == null)
			return m_opDesc;
		return m_opDesc + " (" + m_user.getFullName() + ")";
	}

	/** return true if dms has been configured */
	boolean dmsConfigured() {
		return m_dms.getConfigure();
	}

	/** Return the page on-time. If a value is not found in the MULTI
	 * string, the system default value is returned. */
	Interval determinePageOnInterval(String multi) {
		MultiString ms = new MultiString(multi);
		boolean single = (ms.getNumPages() <= 1);
		Interval dflt_on = defaultPageOnInterval(single);
		Interval[] on_int = ms.pageOnIntervals(dflt_on);
		// extract from 1st page of MULTI
		assert on_int != null && on_int.length > 0;
		Interval pg_1 = on_int[0];
		return PageTimeHelper.validateOnInterval(pg_1, single);
	}

	/** Get the default page on interval */
	private Interval defaultPageOnInterval(boolean single) {
		return (single)
		      ? new Interval(0)
		      : PageTimeHelper.defaultPageOnInterval();
	}

	/** Update operation intermediate status in the client.
	 *  @param is Strings to display, may be null. */
	void updateInterStatus(String[] is) {
		if(is == null || is.length <= 0)
			return;
		for(int i = 0; i < is.length; ++i)
			updateInterStatus(is[i], false);
	}

	/** Update operation intermediate status in the client.
	 *  @param is String to display, may be null.
	 *  @param last True for completion message else false. */
	void updateInterStatus(String is, boolean last) {
		if(is == null || is.isEmpty())
			return;
		// prepend attempt number so user knows this is a retry
		if(m_retry > 0 && !last)
			is = "(attempt " + String.valueOf(m_retry + 1) + 
				") " + is;
		m_dms.setOpStatus(is);
	}

	/** Return an intermediate status XML element */
	private static XmlElem buildInterStatusElem() {
		XmlElem is = new XmlElem(Message.DMSXMLMSGTAG, 
			Message.ISTATUSTAG);
		// response (there is no request)
		is.addRes("Id");
		is.addRes("Msg");
		return is;
	}

	/** Sends a request to the field controller and reads the response. */
	void sendRead(Message mess) throws IOException {

		// add intermediate status element as a possible response
		mess.add(buildInterStatusElem());
		mess.setOperation(this);
		// send and read response, throws IOException
		mess.queryProps();
	}

	/** Return true if the message owner is Reinit */
	static boolean ownerIsReinit(final String o) {
		if (o == null)
			return false;
		else
			return o.toLowerCase().equals("reinit");
	}

	/**
	 * Set the maintenance status string.
	 * @return True if the maintenance status was set and it also changed
	 *         relative to the value stored in the controller.
	 */
	public boolean updateMaintStatus(String o) {
		boolean reinit_detect = SystemAttrEnum.DMSXML_REINIT_DETECT.getBoolean();
		if (!reinit_detect) {
			return false;
		}
		if (ownerIsReinit(o)) {
			String msg = "Power cycle event";
			setMaintStatus(msg);
			return !msg.equals(getControllerMaintStatus());
		} else {
			// NOTE: won't this wipe out existing maint status
			// (from other causes)?:
			setMaintStatus("");
			return false;
		}
	}

	/** Log a message to stderr */
		static private void logStderr(String msg) {
		System.err.println(TimeSteward.currentDateTimeString(true)
			+ "DMSXML: " + msg);
	}

	/** Send power cycle email */
	public void sendMaintenanceEmail() {
		String msg = "IRIS has placed CMS " + m_dms + " into "
			+ "\"maintenance\" mode.  One reason this may have "
			+ "happened is if IRIS has found the sign to be "
			+ "unexpectedly blank, which could indicate that the "
			+ "sign controller's power has been cycled.";
		String subject = "CMS maintenance alert: " + m_dms;
		String host = SystemAttrEnum.EMAIL_SMTP_HOST.getString();
		if (host == null || host.length() <= 0) {
			logStderr("Alert!  DMS reinit detect (" + m_dms
				+ ") email failed: invalid host.  "
				+ "Message: " + msg);
			return;
		}
		String sender = SystemAttrEnum.EMAIL_SENDER_SERVER.getString();
		if (sender == null || sender.length() <= 0) {
			logStderr("Alert!  DMS reinit detect (" + m_dms
				+ ") email failed: invalid sender.  "
				+ "Message: " + msg);
			return;
		}
		String recip =
			SystemAttrEnum.EMAIL_RECIPIENT_DMSXML_REINIT.getString();
		if (recip == null || recip.length() <= 0) {
			logStderr("Alert!  DMS reinit detect (" + m_dms
				+ ") email failed: invalid recipient.  "
				+ "Message: " + msg);
			return;
		}
		try {
			Emailer email = new Emailer(host, sender, recip);
			email.send(subject, msg);
		}
		catch(MessagingException e) {
			logStderr("Alert!  DMS reinit detect (" + m_dms
				+ ") email failed: " + e.getMessage()
				+ ".  Message: " + msg);
		}
	}

	/** Get the maint status message in the controller */
	private String getControllerMaintStatus() {
		return controller.getMaint();
	}

	/** Phase to query the dms config, which is used by subclasses */
	class PhaseGetConfig extends Phase
	{
		/** next phase to execute or null */
		private Phase m_next = null;

		/** constructor */
		private PhaseGetConfig() {}

		/** Constructor
		 *  @param next Phase to execute next else null. */
		PhaseGetConfig(Phase next) {
			m_next = next;
		}

		/** Build XML element:
		 *	<DmsXml><elemname>
		 *		<Id>...</Id>
		 *		<Address>...</Address>
		 *		<MsgText>...</MsgText>
		 *		<UseOnTime>...</UseOnTime>
		 *		<OnTime>...</OnTime>
		 *		<UseOffTime>...</UseOffTime>
	 	 *		<OffTime>...</OffTime>
		 *		<DisplayTimeMS>...</DisplayTimeMS>
		 *		<ActPriority>...</ActPriority>
		 *		<RunPriority>...</RunPriority>
		 *		<Owner>...</Owner>
		 *		<Msg>...</Msg>
		 *	</elemname></DmsXml>
		 */
		private XmlElem buildXmlElem(String elemReqName, 
			String elemResName) 
		{
			XmlElem xrr = new XmlElem(elemReqName, elemResName);

			// request
			xrr.addReq("Id", generateId());
			xrr.addReq("Address", controller.getDrop());

			// response
			xrr.addRes("IsValid");
			xrr.addRes("ErrMsg");
			xrr.addRes("signAccess");
			xrr.addRes("model");
			xrr.addRes("make");
			xrr.addRes("version");
			xrr.addRes("type");
			xrr.addRes("horizBorder");
			xrr.addRes("vertBorder");
			xrr.addRes("horizPitch");
			xrr.addRes("vertPitch");
			xrr.addRes("signHeight");
			xrr.addRes("signWidth");
			xrr.addRes("characterHeightPixels");
			xrr.addRes("characterWidthPixels");
			xrr.addRes("signHeightPixels");
			xrr.addRes("signWidthPixels");

			return xrr;
		}

		/** Parse response.
		 *  @return True to retry the operation else false if done. */
		private boolean parseResponse(Message mess, XmlElem xrr) {
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
				id = xrr.getResLong("Id");

				// valid flag
				valid = xrr.getResBoolean("IsValid");

				// error message text
				errmsg = xrr.getResString("ErrMsg");
				if(!valid && errmsg.length() <= 0)
					errmsg = FAILURE_UNKNOWN;

				// update 
				complete(mess);

				// valid message received?
				if(valid) {
					signAccess = xrr.getResString(
						"signAccess");
					model = xrr.getResString("model");
					make = xrr.getResString("make");
					version = xrr.getResString("version");

					// determine matrix type
					String stype = xrr.getResString("type");
					if(stype.toLowerCase().contains(
						"full")) 
					{
						type = DMSType.VMS_FULL;
					} else {
						LOG.log("SEVERE: Unknown "
							+ "matrix type read (" 
							+ stype + ")");
					}

					horizBorder = xrr.getResInt(
						"horizBorder");
					vertBorder = xrr.getResInt(
						"vertBorder");
					horizPitch = xrr.getResInt(
						"horizPitch");
					vertPitch = xrr.getResInt(
						"vertPitch");
					signHeight = xrr.getResInt(
						"signHeight");
					signWidth = xrr.getResInt(
						"signWidth");
					characterHeightPixels = xrr.getResInt(
						"characterHeightPixels");
					characterWidthPixels = xrr.getResInt(
						"characterWidthPixels");
					signHeightPixels = xrr.getResInt(
						"signHeightPixels");
					signWidthPixels = xrr.getResInt(
						"signWidthPixels");
				}
			} catch (IllegalArgumentException ex) {
				LOG.log("PhaseGetConfig: Malformed XML " +
					"received:" + ex + ", id=" + id);
				valid = false;
				errmsg = ex.getMessage();
				handleCommError(EventType.PARSING_ERROR, 
					errmsg);
			}

			// set config values these values are displayed in 
			// the DMS dialog, Configuration tab
			if(valid) {
				setErrorStatus("");
				m_dms.setModel(model);
				m_dms.setSignAccess(signAccess); // ip or dialup
				m_dms.setMake(make);
				m_dms.setVersionNotify(version);
				m_dms.setDmsType(type);
				m_dms.setHorizontalBorder(horizBorder);// in mm
				m_dms.setVerticalBorder(vertBorder);   // in mm
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
				m_dms.setCharHeightPixels(
					characterHeightPixels);
				m_dms.setCharWidthPixels(
					characterWidthPixels);

			// failure
			} else {
				LOG.log("WARNING: response from " +
					"SensorServer received, ignored " +
					"because Xml valid field is false, " +
					"errmsg=" + errmsg);
				setErrorStatus(errmsg);

				// try again
				if(flagFailureShouldRetry(errmsg)) {
					LOG.log("PhaseGetConfig: will " +
						"retry failed op.");
					return true;
				}
			}

			// execute subsequent phase
			return false;
		}

		/**
		 * Get the DMS configuration. This phase is used by subclassed
		 * operations if the DMS configuration has not been requested.
		 * Note, the type of exception throw here determines
		 * if the messenger reopens the connection on failure.
		 * @see CommThread#doPoll()
		 * @see Messenger#handleCommError()
		 * @see Messenger#shouldReopen()
		 */
		protected Phase poll(CommMessage argmess)
			throws IOException 
		{
			Message mess = (Message) argmess;

			// set message attributes as a function of the op
			setMsgAttributes(mess);

			// build xml request and expected response			
			mess.setName(getOpName());
			XmlElem xrr = buildXmlElem("GetDmsConfigReqMsg", 
				"GetDmsConfigRespMsg");

			// send request and read response
			mess.add(xrr);
			sendRead(mess);

			if(parseResponse(mess, xrr))
				return this;
			else
				return m_next;
		}
	}
}
