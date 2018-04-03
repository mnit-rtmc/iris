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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.MeterAction;
import us.mn.state.dot.tms.MeterActionHelper;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.RampMeter;

/**
 * Job to perform ramp meter actions.
 *
 * @author Douglas Lau
 */
public class MeterActionJob extends Job {

	/** Mapping of ramp meter operating states */
	private final HashMap<RampMeterImpl, Boolean> meters =
		new HashMap<RampMeterImpl, Boolean>();

	/** Create a new meter action job */
	public MeterActionJob() {
		super(0);
	}

	/** Perform all ramp meter actions */
	@Override
	public void perform() {
		Iterator<MeterAction> it = MeterActionHelper.iterator();
		while (it.hasNext()) {
			MeterAction ma = it.next();
			ActionPlan ap = ma.getActionPlan();
			if (ap.getActive())
				updateMeterMap(ma, ap.getPhase());
		}
		for (Map.Entry<RampMeterImpl, Boolean> e: meters.entrySet())
			e.getKey().setOperating(e.getValue());
	}

	/** Update the meter action map */
	private void updateMeterMap(MeterAction ma, PlanPhase phase) {
		RampMeter rm = ma.getRampMeter();
		if (rm instanceof RampMeterImpl) {
			RampMeterImpl meter = (RampMeterImpl) rm;
			boolean o = (phase == ma.getPhase());
			if (meters.containsKey(meter))
				o |= meters.get(meter);
			meters.put(meter, o);
		}
	}
}
