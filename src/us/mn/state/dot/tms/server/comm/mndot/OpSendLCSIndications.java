/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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
import java.util.Arrays;
import java.util.Iterator;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCSIndication;
import us.mn.state.dot.tms.LCSIndicationHelper;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to send new indicaitons to a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class OpSendLCSIndications extends OpLCS {

	/** Indications to send */
	protected final Integer[] indications;

	/** User who sent the indications */
	protected final User user;

	/** Create a new operation to send LCS indications */
	public OpSendLCSIndications(LCSArrayImpl l, Integer[] ind, User u) {
		super(PriorityLevel.COMMAND, l);
		indications = ind;
		user = u;
	}

	/** Operation equality test */
	@Override
	public boolean equals(Object o) {
		if (o instanceof OpSendLCSIndications) {
			OpSendLCSIndications op = (OpSendLCSIndications)o;
			return lcs_array == op.lcs_array &&
			       Arrays.equals(indications, op.indications);
		} else
			return false;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseTwo() {
		return new TurnOffDevices();
	}

	/** Phase to turn off devices */
	protected class TurnOffDevices extends Phase<MndotProperty> {

		/** Turn off devices */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			int address = Address.RAMP_METER_DATA +
				Address.OFF_REMOTE_RATE;
			byte[] data = new byte[Address.OFF_METER_2 + 1];
			data[Address.OFF_METER_1] = MeterRate.FORCED_FLASH;
			data[Address.OFF_METER_2] = MeterRate.FORCED_FLASH;
			mess.add(new MemoryProperty(address, data));
			mess.storeProps();
			return new SetOutputs();
		}
	}

	/** Phase to set the special function output bits */
	protected class SetOutputs extends Phase<MndotProperty> {

		/** Set the special function outputs */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			byte[] buffer = createSpecialFunctionBuffer();
			mess.add(new MemoryProperty(
				Address.SPECIAL_FUNCTION_OUTPUTS, buffer));
			mess.storeProps();
			if (isDark())
				return null;
			else
				return new TurnOnDevices();
		}
	}

	/** Phase to turn on devices */
	protected class TurnOnDevices extends Phase<MndotProperty> {

		/** Turn on devices */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			int address = Address.RAMP_METER_DATA +
				Address.OFF_REMOTE_RATE;
			byte[] data = new byte[Address.OFF_METER_2 + 1];
			data[Address.OFF_METER_1] = MeterRate.CENTRAL;
			data[Address.OFF_METER_2] = MeterRate.CENTRAL;
			mess.add(new MemoryProperty(address, data));
			mess.storeProps();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			lcs_array.setIndicationsCurrent(indications, user);
		super.cleanup();
	}

	/** Test if the new indications are all DARK */
	protected boolean isDark() {
		for (int i: indications) {
			if (i != LaneUseIndication.DARK.ordinal())
				return false;
		}
		return true;
	}

	/** Create a special function output buffer for the indications */
	protected byte[] createSpecialFunctionBuffer() {
		byte[] buffer = new byte[2];
		Iterator<LCSIndication> it = LCSIndicationHelper.iterator();
		while (it.hasNext()) {
			LCSIndication li = it.next();
			if (li.getLcs().getArray() == lcs_array) {
				if (li.getController() == controller)
					checkIndication(li, buffer);
			}
		}
		return buffer;
	}

	/** Check if an indication should be set */
	protected void checkIndication(LCSIndication li, byte[] buffer) {
		int i = li.getLcs().getLane() - 1;
		// We must check bounds here in case the LCSIndication
		// was added after the "indications" array was created
		if (i >= 0 && i < indications.length) {
			if (indications[i] == li.getIndication())
				Op170.setSpecFuncOutput(buffer, li.getPin());
		}
	}
}
