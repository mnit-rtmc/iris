/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2014  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.WeatherPoller;

/**
 * Org815Poller is a weather poller for the Optical Scientific ORG-815 sensor.
 *
 * @author Douglas Lau
 */
public class Org815Poller extends MessagePoller<Org815Property>
	implements WeatherPoller
{
	/** Create a new ORG-815 poller */
	public Org815Poller(String n, Messenger m) {
		super(n, m);
	}

	/** Check if a drop address is valid */
	@Override
	public boolean isAddressValid(int drop) {
		return true;
	}

	/** Send settings to a weather sensor */
	@Override
	public void sendSettings(WeatherSensorImpl ws) {
		addOperation(new OpQuerySettings(ws));
	}

	/** Perform regular poll of one controller */
	@Override
	public void pollController(ControllerImpl c) {
		WeatherSensorImpl ws = c.getActiveWeatherSensor();
		if(ws != null);
			pollWeatherSensor(ws);
	}

	/** Perform regular poll of a weather sensor */
	private void pollWeatherSensor(WeatherSensorImpl ws) {
		addOperation(new OpQueryConditions(ws));
	}
}
