/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.IOException;
import us.mn.state.dot.tms.server.AlarmImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query the controller alarms.
 *
 * @author Douglas Lau
 */
public class OpQueryAlarms extends Op170 {

	/** Parse alarm special function input data */
	static private boolean[] parseAlarms(byte[] data) {
		boolean[] alarms = new boolean[10];
		for (int i = 0; i < 5; i++)
			alarms[i] = 1 == ((data[0] >> (i + 3)) & 1);
		for (int i = 0; i < 5; i++)
			alarms[i + 5] = 1 == ((data[1] >> i) & 1);
		return alarms;
	}

	/** Create a query alarm operation */
	public OpQueryAlarms(ControllerImpl c) {
		super(PriorityLevel.DEVICE_DATA, c);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseOne() {
		return new GetAlarms();
	}

	/** Phase to query the alarm states */
	protected class GetAlarms extends Phase<MndotProperty> {

		/** Query the meter red time */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			byte[] data = new byte[2];
			MemoryProperty alarm_mem = new MemoryProperty(
				Address.ALARM_INPUTS, data);
			mess.add(alarm_mem);
			mess.queryProps();
			boolean[] alarms = parseAlarms(data);
			for (int i = 0; i < 10; i++) {
				int pin = ALARM_PIN + i;
				AlarmImpl a = controller.getAlarm(pin);
				if (a != null)
					a.setStateNotify(alarms[i]);
			}
			return null;
		}
	}
}
