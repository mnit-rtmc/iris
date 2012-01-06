/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
abstract public class OpDevice extends OpController {

	/** This operation; needed for inner Phase classes */
	protected final OpDevice operation;

	/** Device on which to perform operation */
	protected final DeviceImpl device;

	/** Create a new device operation */
	protected OpDevice(PriorityLevel p, DeviceImpl d) {
		super(p, (ControllerImpl)d.getController(), d.getName());
		operation = this;
		device = d;
	}

	/** Operation equality test */
	public boolean equals(Object o) {
		return (o instanceof OpDevice) &&
		       (getClass() == o.getClass()) &&
		       ((OpDevice)o).device == device;
	}

	/** Phase to acquire exclusive ownership of the device */
	protected class AcquireDevice extends Phase {

		/** Perform the acquire device phase */
		protected Phase poll(CommMessage mess)
			throws DeviceContentionException
		{
			OpDevice owner = device.acquire(operation);
			if(owner != operation)
				throw new DeviceContentionException(owner);
			return phaseTwo();
		}
	}

	/** Create the first phase of the operation */
	protected final Phase phaseOne() {
		return new AcquireDevice();
	}

	/** Create the second phase of the operation */
	abstract protected Phase phaseTwo();

	/** Cleanup the operation */
	public void cleanup() {
		device.release(operation);
		super.cleanup();
	}
}
