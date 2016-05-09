/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.stc;

import java.io.IOException;
import java.net.SocketTimeoutException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.GateArmImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operatoin to reset a gate arm.
 *
 * @author Douglas Lau
 */
public class OpResetGate extends OpSTC {

	/** Timeout (ms) to wait for a controller reset */
	static private final long RESET_TIMEOUT = 45 * 1000;

	/** Create a new gate arm reset operation */
	public OpResetGate(GateArmImpl d) {
		super(PriorityLevel.COMMAND, d);
	}

	/** Create the second phase of the operation */
	protected Phase<STCProperty> phaseTwo() {
		return new ExecuteReset();
	}

	/** Phase to execute the reset */
	protected class ExecuteReset extends Phase<STCProperty> {

		/** Execute the reset */
		protected Phase<STCProperty> poll(CommMessage<STCProperty> mess)
			throws IOException
		{
			ResetProperty reset = new ResetProperty(password());
			mess.add(reset);
			mess.storeProps();
			return new QueryVersion();
		}
	}

	/** Phase to query the version (and wait for reset completion) */
	protected class QueryVersion extends Phase<STCProperty> {

		/** Time to stop checking if the test has completed */
		private final long expire = TimeSteward.currentTimeMillis() + 
			RESET_TIMEOUT;

		/** Query the version */
		protected Phase<STCProperty> poll(CommMessage<STCProperty> mess)
			throws IOException
		{
			VersionProperty v = new VersionProperty(password());
			mess.add(v);
			try {
				mess.queryProps();
				gate_arm.setVersion(v.getVersion());
				return null;
			}
			catch (SocketTimeoutException e) {
				// Controller must still be offline
			}
			if (TimeSteward.currentTimeMillis() > expire) {
				mess.logError("reset timeout expired -- " +
					"giving up");
				return null;
			} else
				return this;
		}
	}
}
