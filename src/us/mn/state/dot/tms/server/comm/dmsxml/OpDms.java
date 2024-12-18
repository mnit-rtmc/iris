/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2024  Minnesota Department of Transportation
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
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.CommConfig;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.PageTimeHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.SignConfigImpl;
import us.mn.state.dot.tms.server.SignDetailImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import static us.mn.state.dot.tms.server.comm.dmsxml.DmsXmlPoller.LOG;
import us.mn.state.dot.tms.units.Interval;
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

	/** DMS to operate */
	final DMSImpl m_dms;

	/** operation description */
	private String m_opDesc = "";

	/** Create a new DMS operation */
	OpDms(PriorityLevel p, DMSImpl d, String opDesc) {
		super(p, d);
		m_dms = d;
		m_opDesc = opDesc;
	}

	/** DMS status */
	private final JSONObject status = new JSONObject();

	/** Put an object into DMS status */
	protected final void putStatus(String key, Object value) {
		try {
			status.putOnce(key, value);
		}
		catch (JSONException e) {
			LOG.log("putStatus: " + e.getMessage() + ", " + key);
		}
	}

	/** Put FAULTS into sign status */
	protected void putFaults(Object value) {
		putStatus(DMS.FAULTS, value);
	}

	/** Put FAULTS into controller status */
	@Override
	protected void putCtrlFaults(String fault, String msg) {
		putFaults((fault != null) ? "controller" : null);
		super.putCtrlFaults(fault, msg);
	}

	/** Cleanup the operation. Called by CommThread.doPoll(). */
	@Override
	public void cleanup() {
		if (isSuccess()) {
			if (!status.isEmpty())
				m_dms.setStatusNotify(status.toString());
			m_dms.requestConfigure();
		} else {
			// flag dms not configured
			m_dms.setConfigure(false);
		}
		super.cleanup();
	}

	/** Return name of AWS system */
	static public String awsName() {
		return "CAWS";
	}

	/** Return true if the message is owned by the AWS */
	static boolean ownerIsAws(final String msg_owner) {
		if (msg_owner == null)
			return false;
		return msg_owner.toLowerCase().equals(awsName().toLowerCase());
	}

	/** set message attributes which are a function of the
	 * operation, sign, etc. */
	void setMsgAttributes(Message m) {
		m.setTimeoutMS(calcTimeoutMS());
	}

	/** Get the timeout for this operation */
	private int calcTimeoutMS() {
		CommConfig cc = controller.getCommLink().getCommConfig();
		int s = cc.getNoResponseDisconnectSec();
		LOG.log("Op timeout is " + s + " secs, dms=" + m_dms);
		return s * 1000;
	}

	/** random number generator */
	private static Random m_rand = new Random(System.currentTimeMillis());

	/** generate a unique operation id, which is a long,
	 *  returned as a string. */
	static String generateId() {
		return "" + (System.currentTimeMillis() + m_rand.nextInt());
	}

	/** return description of operation */
	public String getOperationDescription() {
		m_opDesc = (m_opDesc == null ?
			"Unnamed operation" : m_opDesc);
		return m_opDesc;
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

	/** Check message owner */
	public void checkMsgOwner(String o) {
		if ("reinit".equalsIgnoreCase(o))
			putCtrlFaults("other", "Power cycle event");
	}

	/** Phase to query the dms config, which is used by subclasses */
	class PhaseGetConfig extends Phase {

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
		private boolean parseResponse(Message mess, XmlElem xrr)
			throws IOException
		{
			long id = 0;
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
				id = xrr.getResLong("Id");
				boolean valid = xrr.getResBoolean("IsValid");
				String errmsg = xrr.getResString("ErrMsg");
				if (!valid) {
					LOG.log("WARNING: response ignored " +
						"because valid is false, " +
						"errmsg=" + errmsg
					);
					throw new ControllerException(errmsg);
				}
				signAccess = xrr.getResString(
					"signAccess");
				model = xrr.getResString("model");
				make = xrr.getResString("make");
				version = xrr.getResString("version");

				String stype = xrr.getResString("type");
				if (stype.toLowerCase().contains("full")) {
					type = DMSType.VMS_FULL;
				} else {
					LOG.log("SEVERE: Unknown "
						+ "matrix type read ("
						+ stype + ")");
				}

				horizBorder = xrr.getResInt("horizBorder");
				vertBorder = xrr.getResInt("vertBorder");
				horizPitch = xrr.getResInt("horizPitch");
				vertPitch = xrr.getResInt("vertPitch");
				signHeight = xrr.getResInt("signHeight");
				signWidth = xrr.getResInt("signWidth");
				characterHeightPixels = xrr.getResInt(
					"characterHeightPixels");
				characterWidthPixels = xrr.getResInt(
					"characterWidthPixels");
				signHeightPixels = xrr.getResInt(
					"signHeightPixels");
				signWidthPixels = xrr.getResInt(
					"signWidthPixels");
			}
			catch (IllegalArgumentException ex) {
				LOG.log("PhaseGetConfig: Malformed XML " +
					"received:" + ex + ", id=" + id);
				throw new ParsingException(ex);
			}

			controller.setVersionNotify(version);

			int dt = type.ordinal();
			SignDetailImpl sd = SignDetailImpl.findOrCreate(
				dt, false, "OTHER", signAccess, "other",
				"other", make, model, make, model, 0,
				2, 64, false, false);
			if (sd != null)
				m_dms.setSignDetailNotify(sd);
			SignConfigImpl sc = SignConfigImpl.findOrCreate(
				signWidth, signHeight,
				horizBorder, vertBorder,
				horizPitch, vertPitch,
				signWidthPixels, signHeightPixels,
				characterWidthPixels,
				characterHeightPixels,
				ColorScheme.MONOCHROME_1_BIT.ordinal(),
				DmsColor.AMBER.rgb(),
				DmsColor.BLACK.rgb());
			if (sc != null)
				m_dms.setSignConfigNotify(sc);
			return false;
		}

		/**
		 * Get the DMS configuration. This phase is used by subclassed
		 * operations if the DMS configuration has not been requested.
		 * Note, the type of exception throw here determines
		 * if the messenger reopens the connection on failure.
		 * @see CommThread#doPoll()
		 */
		protected Phase poll(CommMessage argmess)
			throws IOException
		{
			Message mess = (Message) argmess;
			setMsgAttributes(mess);
			mess.setName(getOpName());
			XmlElem xrr = buildXmlElem("GetDmsConfigReqMsg",
				"GetDmsConfigRespMsg");
			mess.add(xrr);
			sendRead(mess);
			return parseResponse(mess, xrr) ? this : m_next;
		}
	}
}
