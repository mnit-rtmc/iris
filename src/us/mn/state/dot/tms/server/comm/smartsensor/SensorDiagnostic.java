/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.smartsensor;

import java.io.IOException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.DiagnosticOperation;

/**
 * Sensor Diagnostic Operation
 *
 * @author Douglas Lau
 */
public class SensorDiagnostic extends DiagnosticOperation {

	/** Create a new sensor diagnostic operation */
	public SensorDiagnostic(ControllerImpl c) {
		super(c);
	}

	/** Begin the operation */
	public void begin() {
		phase = new QueryEventData();
	}

	/** Phase to query event data */
	protected class QueryEventData extends Phase {

		/** Perform some comm to test the connection */
		protected Phase poll(AddressedMessage mess) throws IOException {
			EventData events = new EventData();
			mess.add(events);
			mess.getRequest();
			return this;
		}
	}
}
