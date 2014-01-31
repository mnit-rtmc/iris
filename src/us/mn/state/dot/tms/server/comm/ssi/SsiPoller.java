/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
 * Copyright (C) 2012-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ssi;

import java.util.HashMap;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;

/**
 * SSI RWIS poller, which periodically reads SSI data via http.
 *
 * @author Michael Darter
 */
public class SsiPoller extends MessagePoller {

	/** SSI logger */
	static public final DebugLog LOG = new DebugLog("ssi");

	/** Log an SSI message */
	static public void log(String msg) {
		LOG.log(msg);
	}

	/** Mapping of site_id to most recent RWIS records */
	private final HashMap<String, RwisRec> records =
		new HashMap<String, RwisRec>();

	/** Create a new poller */
	public SsiPoller(String n, Messenger m) {
		super(n, m);
	}

	/** Create a new message for the specified controller, 
	 *  called by MessagePoller.doPoll(). */
	@Override
	public CommMessage createMessage(ControllerImpl c) {
		return new SsiMessage(messenger);
	}

	/** Drop address is always valid */
	@Override
	public boolean isAddressValid(int drop) {
		return true;
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
		addOperation(new OpRead(ws, records));
	}
}
