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
package us.mn.state.dot.tms.comm.mndot;

import java.io.IOException;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.LaneControlSignalImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DeviceOperation;

/**
 * Operation to query the verify inputs from a Lane Control Signal.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @author Douglas Lau
 */
public class LCSQuerySignal extends DeviceOperation {

	/** LCS to query */
	protected final LaneControlSignalImpl lcs;

	/** 30-Second completer */
	protected final Completer completer;

	/** Create a new operation to query the LCS */
	public LCSQuerySignal(LaneControlSignalImpl l, Completer comp) {
		super(DATA_30_SEC, l);
		lcs = l;
		completer = comp;
		completer.up();
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new QueryVerifies();
	}

	/** Scan an array of bytes for one big-endian integer */
	static protected int scanBigEndian(byte[] b) {
		int v = 0;
		for(int i = 0; i < 4; i++)
			v |= (b[i] & 0xFF) << (i * 8);
		return v;
	}

	/** Phase to query the LCS verifies */
	protected class QueryVerifies extends Phase {

		/** Verify the module states */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] b = new byte[4];
			mess.add(new MemoryRequest(Address.LCS_VERIFIES, b));
			mess.getRequest();
			int v = scanBigEndian(b);
			lcs.processVerifyData(v);
			if(!lcs.properStatus())
				lcs.commandSign();
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(!success) {
			lcs.processVerifyData(-1);
			lcs.setStatus(status);
		}
		super.cleanup();
		completer.down();
	}
}
