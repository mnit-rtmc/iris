/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCSIndication;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;

/**
 * Operation to send new indicaitons to a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class LCSSendIndications extends OpLCS {

	/** Indications to send */
	protected final Integer[] indications;

	/** User who sent the indications */
	protected final User user;

	/** Create a new operation to send LCS indications */
	public LCSSendIndications(LCSArrayImpl l, Integer[] ind, User u) {
		super(COMMAND, l);
		indications = ind;
		user = u;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new TurnOffDevices();
	}

	/** Phase to turn off devices */
	protected class TurnOffDevices extends Phase {

		/** Turn off devices */
		protected Phase poll(AddressedMessage mess) throws IOException {
			int address = Address.RAMP_METER_DATA +
				Address.OFF_REMOTE_RATE;
			byte[] data = new byte[Address.OFF_METER_2 + 1];
			data[Address.OFF_METER_1] = MeterRate.FORCED_FLASH;
			data[Address.OFF_METER_2] = MeterRate.FORCED_FLASH;
			mess.add(new MemoryRequest(address, data));
			mess.setRequest();
			return new SetOutputs();
		}
	}

	/** Phase to set the special function output bits */
	protected class SetOutputs extends Phase {

		/** Set the special function outputs */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] buffer = createSpecialFunctionBuffer();
			mess.add(new MemoryRequest(
				Address.SPECIAL_FUNCTION_OUTPUTS, buffer));
			mess.setRequest();
			if(isDark())
				return null;
			else
				return new TurnOnDevices();
		}
	}

	/** Phase to turn on devices */
	protected class TurnOnDevices extends Phase {

		/** Turn on devices */
		protected Phase poll(AddressedMessage mess) throws IOException {
			int address = Address.RAMP_METER_DATA +
				Address.OFF_REMOTE_RATE;
			byte[] data = new byte[Address.OFF_METER_2 + 1];
			data[Address.OFF_METER_1] = MeterRate.CENTRAL;
			data[Address.OFF_METER_2] = MeterRate.CENTRAL;
			mess.add(new MemoryRequest(address, data));
			mess.setRequest();
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(success)
			lcs_array.setIndicationsCurrent(indications, user);
		super.cleanup();
	}

	/** Test if the new indications are all DARK */
	protected boolean isDark() {
		for(int i: indications) {
			if(i != LaneUseIndication.DARK.ordinal())
				return false;
		}
		return true;
	}

	/** Create a special function output buffer for the indications */
	protected byte[] createSpecialFunctionBuffer() {
		final byte[] buffer = new byte[2];
		lcs_array.findIndications(new Checker<LCSIndication>() {
			public boolean check(LCSIndication li) {
				if(li.getController() == controller)
					checkIndication(li, buffer);
				return false;
			}
		});
		return buffer;
	}

	/** Check if an indication should be set */
	protected void checkIndication(LCSIndication li, byte[] buffer) {
		int i = li.getLcs().getLane() - 1;
		// We must check bounds here in case the LCSIndication
		// was added after the "indications" array was created
		if(i >= 0 && i < indications.length) {
			if(indications[i] == li.getIndication())
				setPin(buffer, li.getPin());
		}
	}

	/** Set the specified pin in a special function output buffer */
	protected void setPin(byte[] buffer, int pin) {
		int i = pin -
			Controller170Operation.SPECIAL_FUNCTION_OUTPUT_PIN;
		if(i >= 0 && i < 8)
			buffer[0] |= 1 << i;
		i -= 8;
		if(i >= 0 && i < 8)
			buffer[1] |= 1 << i;
	}
}
