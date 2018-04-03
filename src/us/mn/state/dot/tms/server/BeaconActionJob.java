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
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.BeaconAction;
import us.mn.state.dot.tms.BeaconActionHelper;
import us.mn.state.dot.tms.PlanPhase;

/**
 * Job to update action plans
 *
 * @author Douglas Lau
 */
public class BeaconActionJob extends Job {

	/** Create a new beacon action job */
	public BeaconActionJob() {
		super(0);
	}

	/** Perform all beacon actions */
	@Override
	public void perform() {
		Iterator<BeaconAction> it = BeaconActionHelper.iterator();
		while (it.hasNext()) {
			BeaconAction ba = it.next();
			ActionPlan ap = ba.getActionPlan();
			if (ap.getActive())
				performBeaconAction(ba, ap.getPhase());
		}
	}

	/** Perform a beacon action */
	private void performBeaconAction(BeaconAction ba, PlanPhase phase) {
		Beacon b = ba.getBeacon();
		if (b != null)
			b.setFlashing(phase == ba.getPhase());
	}
}
