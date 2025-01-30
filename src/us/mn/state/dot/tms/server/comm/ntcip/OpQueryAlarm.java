/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.AlarmImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query an alarm.
 *
 * @author Douglas Lau
 */
public class OpQueryAlarm extends OpController {

	/** Alarm to query */
	private final AlarmImpl alarm;

	/** Create a new query alarm operation */
	public OpQueryAlarm(AlarmImpl a, ControllerImpl c) {
		super(PriorityLevel.POLL_LOW, c);
		alarm = a;
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase phaseOne() {
		return new QueryAlarmStatus();
	}

	/** Phase to query the alarm status */
	private class QueryAlarmStatus extends Phase {

		/** Query the alarm status */
		protected Phase poll(CommMessage mess) throws IOException {
			// FIXME: query auxIOv2 objects
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			updateAlarmStatus();
		super.cleanup();
	}

	/** Update the alarm status */
	private void updateAlarmStatus() {
		// FIXME: update the alarm
	}
}
