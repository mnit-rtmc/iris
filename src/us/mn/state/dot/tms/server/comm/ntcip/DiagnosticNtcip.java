/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.DiagnosticOperation;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;

/**
 * Operation to test communication to Ntcip devices
 *
 * @author Douglas Lau
 */
public class DiagnosticNtcip extends DiagnosticOperation {

	/** Create a new diagnostic ntcip object */
	public DiagnosticNtcip(ControllerImpl c) {
		super(c);
	}

	/** Begin the operation */
	public void begin() {
		phase = new QueryTimeRemaining();
	}

	/** Phase to query the time remaining for the current message */
	protected class QueryTimeRemaining extends Phase {

		/** Perform some comm to test the connection */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsMessageTimeRemaining time =
				new DmsMessageTimeRemaining();
			mess.add(time);
			mess.getRequest();
			return this;
		}
	}
}
