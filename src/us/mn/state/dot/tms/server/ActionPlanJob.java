/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2019  Minnesota Department of Transportation
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
import java.util.Iterator;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ActionPlanHelper;
import us.mn.state.dot.tms.TMSException;

/**
 * Job to update action plans
 *
 * @author Douglas Lau
 */
public class ActionPlanJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static private final int OFFSET_SECS = 29;

	/** Schedule debug log */
	static public final DebugLog SCHED_LOG = new DebugLog("sched");

	/** TIMER Scheduler */
	private final Scheduler timer;

	/** Create a new action plan job */
	public ActionPlanJob(Scheduler t) {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
		timer = t;
	}

	/** Perform the action plan job */
	@Override
	public void perform() throws TMSException {
		timer.addJob(new TimeActionJob());
		timer.addJob(new DmsActionJob(SCHED_LOG));
		timer.addJob(new BeaconActionJob());
		timer.addJob(new CameraActionJob());
		timer.addJob(new LaneActionJob());
		timer.addJob(new MeterActionJob());
		updateActionPlanPhases();
	}

	/** Update the action plan phases */
	private void updateActionPlanPhases() throws TMSException {
		Iterator<ActionPlan> it = ActionPlanHelper.iterator();
		while (it.hasNext()) {
			ActionPlan ap = it.next();
			if (ap instanceof ActionPlanImpl) {
				ActionPlanImpl api = (ActionPlanImpl) ap;
				api.updatePhase();
			}
		}
	}
}
