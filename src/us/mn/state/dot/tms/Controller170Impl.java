/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

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
		store.update(this, "cabinet", c);
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

	/** Get an active LCS for the controller */
	public LaneControlSignalImpl getActiveLcs() {
		if(isActive()) {
			for(ControllerIO io: io_pins.values()) {
				if(io instanceof LaneControlSignalImpl)
					return (LaneControlSignalImpl)io;
			}
		}
		return null;
	}

	/** Get an active warning sign for the controller */
	public WarningSignImpl getActiveWarningSign() {
		if(isActive()) {
			for(ControllerIO io: io_pins.values()) {
				if(io instanceof WarningSignImpl)
					return (WarningSignImpl)io;
			}
		}
		return null;
	}

	/** Assign a detector to an input */
	protected void setDetector(int input, DetectorImpl d)
		throws TMSException
	{
		setIO(input + DETECTOR_PIN_OFFSET, d);
	}

	/** Get a data detector (input from 1 to 24) */
	public DetectorImpl getDetector(int input) {
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
