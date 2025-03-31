/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsHelper;
import us.mn.state.dot.tms.LcsIndication;
import us.mn.state.dot.tms.LcsLock;
import us.mn.state.dot.tms.LcsState;
import us.mn.state.dot.tms.server.LcsImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to send new indicaitons to a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class OpSendLCSIndications extends OpLCS {

	/** LCS lock (JSON) */
	private final LcsLock lock;

	/** Indications to send */
	private final int[] indications;

	/** Create a new operation to send LCS indications */
	public OpSendLCSIndications(LcsImpl l, String lk) {
		super(PriorityLevel.COMMAND, l);
		lock = new LcsLock(lk);
		int[] ind = lock.optIndications();
		indications = (ind != null)
			? ind
			: LcsHelper.makeIndications(l, LcsIndication.DARK);
	}

	/** Operation equality test */
	@Override
	public boolean equals(Object o) {
		if (o instanceof OpSendLCSIndications) {
			OpSendLCSIndications op = (OpSendLCSIndications) o;
			return (lcs == op.lcs) && lock.equals(op.lock);
		} else
			return false;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseTwo() {
		return new TurnOffDevices();
	}

	/** Phase to turn off devices */
	private class TurnOffDevices extends Phase<MndotProperty> {

		/** Turn off devices */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
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
	private class SetOutputs extends Phase<MndotProperty> {

		/** Set the special function outputs */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
		{
			byte[] buffer = createSpecialFunctionBuffer();
			mess.add(new MemoryProperty(
				Address.SPECIAL_FUNCTION_OUTPUTS, buffer));
			mess.storeProps();
			return (!isDark()) ? new TurnOnDevices() : null;
		}
	}

	/** Test if the new indications are all DARK */
	private boolean isDark() {
		for (int i: indications) {
			if (i != LcsIndication.DARK.ordinal())
				return false;
		}
		return true;
	}

	/** Phase to turn on devices */
	private class TurnOnDevices extends Phase<MndotProperty> {

		/** Turn on devices */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
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
			lcs.setIndicationsNotify(indications);
		super.cleanup();
	}

	/** Create a special function output buffer for the indications */
	private byte[] createSpecialFunctionBuffer() {
		byte[] buffer = new byte[2];
		for (LcsState ls: LcsHelper.lookupStates(lcs)) {
			if (ls.getController() == controller)
				checkIndication(ls, buffer);
		}
		return buffer;
	}

	/** Check if an indication should be set */
	private void checkIndication(LcsState ls, byte[] buffer) {
		int ln = ls.getLane() - 1;
		// We must check bounds here in case the LcsState
		// was added after the "indications" array was created
		if (ln >= 0 && ln < indications.length) {
			if (indications[ln] == ls.getIndication())
				Op170.setSpecFuncOutput(buffer, ls.getPin());
		}
	}
}
