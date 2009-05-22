/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm;

import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DeviceImpl;

/**
 * An operation on a traffic device, such as a ramp meter or DMS.
 *
 * @author Douglas Lau
 */
abstract public class DeviceOperation extends ControllerOperation {

	/** This operation; needed for inner Phase classes */
	protected final DeviceOperation operation;

	/** Device on which to perform operation */
	protected final DeviceImpl device;

	/** Create a new device operation */
	protected DeviceOperation(int p, DeviceImpl d) {
		super(p, (ControllerImpl)d.getController(), d.getName());
		operation = this;
		device = d;
	}

	/** Phase to acquire exclusive ownership of the device */
	protected class AcquireDevice extends Phase {

		/** Perform the acquire device phase */
		protected Phase poll(AddressedMessage mess)
			throws DeviceContentionException
		{
			DeviceOperation owner = device.acquire(operation);
			if(owner != operation)
				throw new DeviceContentionException(owner);
			return phaseOne();
		}
	}

	/** Create the first real phase of the operation */
	abstract protected Phase phaseOne();

	/** Begin the operation */
	public final void begin() {
		phase = new AcquireDevice();
	}

	/** Cleanup the operation */
	public void cleanup() {
		device.release(operation);
		super.cleanup();
	}
}
