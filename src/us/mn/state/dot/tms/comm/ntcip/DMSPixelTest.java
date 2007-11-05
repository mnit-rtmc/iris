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
 * Operation to perform a pixel test on a DMS
 *
 * @author Douglas Lau
 */
public class DMSPixelTest extends DMSOperation {

	/** Create a new DMS pixel test object */
	public DMSPixelTest(DMSImpl d) {
		super(COMMAND, d);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new InitialStatus();
	}

	/** Phase to query the initial status of pixel test activation */
	protected class InitialStatus extends Phase {

		/** Query the initial status of pixel test activation */
		protected Phase poll(AddressedMessage mess) throws IOException {
			PixelTestActivation test = new PixelTestActivation();
			mess.add(test);
			mess.getRequest();
			if(test.getInteger() == PixelTestActivation.NO_TEST)
				return new ActivatePixelTest();
			throw new NtcipException(test.toString());
		}
	}

	/** Phase to activate the pixel test */
	protected class ActivatePixelTest extends Phase {

		/** Activate the pixel test */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new PixelTestActivation());
			mess.setRequest();
			return new CheckTestCompletion();
		}
	}

	/** Phase to check for test completion */
	protected class CheckTestCompletion extends Phase {

		/** Maximum number of checks for test completion */
		static protected final int MAX_CHECKS = 100;

		/** Count of checks made for test completion */
		protected int checks = 0;

		/** Check for test completion */
		protected Phase poll(AddressedMessage mess) throws IOException {
			PixelTestActivation test = new PixelTestActivation();
			mess.add(test);
			mess.getRequest();
			if(test.getInteger() == PixelTestActivation.NO_TEST)
				return null;
			if(++checks > MAX_CHECKS)
				throw new NtcipException(test.toString());
			else
				return this;
		}
	}
}
