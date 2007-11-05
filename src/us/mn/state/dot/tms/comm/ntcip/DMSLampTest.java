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
 * Operation to perform a lamp test on a DMS
 *
 * @author Douglas Lau
 */
public class DMSLampTest extends DMSOperation {

	/** Create a new DMS lamp test object */
	public DMSLampTest(DMSImpl d) {
		super(COMMAND, d);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new InitialStatus();
	}

	/** Phase to query the initial status of lamp test activation */
	protected class InitialStatus extends Phase {

		/** Query the initial status of lamp test activation */
		protected Phase poll(AddressedMessage mess) throws IOException {
			LampTestActivation test = new LampTestActivation();
			mess.add(test);
			mess.getRequest();
			if(test.getInteger() == LampTestActivation.NO_TEST)
				return new ActivateLampTest();
			throw new NtcipException(test.toString());
		}
	}

	/** Phase to activate the lamp test */
	protected class ActivateLampTest extends Phase {

		/** Activate the lamp test */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new LampTestActivation());
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
			LampTestActivation test = new LampTestActivation();
			mess.add(test);
			mess.getRequest();
			if(test.getInteger() == LampTestActivation.NO_TEST)
				return new QueryLampStatus();
			if(++checks > MAX_CHECKS)
				throw new NtcipException(test.toString());
			else
				return this;
		}
	}

	/** Phase to query lamp status */
	protected class QueryLampStatus extends Phase {

		/** Query lamp status */
		protected Phase poll(AddressedMessage mess) throws IOException {
			LampFailureStuckOff l_off = new LampFailureStuckOff();
			LampFailureStuckOn l_on = new LampFailureStuckOn();
			mess.add(l_off);
			mess.add(l_on);
			mess.getRequest();
			String lamp = l_off.getValue();
			if(lamp.equals("OK"))
				lamp = l_on.getValue();
			else if(!l_on.getValue().equals("OK"))
				lamp += ", " + l_on.getValue();
			dms.setLampStatus(lamp);
			return null;
		}
	}
}
