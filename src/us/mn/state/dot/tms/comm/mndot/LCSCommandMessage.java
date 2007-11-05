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
import us.mn.state.dot.tms.LaneControlSignalImpl;
import us.mn.state.dot.tms.LCSModule;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DeviceOperation;

/**
 * Operation to command a lane control signal
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @author Douglas Lau
 */
public class LCSCommandMessage extends DeviceOperation {

	/** LCS to send message to */
	protected final LaneControlSignalImpl lcs;

	/** Signal states to set */
	protected final int[] states;

	/** User that sent the command message */
	protected final String user;

	/** Create a new LCS command message */
	public LCSCommandMessage(LaneControlSignalImpl l, int[] s, String u) {
		super(COMMAND, l);
		lcs = l;
		states = s;
		user = u;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new TurnOffMetering();
	}

	/** Phase to turn off ramp metering */
	protected class TurnOffMetering extends Phase {

		/** Turn off ramp metering */
		protected Phase poll(AddressedMessage mess) throws IOException {
			int address = Address.RAMP_METER_DATA +
				Address.OFF_REMOTE_RATE;
			byte[] data = new byte[1];
			mess.add(new MemoryRequest(address, data));
			mess.setRequest();
			// FIXME: this should really be a separate phase
			address += Address.OFF_METER_2;
			mess.add(new MemoryRequest(address, data));
			mess.setRequest();
			return new SetOutputs();
		}
	}

	/** Phase to set the special function output bits */
	protected class SetOutputs extends Phase {

		/** Set the special function outputs */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] data = new byte[4];
			int settings = lcs.getSFOSettings(states);
			for(int i = 0; i < 4; i++) {
				Integer o = new Integer(settings >> (8 * i));
				data[i] = o.byteValue();
			}
			mess.add(new MemoryRequest(
				Address.SPECIAL_FUNCTION_OUTPUTS, data));
			mess.setRequest();
			return new SetMeteringState();
		}
	}

	/** Phase to set metering dependent on the signals */
	protected class SetMeteringState extends Phase {

		/** Set the ramp metering */
		protected Phase poll(AddressedMessage mess) throws IOException {
			int rate = 0;
			if(states[0] != LCSModule.DARK)
				rate = MeterRate.CENTRAL;
			int address = Address.RAMP_METER_DATA +
				Address.OFF_REMOTE_RATE;
			byte[] data = new byte[1];
			data[0] = (byte)rate;
			mess.add(new MemoryRequest(address, data));
			mess.setRequest();
			// FIXME: this should really be a separate phase
			address += Address.OFF_METER_2;
			mess.add(new MemoryRequest(address, data));
			mess.setRequest();
			return new QueryVerifies();
		}
	}

	/** Scan an array of bytes for one big-endian integer */
	static protected int scanBigEndian(byte[] b) {
		int v = 0;
		for(int i = 0; i < 4; i++)
			v |= (b[i] & 0xFF) << (i * 8);
		return v;
	}

	/** Phase to verify the LCS module state */
	protected class QueryVerifies extends Phase {

		/** Verify the state of the LCS modules */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] b = new byte[4];
			mess.add(new MemoryRequest(Address.LCS_VERIFIES, b));
			mess.getRequest();
			int v = scanBigEndian(b);
			lcs.processVerifyData(v, user);
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(!success)
			lcs.processVerifyData(-1, user);
		super.cleanup();
	}
}
