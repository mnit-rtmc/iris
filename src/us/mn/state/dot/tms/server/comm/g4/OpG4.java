/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Iteris Inc.
 * Copyright (C) 2009-2026  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.g4;

import java.io.IOException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation for G4 device
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
abstract public class OpG4 extends OpController<G4Property> {

	/** RTC property */
	protected final RTCProperty rtc = new RTCProperty();

	/** Create a new G4 operation */
	protected OpG4(PriorityLevel p, ControllerImpl c) {
		super(p, c);
	}

	/** Phase to store the RTC */
	protected class StoreRTC extends Phase<G4Property> {

		/** Store the RTC */
		protected Phase<G4Property> poll(
			CommMessage<G4Property> mess) throws IOException
		{
			rtc.setStamp(TimeSteward.currentTimeMillis());
			mess.add(rtc);
			mess.storeProps();
			return null;
		}
	}
}
