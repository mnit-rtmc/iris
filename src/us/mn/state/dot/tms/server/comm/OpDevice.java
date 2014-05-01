/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2014  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DeviceImpl;

/**
 * An operation on a traffic device, such as a ramp meter or DMS.
 *
 * @author Douglas Lau
 */
abstract public class OpDevice<T extends ControllerProperty>
	extends OpController<T>
{
	/** Device on which to perform operation */
	protected final DeviceImpl device;

	/** Require exclusive access to device */
	private final boolean exclusive;

	/** Create a new device operation */
	protected OpDevice(PriorityLevel p, DeviceImpl d, boolean ex) {
		super(p, (ControllerImpl)d.getController(), d.getName());
		device = d;
		exclusive = ex;
	}

	/** Create a new device operation */
	protected OpDevice(PriorityLevel p, DeviceImpl d) {
		this(p, d, true);
	}

	/** Operation equality test */
	@Override
	public boolean equals(Object o) {
		return (o instanceof OpDevice) &&
		       (getClass() == o.getClass()) &&
		       ((OpDevice)o).device == device;
	}

	/** Phase to acquire exclusive ownership of the device */
	protected class AcquireDevice extends Phase<T> {

		/** Perform the acquire device phase */
		protected Phase<T> poll(CommMessage<T> mess)
			throws DeviceContentionException
		{
			OpDevice owner = device.acquire(OpDevice.this);
			if(owner != OpDevice.this)
				throw new DeviceContentionException(owner);
			return phaseTwo();
		}
	}

	/** Create the first phase of the operation */
	@Override
	protected final Phase<T> phaseOne() {
		return exclusive ? new AcquireDevice() : phaseTwo();
	}

	/** Create the second phase of the operation */
	abstract protected Phase<T> phaseTwo();

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if(exclusive)
			device.release(OpDevice.this);
		super.cleanup();
	}
}
