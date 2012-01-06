/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.org815;

import java.io.EOFException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.WeatherPoller;

/**
 * Org815Poller is a weather poller for the Optical Scientific ORG-815 sensor.
 *
 * @author Douglas Lau
 */
public class Org815Poller extends MessagePoller implements WeatherPoller {

	/** Create a new ORG-815 poller */
	public Org815Poller(String n, Messenger m) {
		super(n, m);
	}

	/** Create a new message for the specified controller */
	public CommMessage createMessage(ControllerImpl c)
		throws EOFException
	{
		return new Message(messenger.getOutputStream(c),
			messenger.getInputStream(c));
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		return true;
	}

	/** Send settings to a weather sensor */
	public void sendSettings(WeatherSensorImpl ws) {
		addOperation(new OpQuerySettings(ws));
	}

	/** Query current weather conditions */
	public void queryConditions(WeatherSensorImpl ws) {
		addOperation(new OpQueryConditions(ws));
	}
}
