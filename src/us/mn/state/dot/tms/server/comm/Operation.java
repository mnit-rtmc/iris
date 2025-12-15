/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2025  Minnesota Department of Transportation
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
import java.nio.ByteBuffer;
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.CommState;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.ControllerIoImpl;
import us.mn.state.dot.tms.utils.I18N;

/**
 * An operation is a sequence of steps to be performed on a field controller.
 *
 * @author Douglas Lau
 */
public final class Operation implements Comparable<Operation> {

	/** Expire time for steps which wait indefinitely */
	static private final int EXPIRE_INDEFINITE_MS = 24 * 60 * 60 * 1000;

	/** Operation name */
	private final String name;

	/** Get the internationalized operation name */
	public String getName() {
		return I18N.get(name);
	}

	/** Controller to be polled */
	private final ControllerImpl controller;

	/** Get the controller being polled */
	public ControllerImpl getController() {
		return controller;
	}

	/** Get the controller drop address */
	public int getDrop() {
		return controller.getDrop();
	}

	/** Device ID */
	private final ControllerIoImpl device;

	/** Get the operation device */
	public ControllerIoImpl getDevice() {
		return device;
	}

	/** Get the device ID */
	public String getId() {
		return (device != null)
		      ? device.toString()
		      : controller.getLbl();
	}

	/** Create a new operation.
	 * @param n Operation name.
	 * @param c Controller.
	 * @param d Device.
	 * @param s First step. */
	public Operation(String n, ControllerImpl c, ControllerIoImpl d,
		OpStep s)
	{
		name = n;
		controller = c;
		device = d;
		step = s;
	}

	/** Create a new operation.
	 * @param n Operation name.
	 * @param c Controller.
	 * @param s First step. */
	public Operation(String n, ControllerImpl c, OpStep s) {
		this(n, c, null, s);
	}

	/** Create a new operation.
	 * @param n Operation name.
	 * @param d Device.
	 * @param s First step. */
	public Operation(String n, ControllerIoImpl d, OpStep s) {
		this(n, (ControllerImpl) d.getController(), d, s);
		// FIXME: acquire device lock before first step
	}

	/** Get a string description */
	@Override
	public String toString() {
		return getName() + " (" + getId() + ")";
	}

	/** Operation equality test */
	@Override
	public boolean equals(Object other) {
		return (other instanceof Operation) &&
		       (compareTo((Operation) other) == 0);
	}

	/** Compare to another operation */
	@Override
	public int compareTo(Operation other) {
		if (this == other)
			return 0;
		int c = name.compareTo(other.name);
		if (c != 0)
			return c;
		else
			return getId().compareTo(other.getId());
	}

	/** Get the operation hash code */
	@Override
	public int hashCode() {
		return name.hashCode() ^ getId().hashCode();
	}

	/** Current step */
	private OpStep step;

	/** Set the step */
	private void setStep(OpStep s) {
		step = s;
	}

	/** Check if the operation is done */
	public boolean isDone() {
		return step == null;
	}

	/** Check if the operation needs polling */
	public boolean isPolling() {
		OpStep s = step;
		return (s != null) && s.isPolling();
	}

	/** Priority of the operation */
	private PriorityLevel priority = PriorityLevel.POLL_LOW;

	/** Get the priority of the operation.
	 * @return Priority of the operation (@see PriorityLevel) */
	public PriorityLevel getPriority() {
		return priority;
	}

	/** Set the priority of the operation */
	public void setPriority(PriorityLevel p) {
		priority = p;
	}

	/** Number of runs -- used for fair queueing */
	private int n_runs = 0;

	/** Get the number of runs */
	public int getRuns() {
		return n_runs;
	}

	/** Expiration time */
	private long expire = 0;

	/** Get expiration time */
	public long getExpire() {
		return expire;
	}

	/** Set the remaining time (ms) */
	public void setRemaining(int rt) {
		OpStep s = step;
		if (s != null && s.isWaitingIndefinitely())
			rt = EXPIRE_INDEFINITE_MS;
		expire = TimeSteward.currentTimeMillis() + rt;
	}

	/** Get the remaining time (ms) */
	public long getRemaining() {
		return expire - TimeSteward.currentTimeMillis();
	}

	/** Success or failure of operation */
	private boolean success = true;

	/** Check if the operation succeeded */
	public boolean isSuccess() {
		return success;
	}

	/** Set the success flag.  This will clear the error counter if true. */
	public final void setSuccess(boolean s) {
		success = s;
		if (s)
			error_cnt = 0;
	}

	/** Set the operation to failed */
	public void setFailed() {
		setSuccess(false);
		setStep(null);
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
	public void putCtrlFaults(String fault, String msg) {
		putCtrlStatus(Controller.FAULTS, fault);
		putCtrlStatus(Controller.MSG, msg);
	}

	/** Poll the current step.
	 * @param tx_buf Transmit buffer. */
	public void poll(ByteBuffer tx_buf) throws IOException {
		n_runs++;
		OpStep s = step;
		if (s != null) {
			s.poll(this, tx_buf);
			if (!isDone())
				setStep(s.next());
		}
	}

	/** Decode received data.
	 * @param rx_buf Receive buffer. */
	public void recv(ByteBuffer rx_buf) throws IOException {
		n_runs++;
		OpStep s = step;
		if (s != null) {
			s.recv(this, rx_buf);
			if (!isDone())
				setStep(s.next());
		}
	}

	/** Handle a comm state change */
	public void handleCommState(CommState cs) {
		controller.setCommState(cs);
		if (!retry())
			setFailed();
	}

	/** Operation error counter */
	private int error_cnt = 0;

	/** Check if the operation should be retried */
	private boolean retry() {
		OpStep s = step;
		if (s != null)
			s.clearError();
		error_cnt++;
		return error_cnt < getRetryThreshold();
	}

	/** Get the error retry threshold */
	private int getRetryThreshold() {
		return controller.isOffline()
		      ? 0
		      : controller.getRetryThreshold();
	}

	/** Destroy the operation.  The operation gets destroyed after
	 * processing is complete and it is removed from the queue. */
	public void destroy() {
		if (n_runs > 0 && controller != null) {
			// FIXME: release device lock
			updateCtrlStatus();
		}
	}

	/** Update status when done or for long-lived operations */
	public void updateCtrlStatus() {
		JSONObject s = ctrl_stat;
		if (s != null) {
			controller.setStatusNotify((!s.isEmpty())
				? s.toString()
				: null);
			// Set to `null` because this method may be called
			// more than once after the operation is done
			ctrl_stat = null;
		}
		controller.completeOperation(getId(), isSuccess());
	}
}
