/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2024  Minnesota Department of Transportation
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
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * An operation is a sequence of phases to be performed on a field controller.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author Travis Swanston
 */
abstract public class OpController<T extends ControllerProperty> {

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
	public synchronized final void setSucceeded() {
		setSuccess(true);
		phase = null;
	}

	/** Controller status */
	private JSONObject ctrl_stat = null;

	/** Put a key/value pair into controller status */
	private final void putCtrlStatus(String key, Object value) {
		if (ctrl_stat == null)
			ctrl_stat = new JSONObject();
		try {
			ctrl_stat.putOpt(key, value);
		}
		catch (JSONException e) {
			System.err.println(
				"putCtrlStatus: " + e.getMessage() + ", " + key
			);
		}
	}

	/** Put FAULTS into controller status */
	protected void putCtrlFaults(String fault, String msg) {
		putCtrlStatus(Controller.FAULTS, fault);
		putCtrlStatus(Controller.MSG, msg);
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
	public void handleCommError(EventType et) {
		controller.logCommEvent(et, id);
		if (!shouldRetry())
			setFailed();
	}

	/** Operation error counter */
	private int error_cnt = 0;

	/** Check if the operation should be retried */
	private boolean shouldRetry() {
		++error_cnt;
		return error_cnt < getRetryThreshold();
	}

	/** Get the error retry threshold */
	public int getRetryThreshold() {
		return controller.isOffline()
		      ? 0
		      : controller.getRetryThreshold();
	}

	/** Cleanup the operation.  The operation gets cleaned up after
	 * processing is complete and it is removed from the queue. */
	public void cleanup() {
		updateCtrlStatus();
		controller.completeOperation(id, isSuccess());
	}

	/** Update the controller status */
	protected final void updateCtrlStatus() {
		JSONObject s = ctrl_stat;
		if (s != null) {
			controller.setStatusNotify((!s.isEmpty())
				? s.toString()
				: null);
			// Set to `null` in case this is called more than once
			ctrl_stat = null;
		}
	}
}
