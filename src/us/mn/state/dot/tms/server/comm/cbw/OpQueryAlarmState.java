/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2026  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cbw;

import java.io.IOException;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.server.AlarmImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Query the state of an alarm
 *
 * @author Douglas Lau
 */
public class OpQueryAlarmState extends OpController<CBWProperty> {

	/** Alarm */
	private final AlarmImpl alarm;

	/** Relay/input state property */
	private final CBWProperty prop;

	/** Create a new query alarm state operation */
	public OpQueryAlarmState(AlarmImpl a, ControllerImpl c) {
		super(PriorityLevel.POLL_HIGH, c);
		alarm = a;
		String m = ControllerHelper.getSetup(c, "hw", "model");
		Model mdl = Model.fromValue(m);
		prop = new CBWProperty(mdl.statePath());
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<CBWProperty> phaseOne() {
		return new QueryAlarm();
	}

	/** Phase to query the alarm status */
	private class QueryAlarm extends Phase<CBWProperty> {

		/** Query the alarm status */
		protected Phase<CBWProperty> poll(
			CommMessage<CBWProperty> mess) throws IOException
		{
			mess.add(prop);
			mess.queryProps();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			alarm.setStateNotify(getState());
		super.cleanup();
	}

	/** Get the alarm input state */
	private boolean getState() {
		return prop.getInput(alarm.getPin());
	}
}
