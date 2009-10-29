/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.util.Calendar;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentHelper;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Job to periodically reap dead stuff.
 *
 * @author Douglas Lau
 */
public class ReaperJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static protected final int OFFSET_SECS = 27;

	/** Create a new job to reap dead stuff */
	public ReaperJob() {
		super(Calendar.MINUTE, 1, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the reaper job */
	public void perform() {
		reapIncidents();
	}

	/** Reap incidents which have been cleared for awhile */
	protected void reapIncidents() {
		IncidentHelper.find(new Checker<Incident>() {
			public boolean check(Incident inc) {
				if(inc instanceof IncidentImpl)
					reapIncident((IncidentImpl)inc);
				return false;
			}
		});
	}

	/** Reap an incident */
	protected void reapIncident(IncidentImpl inc) {
		if(inc.getCleared()) {
			if(inc.getClearTime() + getIncidentClearThreshold() <
			   System.currentTimeMillis())
				MainServer.server.removeObject(inc);
		}
	}

	/** Get the incident clear time threshold (ms) */
	protected long getIncidentClearThreshold() {
		int secs = SystemAttrEnum.INCIDENT_CLEAR_SECS.getInt();
		return secs * (long)1000;
	}
}
