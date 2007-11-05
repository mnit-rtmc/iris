/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2007  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DiagnosticOperation;

/**
 * Operatino to test communications to 170 controllers
 *
 * @author Douglas Lau
 */
public class Diagnostic170 extends DiagnosticOperation {

	/** Create a new diagnostic 170 operation */
	public Diagnostic170(ControllerImpl c) {
		super(c);
	}

	/** Begin the operation */
	public void begin() {
		phase = new QueryMemoryBlock();
	}

	/** Phase to query a large block of memory */
	protected class QueryMemoryBlock extends Phase {

		/** Perform communication diagnostic */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] buf = new byte[123];
			mess.add(new MemoryRequest(0x0100, buf));
			mess.getRequest();
			return this;
		}
	}
}
