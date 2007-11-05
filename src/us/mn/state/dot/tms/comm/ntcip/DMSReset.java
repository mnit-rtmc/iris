/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;

/**
 * Operatoin to reset a dynamic message sign
 *
 * @author Douglas Lau
 */
public class DMSReset extends DMSOperation {

	/** Create a new DMS reset object */
	public DMSReset(DMSImpl d) {
		super(COMMAND, d);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new ExecuteReset();
	}

	/** Phase to execute the DMS reset */
	protected class ExecuteReset extends Phase {

		/** Execute the DMS reset */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new DmsSWReset());
			mess.setRequest();
			try { Thread.sleep(5000); }
			catch(InterruptedException e) {}
			return new CheckResetCompletion();
		}
	}

	/** Phase to check for completion of the DMS reset */
	protected class CheckResetCompletion extends Phase {

		/** Maximum number of checks for completion */
		static protected final int MAX_CHECKS = 20;

		/** Count of checks made for completion */
		protected int checks = 0;

		/** Check for reset completion */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsSWReset reset = new DmsSWReset();
			mess.add(reset);
			mess.getRequest();
			if(reset.getInteger() == 0)
				return null;
			if(++checks > MAX_CHECKS)
				throw new NtcipException(reset.toString());
			else
				return this;
		}
	}
}
