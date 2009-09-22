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
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DmsActionHelper;
import us.mn.state.dot.tms.HolidayHelper;
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneActionHelper;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TimeActionHelper;

/**
 * Job to update action plans
 *
 * @author Douglas Lau
 */
public class ActionJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static protected final int OFFSET_SECS = 29;

	/** Calendar instance for calculating the minute of day */
	static protected final Calendar STAMP = Calendar.getInstance();

	/** Get the current minute of the day */
	static protected int minute_of_day() {
		synchronized(STAMP) {
			STAMP.setTimeInMillis(System.currentTimeMillis());
			return STAMP.get(Calendar.HOUR_OF_DAY) * 60 +
				STAMP.get(Calendar.MINUTE);
		}
	}

	/** Timer scheduler */
	protected final Scheduler timer;

	/** Create a new action job */
	public ActionJob(Scheduler t) {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
		timer = t;
	}

	/** Perform the action job */
	public void perform() {
		// FIXME: TimeAction should have an associated DayPlan
		if(!HolidayHelper.isHoliday(Calendar.getInstance()))
			performTimeActions();
		performDmsActions();
		performLaneActions();
	}

	/** Perform time actions */
	protected void performTimeActions() {
		final int minute = minute_of_day();
		TimeActionHelper.find(new Checker<TimeAction>() {
			public boolean check(TimeAction ta) {
				if(ta.getMinute() == minute)
					performTimeAction(ta);
				return false;
			}
		});
	}

	/** Perform a time action */
	protected void performTimeAction(TimeAction ta) {
		ActionPlan ap = ta.getActionPlan();
		if(ap instanceof ActionPlanImpl) {
			ActionPlanImpl api = (ActionPlanImpl)ap;
			if(api.getActive())
				api.setDeployedNotify(ta.getDeploy());
		}
	}

	/** Perform DMS actions */
	protected void performDmsActions() {
		DmsActionHelper.find(new Checker<DmsAction>() {
			public boolean check(DmsAction da) {
				ActionPlan ap = da.getActionPlan();
				if(ap.getActive()) {
					if(ap.getDeployed() == da.getOnDeploy())
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
				if(ap.getActive()) {
					LaneMarking m = la.getLaneMarking();
					if(m != null)
						m.setDeployed(ap.getDeployed());
				}
				return false;
			}
		});
	}
}
