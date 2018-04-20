/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Test communication to a 170 controller.
 *
 * @author Douglas Lau
 */
public class OpTest170 extends Op170 {

	/** Create a new test operation */
	public OpTest170(ControllerImpl c) {
		super(PriorityLevel.DIAGNOSTIC, c);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseOne() {
		return new TestCommunication();
	}

	/** Phase to test communication */
	protected class TestCommunication extends Phase<MndotProperty> {

		/** Test communication */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
		{
			byte[] data = new byte[123];
			MemoryProperty mem = new MemoryProperty(0x100, data);
			mess.add(mem);
			mess.queryProps();
			return this;
		}
	}

	/** Get the error retry threshold */
	@Override
	public int getRetryThreshold() {
		return Integer.MAX_VALUE;
	}
}
