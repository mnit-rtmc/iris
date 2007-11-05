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
package us.mn.state.dot.tms.comm.mndot;

import java.io.IOException;
import us.mn.state.dot.tms.TrafficDeviceImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DeviceOperation;

/**
 * Operation to update a 170 controller metering rate
 *
 * @author Douglas Lau
 */
public class SetMeterRate extends DeviceOperation {

	/** Controller memory address */
	protected final int address;

	/** New metering rate */
	protected final byte rate;

	/** Create a new set meter rate operation */
	public SetMeterRate(TrafficDeviceImpl d, int i, int r) {
		super(COMMAND, d);
		int a = Address.RAMP_METER_DATA + Address.OFF_REMOTE_RATE;
		if(i == 2)
			a += Address.OFF_METER_2;
		address = a;
		rate = (byte)r;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new SetRate();
	}

	/** Phase to set the metering rate */
	protected class SetRate extends Phase {

		/** Write the meter rate to the controller */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] data = { rate };
			mess.add(new MemoryRequest(address, data));
			mess.setRequest();
			return null;
		}
	}
}
