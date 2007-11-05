/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.mndot;

import java.io.IOException;
import us.mn.state.dot.tms.AlarmImpl;
import us.mn.state.dot.tms.Controller170Impl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.ControllerOperation;

/**
 * Operation to query the controller alarms.
 *
 * @author Douglas Lau
 */
public class QueryAlarms extends ControllerOperation {

	/** Parse alarm special function input data */
	static protected boolean[] parseAlarms(byte[] data) {
		boolean[] alarms = new boolean[10];
		for(int i = 0; i < 5; i++)
			alarms[i] = 1 == ((data[0] >> (i + 3)) & 1);
		for(int i = 0; i < 5; i++)
			alarms[i + 5] = 1 == ((data[1] >> i) & 1);
		return alarms;
	}

	/** Create a query alarm operation */
	public QueryAlarms(Controller170Impl c) {
		super(DEVICE_DATA, c);
	}

	/** Begin the operation */
	public void begin() {
		phase = new GetAlarms();
	}

	/** Phase to query the alarm states */
	protected class GetAlarms extends Phase {

		/** Query the meter red time */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] data = new byte[2];
			mess.add(new MemoryRequest(Address.ALARM_INPUTS, data));
			mess.getRequest();
			boolean[] alarms = parseAlarms(data);
			for(int i = 0; i < 10; i++) {
				AlarmImpl a = (AlarmImpl)controller.getAlarm(
					i + Address.ALARM_PIN);
				if(a != null)
					a.setState(alarms[i]);
			}
			return null;
		}
	}
}
