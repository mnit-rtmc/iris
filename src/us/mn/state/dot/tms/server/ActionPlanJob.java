/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ActionPlanHelper;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.BeaconAction;
import us.mn.state.dot.tms.BeaconActionHelper;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DmsActionHelper;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
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
	static private final DebugLog SCHED_LOG = new DebugLog("sched");

	/** Mapping of DMS actions */
	private final HashMap<DMSImpl, DmsAction> dms_actions =
		new HashMap<DMSImpl, DmsAction>();

	/** Mapping of ramp meter operating states */
	private final HashMap<RampMeterImpl, Boolean> meters =
		new HashMap<RampMeterImpl, Boolean>();

	/** Create a new action plan job */
	public ActionPlanJob() {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the action plan job */
	@Override
	public void perform() throws TMSException {
		updateActionPlanPhases();
		performTimeActions();
		performDmsActions();
		performBeaconActions();
		performLaneActions();
		performMeterActions();
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

	/** Perform time actions */
	private void performTimeActions() throws TMSException {
		Calendar cal = TimeSteward.getCalendarInstance();
		int min = TimeSteward.currentMinuteOfDayInt();
		Iterator<TimeAction> it = TimeActionHelper.iterator();
		while (it.hasNext()) {
			TimeAction ta = it.next();
			if (ta instanceof TimeActionImpl) {
				TimeActionImpl tai = (TimeActionImpl) ta;
				tai.perform(cal, min);
			}
		}
	}

	/** Log a DMS schedule message */
	private void logSched(DMS dms, String msg) {
		if (SCHED_LOG.isOpen())
			SCHED_LOG.log(dms.getName() + ": " + msg);
	}

	/** Perform DMS actions */
	private void performDmsActions() {
		dms_actions.clear();
		Iterator<DmsAction> it = DmsActionHelper.iterator();
		while (it.hasNext()) {
			DmsAction da = it.next();
			ActionPlan ap = da.getActionPlan();
			if (ap.getActive()) {
				if (ap.getPhase() == da.getPhase())
					performDmsAction(da);
			}
		}
		updateDmsMessages();
		dms_actions.clear();
	}

	/** Perform a DMS action */
	private void performDmsAction(DmsAction da) {
		SignGroup sg = da.getSignGroup();
		Iterator<DmsSignGroup> it = DmsSignGroupHelper.iterator();
		while (it.hasNext()) {
			DmsSignGroup dsg = it.next();
			if (dsg.getSignGroup() == sg) {
				DMS dms = dsg.getDms();
				if (dms instanceof DMSImpl)
					checkAction(da, (DMSImpl) dms);
			}
		}
	}

	/** Check an action for one DMS */
	private void checkAction(DmsAction da, DMSImpl dms) {
		if (SCHED_LOG.isOpen())
			logSched(dms, "checking " + da);
		if (shouldReplace(da, dms) && dms.checkAction(da))
			dms_actions.put(dms, da);
	}

	/** Check if an action should replace the current DMS action */
	private boolean shouldReplace(DmsAction da, DMSImpl dms) {
		DmsAction o = dms_actions.get(dms);
		return (null == o) ||
		       da.getActivationPriority() > o.getActivationPriority() ||
		       da.getRunTimePriority() >= o.getRunTimePriority();
	}

	/** Update the DMS messages */
	private void updateDmsMessages() {
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS dms = it.next();
			if (dms instanceof DMSImpl) {
				DMSImpl dmsi = (DMSImpl) dms;
				DmsAction da = dms_actions.get(dmsi);
				if (SCHED_LOG.isOpen())
					logSched(dms, "scheduling " + da);
				dmsi.setScheduledAction(da);
			}
		}
	}

	/** Perform all beacon actions */
	private void performBeaconActions() {
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

	/** Perform all lane actions */
	private void performLaneActions() {
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

	/** Perform all meter actions */
	private void performMeterActions() {
		meters.clear();
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
