/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ipaws;

import org.json.JSONObject;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.server.CapAlert;

/**
 * The alert processor stores alerts in the database and processes them.
 *
 * @author Douglas Lau
 */
public class AlertProcessor {

	/** Timer thread for IPAWS jobs */
	static private final Scheduler SCHED = new Scheduler("ipaws");

	/** Process one alert */
	public void processAlert(JSONObject ja) {
		String id = ja.getString("identifier");
		if (id != null) {
			CapAlert ca = new CapAlert(id, ja);
			SCHED.addJob(new Job() {
				public void perform() {
					ca.process();
				}
			});
		} else
			IpawsPoller.slog("identifier not found!");
	}
}
