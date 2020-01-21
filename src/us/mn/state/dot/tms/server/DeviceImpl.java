/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2020  Minnesota Department of Transportation
 * Copyright (C) 2015-2017  SRF Consulting Group
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
package us.mn.state.dot.tms.server;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.OpDevice;

/**
 * DeviceImpl is the base class for all field devices, including detectors,
 * cameras, ramp meters, dynamic message signs, etc.
 *
 * @author Douglas Lau
 */
abstract public class DeviceImpl extends ControllerIoImpl implements Device {

	/** Device debug log */
	static private final DebugLog DEVICE_LOG = new DebugLog("device");

	/** "Long" polling period */
	static private final int PERIOD_LONG_SEC = 60 * 5;

	/** Check if device log is open */
	protected boolean isDeviceLogging() {
		return DEVICE_LOG.isOpen();
	}

	/** Log a device message */
	protected void logError(String msg) {
		if (DEVICE_LOG.isOpen())
			DEVICE_LOG.log(getName() + ": " + msg);
	}

	/** Create a device */
	protected DeviceImpl(String n, ControllerImpl c, int p, String nt) {
		super(n, c, p);
		notes = nt;
	}

	/** Create a new device */
	protected DeviceImpl(String n) throws TMSException, SonarException {
		this(n, null, 0, "");
	}

	/** Get the device poller */
	public DevicePoller getPoller() {
		ControllerImpl c = controller;	// Avoid race
		return (c != null) ? c.getPoller() : null;
	}

	/** Administrator notes for this device */
	protected String notes;

	/** Set the administrator notes */
	@Override
	public void setNotes(String n) {
		notes = n;
	}

	/** Set the administrator notes */
	public void doSetNotes(String n) throws TMSException {
		if (!objectEquals(n, notes)) {
			store.update(this, "notes", n);
			setNotes(n);
		}
	}

	/** Get the administrator notes */
	@Override
	public String getNotes() {
		return notes;
	}

	/** Operation which owns the device */
	private transient OpDevice owner;

	/** Acquire ownership of the device */
	public OpDevice acquire(OpDevice o) {
		try {
			// Name used for unique device acquire/release lock
			synchronized (name) {
				if (owner == null)
					owner = o;
				return owner;
			}
		}
		finally {
			if (owner == o)
				notifyAttribute("operation");
		}
	}

	/** Release ownership of the device */
	public OpDevice release(OpDevice o) {
		try {
			// Name used for unique device acquire/release lock
			synchronized(name) {
				OpDevice _owner = owner;
				if (owner == o)
					owner = null;
				return _owner;
			}
		}
		finally {
			if (owner == null)
				notifyAttribute("operation");
		}
	}

	/** Get a description of the current device operation */
	@Override
	public String getOperation() {
		OpDevice o = owner;
		return (o != null) ? o.getOperationDescription() : "None";
	}

	/** Item style bits */
	private transient long styles = calculateStyles();

	/** Get item style bits */
	@Override
	public long getStyles() {
		return styles;
	}

	/** Set the item style bits (and notify clients) */
	private final void setStyles(long s) {
		if (s != styles) {
			styles = s;
			notifyAttribute("styles");
		}
	}

	/** Update the device item styles */
	public void updateStyles() {
		setStyles(calculateStyles());
	}

	/** Calculate the item styles */
	protected long calculateStyles() {
		long s = ItemStyle.ALL.bit();
		if (isActive())
			s |= ItemStyle.ACTIVE.bit();
		else
			s |= ItemStyle.INACTIVE.bit();
		if (isAvailable())
			s |= ItemStyle.AVAILABLE.bit();
		if (isOnline() && needsMaintenance())
			s |= ItemStyle.MAINTENANCE.bit();
		if (isActive() && isFailed())
			s |= ItemStyle.FAILED.bit();
		if (getController() == null)
			s |= ItemStyle.NO_CONTROLLER.bit();
		return s;
	}

	/** Get the active status */
	public boolean isActive() {
		ControllerImpl c = controller;	// Avoid race
		return (c != null) && c.isActive();
	}

	/** Get the failure status */
	public boolean isFailed() {
		ControllerImpl c = controller;	// Avoid race
		// FIXME: check if comm link connected
		//        needs status update to work properly
		return (c == null) || c.isFailed();
	}

	/** Test if device is online (active and not failed) */
	public boolean isOnline() {
		return isActive() && !isFailed();
	}

	/** Test if device is available */
	protected boolean isAvailable() {
		return isOnline() && !needsMaintenance();
	}

	/** Test if a device needs maintenance */
	protected boolean needsMaintenance() {
		return !getMaintenance().isEmpty();
	}

	/** Get maintenance status */
	private String getMaintenance() {
		return ControllerHelper.getMaintenance(getController());
	}

	/** Check if the controller has an error */
	public boolean hasError() {
		return isFailed() || hasStatusError();
	}

	/** Check if the controller has a status error */
	private boolean hasStatusError() {
		ControllerImpl c = controller;	// Avoid race
		return (c == null) || !c.getStatus().isEmpty();
	}

	/** Get the polling period (sec) */
	protected int getPollPeriod() {
		ControllerImpl c = controller;	// Avoid race
		return (c != null) ? c.getPollPeriod() : 30;
	}

	/** Check if link is any type of modem (dial-up or cell) */
	public boolean isModemAny() {
		ControllerImpl c = controller;
		return (c != null) && c.isModemAny();
	}

	/** Check if the polling period is "long", with a modem link */
	public boolean isLongPeriodModem() {
		return getPollPeriod() >= PERIOD_LONG_SEC && isModemAny();
	}

	/** Check if dial-up is required to communicate */
	public boolean isDialUpRequired() {
		ControllerImpl c = controller;
		return (c != null) && c.isDialUpRequired();
	}

	/** Request a device operation */
	@Override
	public void setDeviceRequest(int r) {
		sendDeviceRequest(DeviceRequest.fromOrdinal(r));
	}

	/** Request a device operation */
	public final void setDeviceReq(DeviceRequest dr) {
		sendDeviceRequest(dr);
	}

	/** Send a device request operation */
	abstract protected void sendDeviceRequest(DeviceRequest dr);

	/** Perform a periodic poll */
	@Override
	public void periodicPoll() {
		sendDeviceRequest(DeviceRequest.QUERY_STATUS);
	}
}
