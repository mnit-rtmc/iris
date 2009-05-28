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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.IOException;
import us.mn.state.dot.tms.server.WarningSignImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.DeviceOperation;

/**
 * Operation to command a 170 controller warning sign
 *
 * @author Douglas Lau
 */
public class WarningSignCommand extends DeviceOperation {

	/** Get the appropriate rate for the deployed state */
	static protected byte getDeployedRate(boolean d) {
		if(d)
			return MeterRate.CENTRAL;
		else
			return MeterRate.FORCED_FLASH;
	}

	/** Controller memory address */
	protected final int address;

	/** New "metering rate" for deploying to warning sign */
	protected final byte rate;

	/** Create a new warning sign command operation */
	public WarningSignCommand(WarningSignImpl s, boolean d) {
		super(COMMAND, s);
		address = Address.RAMP_METER_DATA + Address.OFF_REMOTE_RATE;
		rate = getDeployedRate(d);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new SetRate();
	}

	/** Phase to set the metering rate (which controls warning sign) */
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
