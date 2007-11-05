/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.rmi.RemoteException;
import java.util.Calendar;
import us.mn.state.dot.vault.FieldMap;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * The Controller170Impl class represents model 170 traffic controllers.
 *
 * @author Douglas Lau
 */
public class Controller170Impl extends ControllerImpl
	implements Controller170, Constants
{
	/** ObjectVault table name */
	static public final String tableName = "controller_170";

	/** Create a new 170 controller */
	public Controller170Impl(CircuitImpl c, short d)
		throws RemoteException
	{
		super(c, d);
	}

	/** Create a controller from an ObjectVault field map */
	protected Controller170Impl(FieldMap fields) throws RemoteException {
		super(fields);
	}

	/** Cabinet type code */
	protected short cabinet;

	/** Set the cabinet type */
	public synchronized void setCabinet(short c) throws TMSException {
		if(c == cabinet)
			return;
		if(c < 0 || c >= CABINET.length || CABINET[c].equals(RESERVED))
			throw new ChangeVetoException("Invalid cabinet type");
		try {
			vault.update(this, "cabinet", new Short(c),
				getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		cabinet = c;
	}

	/** Get the cabinet type */
	public short getCabinet() { return cabinet; }

	/** Check the dip switch settings against the selected cabinet type */
	public void checkCabinetType(int dips) {
		if(dips != cabinet) {
			String st = String.valueOf(dips);
			if(dips >= 0 && dips < CABINET.length)
				st = CABINET[dips];
			setSetup("CABINET TYPE " + st);
		}
	}

	/** I/O pin for first traffic device */
	static protected final int DEVICE_PIN = 2;

	/** Set the (first) traffic device */
	public void setDevice(String id) throws TMSException {
		DeviceImpl d = (DeviceImpl)deviceList.getElement(id);
		if(d == null && id != null) {
			d = (DeviceImpl)getDevice();
			if(d == null || !id.equals(d.getId())) {
				throw new ChangeVetoException(
					"Device unavailable: " + id);
			}
		}
		setIO(DEVICE_PIN, d);
	}

	/** Get the (first) traffic device */
	public synchronized TrafficDevice getDevice() {
		ControllerIO io = io_pins.get(DEVICE_PIN);
		if(io instanceof TrafficDevice)
			return (TrafficDevice)io;
		else
			return null;
	}

	/** Get the first ramp meter */
	public RampMeter getMeter1() {
		TrafficDevice d = getDevice();
		if(d instanceof RampMeter)
			return (RampMeter)d;
		else
			return null;
	}

	/** I/O pin for second ramp meter */
	static protected final int METER_2_PIN = 3;

	/** Set the second ramp meter */
	public void setMeter2(String id) throws TMSException {
		RampMeterImpl m = (RampMeterImpl)availableMeters.getElement(id);
		if(m == null && id != null) {
			m = (RampMeterImpl)getMeter2();
			if(m == null || !id.equals(m.getId())) {
				throw new ChangeVetoException(
					"Meter unavailable: " + id);
			}
		}
		setIO(METER_2_PIN, m);
	}

	/** Get the second ramp meter */
	public synchronized RampMeter getMeter2() {
		ControllerIO io = io_pins.get(METER_2_PIN);
		if(io instanceof RampMeter)
			return (RampMeter)io;
		else
			return null;
	}

	/** Assign a detector to an input */
	protected void setDetector(int input, DetectorImpl d)
		throws TMSException
	{
		setIO(input + DETECTOR_PIN_OFFSET, d);
	}

	/** Get a data detector (input from 1 to 24) */
	public Detector getDetector(int input) {
		return getDetectorAtPin(input + DETECTOR_PIN_OFFSET);
	}

	/** Get the queue detector bitmap */
	public byte[] getQueueBitmap() {
		byte[] bitmap = new byte[DETECTOR_INPUTS / 8];
		for(int d = 0; d < DETECTOR_INPUTS; d++) {
			DetectorImpl det = (DetectorImpl)getDetector(d + 1);
			if(det != null && det.getLaneType() == Detector.QUEUE)
				bitmap[d / 8] |= 1 << (d % 8);
		}
		return bitmap;
	}

	/** Set the controller firmware version */
	public void setVersion(int major, int minor) {
		setVersion(Integer.toString(major) + "." +
			Integer.toString(minor));
		TrafficDevice d = getDevice();
		if(major < 4 || (major == 4 && minor < 2) ||
			(major == 5 && minor < 4))
		{
			System.err.println("BUGGY 170 firmware! (version " +
				major + '.' + minor + ") at " + toString());
		}
	}

	/** Get the meter number on the controller */
	public int getMeterNumber(RampMeterImpl meter) {
		if(isActive()) {
			if(meter == getDevice())
				return 1;
			else if(meter == getMeter2())
				return 2;
		}
		return 0;
	}

	/** Check if the controller has been failed beyond threshold time */
	public boolean isFailedBeyondThreshold() {
		if(isFailed()) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MINUTE, -COMM_FAIL_THRESHOLD);
			return c.getTime().after(failTime);
		} else
			return false;
	}
}
