/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import java.io.IOException;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DebugLog;
import us.mn.state.dot.tms.server.event.EventType;
import us.mn.state.dot.tms.utils.SString;

/**
 * An operation which is performed on a field controller.
 *
 * @author Douglas Lau
 */
abstract public class OpController extends Operation {

	/** Comm error log */
	static protected final DebugLog COMM_LOG = new DebugLog("comm");

	/** Filter a message */
	static protected String filterMessage(String m) {
		final int MAXLEN = 64;
		return SString.truncate(m, MAXLEN);
	}

	/** Controller to be polled */
	protected final ControllerImpl controller;

	/** Get the controller being polled */
	public ControllerImpl getController() {
		return controller;
	}

	/** Drop address of the controller to be polled */
	protected final int drop;

	/** Device ID */
	protected final String id;

	/** Error status message */
	protected String errorStatus = null;

	/** Set the error status message */
	public void setErrorStatus(String s) {
		errorStatus = s;
	}

	/** Operation error counter */
	protected int errorCounter = 0;

	/** Create a new controller operation */
	protected OpController(int p, ControllerImpl c, String i) {
		super(p);
		assert c != null;
		controller = c;
		drop = controller.getDrop();
		id = i;
	}

	/** Create a new controller operation */
	protected OpController(int p, ControllerImpl c) {
		this(p, c, c.toString());
	}

	/** Start an operation on the device */
	public void start() {
		controller.addOperation(this);
	}

	/** Get a string description of the operation */
	public String toString() {
		return super.toString() + " (" + id + ")";
	}

	/** Handle a communication error */
	public void handleCommError(EventType et, String msg) {
		COMM_LOG.log(id + " " + et + ", " + msg);
		controller.logException(id, filterMessage(msg));
		if(!retry())
 			super.handleCommError(et, msg);
	}

	/** Determine if this operation should be retried */
	public boolean retry() {
		if(controller.isFailed())
			return false;
		errorCounter++;
		return errorCounter < getRetryThreshold();
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(errorStatus != null)
			controller.setError(filterMessage(errorStatus));
		controller.completeOperation(id, success);
	}

	/** Get the error retry threshold */
	public int getRetryThreshold() {
		return SystemAttrEnum.OPERATION_RETRY_THRESHOLD.getInt();
	}
}
