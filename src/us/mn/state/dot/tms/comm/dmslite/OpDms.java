/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2008  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.DebugLog;
import us.mn.state.dot.tms.comm.ChecksumException;
import us.mn.state.dot.tms.comm.DeviceOperation;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.utils.SString;
import us.mn.state.dot.tms.utils.STime;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

/**
 * Operation to be performed on a dynamic message sign
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
abstract public class OpDms extends DeviceOperation {

	// timeout for DMS messages (values must match cmsserver props file)
	static final int TIMEOUT_DMS_DEFAULT_MS = 1000*(60+5);
	static final int TIMEOUT_DMS_MODEM_MS = 1000*(60*5+5);
	static final int TIMEOUT_DMS_WIZARD_MS = 1000*(60+5);

	/** DMS debug log */
	static protected final DebugLog DMS_LOG = new DebugLog("dms");

	/** DMS to operate */
	protected final DMSImpl m_dms;

	/** Create a new DMS operation */
	public OpDms(int p, DMSImpl d) {
		super(p, d);
		m_dms = d;
	}

	/** 
	* Log exceptions in the DMS debug log. This method should be called by
	* operations that fail.
	*/
	public void handleException(IOException e) {
		if (e instanceof ChecksumException) {
		    ChecksumException ce = (ChecksumException) e;
		    DMS_LOG.log(m_dms.getId() + " (" + toString() + "), " + ce.getScannedData());
		}

		super.handleException(e);
	}

	/** Cleanup the operation. This method is called by MessagePoller.doPoll() if an operation is successful */
	public void cleanup() {
		//System.err.println("dmslite.OpDms.cleanup() called, success="+success);
		m_dms.setReset(success);
		super.cleanup();
	}

	/** return the timeout for this operation */
	public int calcTimeoutMS() {
		assert m_dms!=null : "m_dms is null in OpDms.getTimeoutMS()";
		String a=m_dms.getSignAccess();
		int ms=TIMEOUT_DMS_DEFAULT_MS;
		if (a.toLowerCase().contains("modem")) {
			ms=TIMEOUT_DMS_MODEM_MS;
			System.err.println("connection type is modem:"+a+", dms="+m_dms.toString());
		} else if (a.toLowerCase().contains("wizard")) {
			ms=TIMEOUT_DMS_WIZARD_MS;
			System.err.println("connection type is wizard:"+a+", dms="+m_dms.toString());
		} else {
			ms=TIMEOUT_DMS_DEFAULT_MS;
			// unknown sign type, this happens when the first 
			// OpDmsQueryConfig message is being sent, so a 
			// default timeout should be assigned.
			//System.err.println("OpDms.calcTimeoutMS(): unknown sign access type:"+a+", dms="+m_dms.toString());
		}
		return ms;
	}

	/** set message attributes which are a function of the operation, sign, etc. */
	public void setMsgAttributes(Message m) {
		m.setTimeoutMS(this.calcTimeoutMS());
	}

	/**
	  * handle a failed operation.
	  * @return true if the operation should be retried else false.
	  */
	protected boolean flagFailureShouldRetry(String errmsg)
	{
	 	String msg=m_dms.getId()+" error: "+errmsg;

		// trigger error handling, changes status if necessary
		handleException(new IOException(msg));

		// enforce id length restriction due to database column size
		final int DB_COLUMN_MAX_LEN=30;
		String id=SString.truncate(msg,DB_COLUMN_MAX_LEN);

		// retry?
		boolean retry = (controller != null && controller.retry(id));
		return retry;
	}

	/* reset error counter for DMS */
	protected void resetErrorCounter()
	{
	 	String id=m_dms.getId();
		if(controller != null) {
			controller.resetErrorCounter(id);
			//System.err.println("OpQueryDms.resetErrorCounter(): reset comm counter");
		}
	}

	/** random number generator */
	static private Random m_rand = new Random(System.currentTimeMillis());

	/** generate a unique operation id, which is a long, returned as a string */
	public static String generateId() {
		return new Long(System.currentTimeMillis()+m_rand.nextInt()).toString();
	}

	/** create a blank message */
	public static SignMessage createBlankMsg(DMSImpl dms,String owner)
	{
		MultiString multi = new MultiString();
		BitmapGraphic bbm = new BitmapGraphic(
	    		dms.getSignWidthPixels(), 
			dms.getSignHeightPixels());
		SignMessage sm = new SignMessage(owner,multi,bbm,0);
		return(sm);
	}

	/** update iris status, called after operation complete */
	public void complete(Message m) {
		m_dms.setUserNote(buildUserNote(m));
	}

	/** build user note */
	public String buildUserNote(Message m) {
		SignMessage sm=m_dms.getMessage();
		StringBuilder note=new StringBuilder();
		String deploytime="null";
		if (sm!=null && sm.getDeployTime()!=null)
			deploytime=sm.getDeployTime().toString();
		note.append("Last operation at "+STime.getCurTimeShortString());
		String delta=SString.doubleToString((((double)m.getCompletionTimeMS())/1000),2);
		note.append(" (").append(delta).append(" secs)");
		//note.append(", last message deployed: "+deploytime);
		note.append(".");
		return note.toString();
	}

}

