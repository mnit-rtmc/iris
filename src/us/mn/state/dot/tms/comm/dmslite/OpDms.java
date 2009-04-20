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

package us.mn.state.dot.tms.comm.dmslite;

import java.io.IOException;
import java.util.Random;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.DebugLog;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttributeHelperD10;
import us.mn.state.dot.tms.comm.ChecksumException;
import us.mn.state.dot.tms.comm.Device2Operation;
import us.mn.state.dot.tms.utils.SString;
import us.mn.state.dot.tms.utils.STime;

/**
 * Operation to be performed on a dynamic message sign
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
abstract public class OpDms extends Device2Operation {

	/** failure message for unknown reasons */
	final static String FAILURE_UNKNOWN = "Failure, unknown reason";

	/** DMS debug log */
	static protected final DebugLog DMS_LOG = new DebugLog("dms");

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

	/** 
	* Log exceptions in the DMS debug log. This method should be called by
	* operations that fail.
	*/
	public void handleException(IOException e) {
		if(e instanceof ChecksumException) {
			ChecksumException ce = (ChecksumException)e;
			DMS_LOG.log(m_dms.getName() + " (" + toString() +
				"), " + ce.getScannedData());
		}
		super.handleException(e);
	}

	/** Cleanup the operation. This method is called by MessagePoller.doPoll() if an operation is successful */
	public void cleanup() {
		if(success)
			m_dms.requestConfigure();
		else
			m_dms.setConfigure(false);
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

	/** return the timeout for this operation */
	public int calcTimeoutMS() {
		int secs = 60; //FIXME: use existing sys attribute
		assert m_dms != null : "m_dms is null in OpDms.getTimeoutMS()";
		SignAccessType at = getSignAccessType(m_dms);
		if(at == SignAccessType.DIALUPMODEM) {
			secs = SystemAttributeHelperD10.dmsliteModemOpTimeoutSecs();
			System.err.println("connection type is modem" +
				", dms="+m_dms.toString()+", timeout secs="+secs);
		} else if(at == SignAccessType.WIZARD) {
			secs = SystemAttributeHelperD10.dmsliteOpTimeoutSecs();
			System.err.println("connection type is wizard" +
				", dms="+m_dms.toString()+", timeout secs="+secs);
		}
		// if unknown access type, this happens when the first 
		// OpQueryConfig message is being sent, so a default 
		// timeout should be used.
		return secs * 1000;
	}

	/** set message attributes which are a function of the operation, sign, etc. */
	public void setMsgAttributes(Message m) {
		m.setTimeoutMS(this.calcTimeoutMS());
	}

	/**
	  * handle a failed operation.
	  * @return true if the operation should be retried else false.
	  */
	protected boolean flagFailureShouldRetry(String errmsg) {
	 	String msg = m_dms.getName() + " error: " + errmsg;

		// trigger error handling, changes status if necessary
		handleException(new IOException(msg));

		// retry?
		boolean retry = (controller != null && controller.retry(msg));
		return retry;
	}

	/* reset error counter for DMS */
	protected void resetErrorCounter() {
	 	String id = m_dms.getName();
		if(controller != null) {
			controller.resetErrorCounter(id);
		}
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
}
