/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ActionPlanHelper;

/**
 * Job to automatically update action plan phases
 *
 * @author Douglas Lau
 */
public class ActionPlanPhaseJob extends Job {

	/** Create a new action plan phase update job */
	public ActionPlanPhaseJob() {
		super(Calendar.SECOND, 5);
	}

	/** Perform the action plan job */
	@Override
	public void perform() {
		updateActionPlanPhases();
	}

	/** Update the action plan phases */
	private void updateActionPlanPhases() {
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
