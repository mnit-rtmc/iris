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
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * An operation is a sequence of phases to be performed on a field controller.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author Travis Swanston
 */
abstract public class OpController<T extends ControllerProperty>
	extends Operation<T>
{
	/** Get the error retry threshold */
	static private int systemRetryThreshold() {
		return SystemAttrEnum.OPERATION_RETRY_THRESHOLD.getInt();
	}

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

	/** Strip all characters up to the last dot */
	static private String stripToLastDot(String v) {
		int i = v.lastIndexOf('.');
		return (i >= 0) ? v.substring(i + 1) : v;
	}

	/** Base class for operation phases */
	abstract protected class Phase<T extends ControllerProperty> {

		/** Perform a poll.
		 * @return The next phase of the operation, or null */
		abstract protected Phase<T> poll(CommMessage<T> mess)
			throws IOException, DeviceContentionException;
	}

	/** Current phase of the operation, or null if done */
	private Phase<T> phase;

	/** Begin the operation.  The operation begins when it is queued for
	 * processing. */
	public final void begin() {
		phase = phaseOne();
	}

	/** Create the first phase of the operation.  This method cannot be
	 * called in the Operation constructor, because the object may not
	 * have been fully constructed yet (subclass initialization). */
	abstract protected Phase<T> phaseOne();

	/** Priority of the operation */
	private PriorityLevel priority;

	/** Get the priority of the operation.
	 * @return Priority of the operation (@see PriorityLevel) */
	public final PriorityLevel getPriority() {
		return priority;
	}

	/** Set the priority of the operation */
	public final void setPriority(PriorityLevel p) {
		if (p.ordinal() < priority.ordinal())
			priority = p;
	}

	/** Controller to be polled */
	protected final ControllerImpl controller;

	/** Get the controller being polled */
	public ControllerImpl getController() {
		return controller;
	}

	/** Device ID */
	protected final String id;

	/** Success or failure of operation */
	private boolean success = true;

	/** Check if the operation succeeded */
	public boolean isSuccess() {
		return success;
	}

	/** Set the success flag.  This will clear the error counter if true. */
	protected final void setSuccess(boolean s) {
		success = s;
		if (s)
			error_cnt = 0;
	}

	/** Set the operation to failed */
	public synchronized final void setFailed() {
		setSuccess(false);
		phase = null;
	}

	/** Set the operation to succeeded */
	@Override
	public synchronized final void setSucceeded() {
		setSuccess(true);
		phase = null;
	}

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
		assert p != null;
		assert c != null;
		priority = p;
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
		       ((OpController) o).controller == controller;
	}

	/** Get a string description of the operation */
	@Override
	public final String toString() {
		return stripToLastDot(phaseClass().getName()) + " (" + id + ")";
	}

	/** Get the phase class */
	private Class phaseClass() {
		Phase<T> p = phase;
		return (p != null) ? p.getClass() : getClass();
	}

	/** Get the operation name */
	protected final String getOpName() {
		return stripToLastDot(getClass().getName());
	}

	/** Get a description of the operation */
	public String getOperationDescription() {
		return getOpName();
	}

	/** Perform a poll with the current phase.
	 * @param mess Message to use for polling. */
	public final void poll(CommMessage<T> mess) throws IOException,
		DeviceContentionException
	{
		Phase<T> p = phase;
		if (p != null)
			updatePhase(p.poll(mess));
	}

	/** Update the phase of the operation */
	private synchronized void updatePhase(Phase<T> p) {
		// Need to synchronize against setFailed / setSucceeded
		if (!isDone())
			phase = p;
	}

	/** Check if the operation is done */
	public final boolean isDone() {
		return phase == null;
	}

	/** Handle a communication error */
	@Override
	public void handleCommError(EventType et, String msg) {
		logComm(et, msg);
		controller.logCommEvent(et, id, filterMsg(msg));
		if (!retry())
			setFailed();
	}

	/** Log a comm error to debug log */
	private void logComm(EventType et, String msg) {
		if (COMM_LOG.isOpen())
			COMM_LOG.log(id + " " + et + ", " + msg);
	}

	/** Operation error counter */
	private int error_cnt = 0;

	/** Check if the operation should be retried */
	private boolean retry() {
		++error_cnt;
		return error_cnt < getRetryThreshold();
	}

	/** Get the error retry threshold */
	public int getRetryThreshold() {
		return (controller.isFailed()) ? 0 : systemRetryThreshold();
	}

	/** Cleanup the operation.  The operation gets cleaned up after
	 * processing is complete and it is removed from the queue.  This method
	 * may get called more than once after the operation is done. */
	@Override
	public void cleanup() {
		updateMaintStatus();
		updateErrorStatus();
		controller.completeOperation(id, isSuccess());
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
}
