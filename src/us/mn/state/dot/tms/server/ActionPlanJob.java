/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ActionPlanHelper;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DmsActionHelper;
import us.mn.state.dot.tms.HolidayHelper;
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneActionHelper;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.MeterAction;
import us.mn.state.dot.tms.MeterActionHelper;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TimeActionHelper;

/**
 * Job to update action plans
 *
 * @author Douglas Lau
 */
public class ActionPlanJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static protected final int OFFSET_SECS = 29;

	/** Timer scheduler */
	protected final Scheduler timer;

	/** Create a new action plan job */
	public ActionPlanJob(Scheduler t) {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
		timer = t;
	}

	/** Perform the action plan job */
	public void perform() {
		updateActionPlanPhases();
		performTimeActions();
		performDmsActions();
		performLaneActions();
		performMeterActions();
	}

	/** Update the action plan phases */
	protected void updateActionPlanPhases() {
		ActionPlanHelper.find(new Checker<ActionPlan>() {
			public boolean check(ActionPlan ap) {
				if(ap instanceof ActionPlanImpl)
					((ActionPlanImpl)ap).updatePhase();
				return false;
			}
		});
	}

	/** Perform time actions */
	protected void performTimeActions() {
		final Calendar cal = TimeSteward.getCalendarInstance();
		final int min = TimeSteward.currentMinuteOfDayInt();
		TimeActionHelper.find(new Checker<TimeAction>() {
			public boolean check(TimeAction ta) {
				if(ta instanceof TimeActionImpl)
					((TimeActionImpl)ta).perform(cal, min);
				return false;
			}
		});
	}

	/** Perform DMS actions */
	protected void performDmsActions() {
		DmsActionHelper.find(new Checker<DmsAction>() {
			public boolean check(DmsAction da) {
				ActionPlan ap = da.getActionPlan();
				if(ap.getActive()) {
					if(ap.getPhase() == da.getPhase())
						performDmsAction(da);
				}
				return false;
			}
		});
		DMSHelper.find(new Checker<DMS>() {
			public boolean check(DMS dms) {
				if(dms instanceof DMSImpl)
					updateMessage((DMSImpl)dms);
				return false;
			}
		});
	}

	/** Perform a DMS action */
	protected void performDmsAction(final DmsAction da) {
		SignGroup sg = da.getSignGroup();
		DMSHelper.find(new Checker<DMS>() {
			public boolean check(DMS dms) {
				if(dms instanceof DMSImpl)
					performDmsAction((DMSImpl)dms, da);
				return false;
			}
		}, sg);
	}

	/** Perform a DMS action */
	protected void performDmsAction(final DMSImpl dms, final DmsAction da) {
		// We need to create a new Job here so that when performAction
		// is called, we're not holding the SONAR TypeNode locks for
		// DMS and DmsAction.
		timer.addJob(new Job() {
			public void perform() {
				dms.performAction(da);
			}
		});
	}

	/** Update the scheduled message for a DMS */
	protected void updateMessage(final DMSImpl dms) {
		// This needs to be in another Job so that it happens after any
		// performDmsAction jobs which might be queued
		timer.addJob(new Job() {
			public void perform() {
				dms.updateScheduledMessage();
			}
		});
	}

	/** Perform all lane actions */
	protected void performLaneActions() {
		LaneActionHelper.find(new Checker<LaneAction>() {
			public boolean check(LaneAction la) {
				ActionPlan ap = la.getActionPlan();
				if(ap.getActive())
					performLaneAction(la, ap.getPhase());
				return false;
			}
		});
	}

	/** Perform a lane action */
	private void performLaneAction(LaneAction la, PlanPhase phase) {
		LaneMarking lm = la.getLaneMarking();
		if(lm != null)
			lm.setDeployed(phase == la.getPhase());
	}

	/** Perform all meter actions */
	private void performMeterActions() {
		MeterActionHelper.find(new Checker<MeterAction>() {
			public boolean check(MeterAction ma) {
				ActionPlan ap = ma.getActionPlan();
				if(ap.getActive())
					performMeterAction(ma, ap.getPhase());
				return false;
			}
		});
	}

	/** Perform a meter action */
	private void performMeterAction(MeterAction ma, PlanPhase phase) {
		RampMeter rm = ma.getRampMeter();
		if(rm instanceof RampMeterImpl) {
			RampMeterImpl meter = (RampMeterImpl)rm;
			meter.setOperating(phase == ma.getPhase());
		}
	}
}
