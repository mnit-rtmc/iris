/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DeviceImpl;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.SString;

/**
 * An operation is a sequence of steps to be performed on a field controller.
 *
 * @author Douglas Lau
 */
public final class Operation implements Comparable<Operation> {

	/** Get the error retry threshold */
	static private int systemRetryThreshold() {
		return SystemAttrEnum.OPERATION_RETRY_THRESHOLD.getInt();
	}

	/** Maximum message length */
	static private final int MAX_MSG_LEN = 64;

	/** Filter a message */
	static private String filterMsg(String m) {
		return SString.truncate(m, MAX_MSG_LEN);
	}

	/** Append a status string */
	static private String appendStatus(String a, String b) {
		return (a.length() > 0) ? (a + ", " + b) : b;
	}

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
	private final DeviceImpl device;

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
	private Operation(String n, ControllerImpl c, DeviceImpl d, OpStep s) {
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
	public Operation(String n, DeviceImpl d, OpStep s) {
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

	/** Set the polling flag */
	private void setPolling(boolean p) {
		OpStep s = step;
		if (s != null)
			s.setPolling(p);
	}

	/** Priority of the operation */
	private PriorityLevel priority = PriorityLevel.DEVICE_DATA;

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

	/** Handle an IO event */
	public void handleEvent(EventType et, String msg) {
		// Poll failed -- try again
		setPolling(true);
		controller.logCommEvent(et, getId(), filterMsg(msg));
		if (!retry())
			setFailed();
	}

	/** Operation error counter */
	private int error_cnt = 0;

	/** Check if the operation should be retried */
	private boolean retry() {
		error_cnt++;
		return error_cnt < getRetryThreshold();
	}

	/** Get the error retry threshold */
	private int getRetryThreshold() {
		return (controller.isFailed()) ? 0 : systemRetryThreshold();
	}

	/** Destroy the operation.  The operation gets destroyed after
	 * processing is complete and it is removed from the queue. */
	public void destroy() {
		if (n_runs > 0 && controller != null) {
			// FIXME: release device lock
			updateStatus();
		}
	}

	/** Update status when done or for long-lived operations */
	public void updateStatus() {
		updateMaintStatus();
		updateErrorStatus();
		controller.completeOperation(getId(), isSuccess());
	}

	/** Update controller maintenance status */
	private void updateMaintStatus() {
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
