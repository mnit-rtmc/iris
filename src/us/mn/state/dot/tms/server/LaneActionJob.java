/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2018  Minnesota Department of Transportation
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

import java.util.Iterator;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneActionHelper;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.PlanPhase;

/**
 * Job to perform lane actions.
 *
 * @author Douglas Lau
 */
public class LaneActionJob extends Job {

	/** Create a new lane action job */
	public LaneActionJob() {
		super(0);
	}

	/** Perform all lane actions */
	@Override
	public void perform() {
		Iterator<LaneAction> it = LaneActionHelper.iterator();
		while (it.hasNext()) {
			LaneAction la = it.next();
			ActionPlan ap = la.getActionPlan();
			if (ap.getActive())
				performLaneAction(la, ap.getPhase());
		}
	}

	/** Perform a lane action */
	private void performLaneAction(LaneAction la, PlanPhase phase) {
		LaneMarking lm = la.getLaneMarking();
		if (lm != null)
			lm.setDeployed(phase == la.getPhase());
	}
}
