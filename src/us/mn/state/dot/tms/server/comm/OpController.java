/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2016  Minnesota Department of Transportation
 * Copyright (C) 2012  Iteris Inc.
 * Copyright (C) 2014-2015  AHMCT, University of California
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
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * An operation which is performed on a field controller.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author Travis Swanston
 */
abstract public class OpController<T extends ControllerProperty>
	extends Operation<T>
{
	/** Comm error log */
	static private final DebugLog COMM_LOG = new DebugLog("comm");

	/** Maximum message length */
	static private final int MAX_MSG_LEN = 64;

	/** Truncate a message */
	static private String truncateMsg(String m) {
		return (m.length() <= MAX_MSG_LEN)
		      ? m
		      : m.substring(0, MAX_MSG_LEN);
	}

	/** Filter a message */
	static private String filterMsg(String m) {
		return (m != null) ? truncateMsg(m) : "";
	}

	/** Append a status string */
	static private String appendStatus(String a, String b) {
		return (a.length() > 0) ? (a + ", " + b) : b;
	}

	/** Controller to be polled */
	protected final ControllerImpl controller;

	/** Get the controller being polled */
	public ControllerImpl getController() {
		return controller;
	}

	/** Device ID */
	protected final String id;

	/** Maint status message */
	private String maintStatus = null;

	/** Set the maint status message.  If non-null, the controller "maint"
	 * attribute is set to this message when the operation completes. */
	public void setMaintStatus(String s) {
		maintStatus = s;
	}

	/** Error status message */
	private String err_status = null;

	/** Set the error status message.  If non-null, the controller "error"
	 * attribute is set to this message when the operation completes. */
	public void setErrorStatus(String s) {
		assert s != null;
		if (err_status != null) {
			if (s.length() > 0)
				err_status = appendStatus(err_status, s);
		} else
			err_status = s;
	}

	/** Create a new controller operation */
	protected OpController(PriorityLevel p, ControllerImpl c, String i) {
		super(p);
		assert c != null;
		controller = c;
		id = i;
	}

	/** Create a new controller operation */
	protected OpController(PriorityLevel p, ControllerImpl c) {
		this(p, c, c.toString());
	}

	/** Operation equality test */
	@Override
	public boolean equals(Object o) {
		return (o instanceof OpController) &&
		       (getClass() == o.getClass()) &&
		       ((OpController)o).controller == controller;
	}

	/** Get a string description of the operation */
	@Override
	public final String toString() {
		return super.toString() + " (" + id + ")";
	}

	/** Handle a communication error */
	@Override
	public void handleCommError(EventType et, String msg) {
		logComm(et, msg);
		controller.logCommEvent(et, id, filterMsg(msg));
		super.handleCommError(et, msg);
	}

	/** Log a comm error to debug log */
	private void logComm(EventType et, String msg) {
		if (COMM_LOG.isOpen())
			COMM_LOG.log(id + " " + et + ", " + msg);
	}

	/** Update controller maintenance status */
	protected final void updateMaintStatus() {
		String s = maintStatus;
		if (s != null) {
			controller.setMaintNotify(filterMsg(s));
			maintStatus = null;
		}
	}

	/** Update controller error status */
	private void updateErrorStatus() {
		String s = err_status;
		if (s != null) {
			controller.setErrorStatus(filterMsg(s));
			err_status = null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		updateMaintStatus();
		updateErrorStatus();
		controller.completeOperation(id, isSuccess());
		super.cleanup();
	}

	/** Get the error retry threshold */
	@Override
	public int getRetryThreshold() {
		return (controller.isFailed()) ? 0 : super.getRetryThreshold();
	}
}
