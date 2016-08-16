/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import java.io.IOException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.units.Interval;

/**
 * Operation to hang up a modem.
 *
 * @author Douglas Lau
 */
public class OpHangUp<T extends ControllerProperty> extends OpController<T> {

	/** Quiescent modem interval */
	static private final Interval QUIESCENT_INTVL = new Interval(10);

	/** Modem messenger */
	private final ModemMessenger messenger;

	/** Create a new hang-up operation */
	public OpHangUp(ControllerImpl c, ModemMessenger mm) {
		super(PriorityLevel.DIAGNOSTIC, c);
		messenger = mm;
	}

	/** Operation equality test */
	@Override
	public boolean equals(Object o) {
		// Only one needed per comm thread
		return o instanceof OpHangUp;
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<T> phaseOne() {
		return new WaitQuiescent();
	}

	/** Phase to wait for quiescent messenger */
	protected class WaitQuiescent extends Phase<T> {
		protected Phase<T> poll(CommMessage<T> mess) throws IOException{
			switch (messenger.getState()) {
			case online:
				if (isQuiescent())
					throw new HangUpException();
				// else fall through ...
			case connecting:
				TimeSteward.sleep_well(200);
				return this;
			default:
				// Hang up not needed
				return null;
			}
		}
	}

	/** Check if the messenger is quiescent */
	private boolean isQuiescent() {
		long activity = messenger.getActivity();
		long now = TimeSteward.currentTimeMillis();
		return now > activity + QUIESCENT_INTVL.ms();
	}
}
