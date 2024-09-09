/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
 * Copyright (C) 2018  Iteris Inc.
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.BeaconHelper;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DeviceAction;
import us.mn.state.dot.tms.DeviceActionHelper;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.LaneMarkingHelper;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.event.ActionPlanEvent;
import static us.mn.state.dot.tms.server.ActionPlanSystem.sendEmailAlert;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * An action plan is a set of actions which can be deployed together.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class ActionPlanImpl extends BaseObjectImpl implements ActionPlan {

	/** Create a unique ActionPlan record name */
	static public String createUniqueName(String template) {
		UniqueNameCreator unc = new UniqueNameCreator(template, 16,
			(n)->lookupActionPlan(n));
		return unc.createUniqueName();
	}

	/** Load all the action plans */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, ActionPlanImpl.class);
		store.query("SELECT name, notes, sync_actions, sticky, " +
			"ignore_auto_fail, active, default_phase, phase " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new ActionPlanImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("notes", notes);
		map.put("sync_actions", sync_actions);
		map.put("sticky", sticky);
		map.put("ignore_auto_fail", ignore_auto_fail);
		map.put("active", active);
		map.put("default_phase", default_phase);
		map.put("phase", phase);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new action plan */
	public ActionPlanImpl(String n) {
		super(n);
	}

	/** Create an action plan */
	private ActionPlanImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // notes
		     row.getBoolean(3), // sync_actions
		     row.getBoolean(4), // sticky
		     row.getBoolean(5), // ignore_auto_fail
		     row.getBoolean(6), // active
		     row.getString(7),  // default_phase
		     row.getString(8)   // phase
		);
	}

	/** Create an action plan */
	protected ActionPlanImpl(String n, String nt, boolean sa, boolean st,
		boolean ig, boolean a, String dp, String p)
	{
		this(n);
		notes = nt;
		sync_actions = sa;
		sticky = st;
		ignore_auto_fail = ig;
		active = a;
		default_phase = lookupPlanPhase(dp);
		phase = lookupPlanPhase(p);
	}

	/** Administrator notes */
	private String notes;

	/** Set administrator notes (including hashtags) */
	@Override
	public void setNotes(String n) {
		notes = n;
	}

	/** Set administrator notes (including hashtags) */
	public void doSetNotes(String n) throws TMSException {
		if (!objectEquals(n, notes)) {
			store.update(this, "notes", n);
			setNotes(n);
		}
	}

	/** Get administrator notes (including hashtags) */
	@Override
	public String getNotes() {
		return notes;
	}

	/** Sync actions flag */
	private boolean sync_actions;

	/** Set the sync actions flag */
	@Override
	public void setSyncActions(boolean s) {
		sync_actions = s;
	}

	/** Set the sync actions flag */
	public void doSetSyncActions(boolean s) throws TMSException {
		if (s != sync_actions) {
			store.update(this, "sync_actions", s);
			setSyncActions(s);
		}
	}

	/** Get the sync actions flag */
	@Override
	public boolean getSyncActions() {
		return sync_actions;
	}

	/** Sticky flag */
	private boolean sticky;

	/** Set the sticky flag */
	@Override
	public void setSticky(boolean s) {
		sticky = s;
	}

	/** Set the sticky flag */
	public void doSetSticky(boolean s) throws TMSException {
		if (s != sticky) {
			store.update(this, "sticky", s);
			setSticky(s);
		}
	}

	/** Get the sticky flag */
	@Override
	public boolean getSticky() {
		return sticky;
	}

	/** Ignore auto-fail flag */
	private boolean ignore_auto_fail;

	/** Set ignore auto-fail flag */
	@Override
	public void setIgnoreAutoFail(boolean ig) {
		ignore_auto_fail = ig;
	}

	/** Set ignore auto-fail flag */
	public void doSetIgnoreAutoFail(boolean ig) throws TMSException {
		if (ig != ignore_auto_fail) {
			store.update(this, "ignore_auto_fail", ig);
			setIgnoreAutoFail(ig);
		}
	}

	/** Get ignore auto-fail flag */
	@Override
	public boolean getIgnoreAutoFail() {
		return ignore_auto_fail;
	}

	/** Active status */
	private boolean active;

	/** Set the active status */
	@Override
	public void setActive(boolean a) {
		active = a;
		String un = getProcUser();
		EventType et = (a ? EventType.ACTION_PLAN_ACTIVATED : 
			EventType.ACTION_PLAN_DEACTIVATED);
		logEvent(new ActionPlanEvent(et, getName(), un));
		sendEmailAlert(un, a, getName());
	}

	/** Set the active status */
	public void doSetActive(boolean a) throws TMSException {
		if (a != active) {
			if (a)
				setPhaseNotify(default_phase);
			store.update(this, "active", a);
			setActive(a);
		}
	}

	/** Set active and schedule phase */
	public void setActiveScheduledNotify(boolean a) throws TMSException {
		if (a)
			setPhaseNotify(getScheduledPhase());
		if (a != active) {
			store.update(this, "active", a);
			setActive(a);
			notifyAttribute("active");
		}
	}

	/** Get the currently scheduled phase */
	private PlanPhase getScheduledPhase() {
		// Use time in thirty seconds to avoid missing time actions
		long now = TimeSteward.currentTimeMillis() + 30 * 1000;
		TimeAction ta = TimeActionHelper.getMostRecentAction(this,
			new Date(now));
		return (ta != null) ? ta.getPhase() : default_phase;
	}

	/** Get the active status */
	@Override
	public boolean getActive() {
		return active;
	}

	/** Default plan phase */
	private PlanPhase default_phase;

	/** Set the default phase */
	@Override
	public void setDefaultPhase(PlanPhase dp) {
		default_phase = dp;
	}

	/** Set the default phase */
	public void doSetDefaultPhase(PlanPhase dp) throws TMSException {
		if (dp != default_phase) {
			store.update(this, "default_phase", dp);
			setDefaultPhase(dp);
		}
	}

	/** Get the default phase */
	@Override
	public PlanPhase getDefaultPhase() {
		return default_phase;
	}

	/** Current plan phase */
	private PlanPhase phase;

	/** Time stamp for last phase change */
	private long phase_time = TimeSteward.currentTimeMillis();

	/** Set the phase */
	@Override
	public void setPhase(PlanPhase p) {
		phase = p;
		phase_time = TimeSteward.currentTimeMillis();
		EventType et = EventType.ACTION_PLAN_PHASE_CHANGED;
		logEvent(new ActionPlanEvent(et, getName(), p.toString()));
	}

	/**
	 * Set the phase.  If sync actions are enabled, the phase is set only
	 * only if all dms, beacon, lane, and meter actions are valid.
	 */
	public void doSetPhase(PlanPhase p) throws TMSException {
		if (p != phase) {
			if (queryPermAccess() < 2)
				throw new ChangeVetoException("NOT PERMITTED");
			if (getSyncActions())
				validateDeviceActions(); // throws exception
			store.update(this, "phase", p);
			setPhase(p);
		}
	}

	/** Get the phase */
	@Override
	public PlanPhase getPhase() {
		return phase;
	}

	/** Set the deployed phase (and notify clients) */
	public boolean setPhaseNotify(PlanPhase p) throws TMSException {
		boolean change = (p != phase);
		if (change)
			doSetPhaseNotify(p);
		return change;
	}

	/** Set the deployed phase with notification */
	private void doSetPhaseNotify(PlanPhase p) throws TMSException {
		doSetPhase(p);
		notifyAttribute("phase");
	}

	/**
	 * Validate that all device actions are deployable.
	 * @throws ChangeVetoException If any DeviceAction for this
	 *         ActionPlan is not deployable.
	 */
	private void validateDeviceActions() throws ChangeVetoException {
		Iterator<DeviceAction> it = DeviceActionHelper.iterator();
		while (it.hasNext()) {
			DeviceAction da = it.next();
			if (da.getActionPlan() == this && !isDeployable(da)) {
				throw new ChangeVetoException("Device action " +
					da.getName() + " not deployable");
			}
		}
	}

	/** Check if a device action is deployable */
	private boolean isDeployable(DeviceAction da) {
		String ht = da.getHashtag();
		// FIXME: any way to validate camera actions?
		return areBeaconsDeployable(ht)
		    && areDmsDeployable(ht)
		    && areLaneMarkingsDeployable(ht)
		    && areRampMetersDeployable(ht);
	}

	/** Check if all beacons for a hashtag are deployable */
	private boolean areBeaconsDeployable(String ht) {
		Iterator<Beacon> it = BeaconHelper.iterator();
		while (it.hasNext()) {
			Beacon b = it.next();
			if (b instanceof BeaconImpl) {
				BeaconImpl bi = (BeaconImpl) b;
				if (bi.isFailed()) {
					if (new Hashtags(bi.getNotes())
					   .contains(ht))
						return false;
				}
			}
		}
		return true;
	}

	/** Check if all DMS for a hashtag are deployable */
	private boolean areDmsDeployable(String ht) {
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS d = it.next();
			if (d instanceof DMSImpl) {
				DMSImpl dms = (DMSImpl) d;
				if (dms.hasError()) {
					if (new Hashtags(d.getNotes())
					   .contains(ht))
						return false;
				}
			}
		}
		return true;
	}

	/** Check if all lane markings for a hashtag are deployable */
	private boolean areLaneMarkingsDeployable(String ht) {
		Iterator<LaneMarking> it = LaneMarkingHelper.iterator();
		while (it.hasNext()) {
			LaneMarking lm = it.next();
			if (lm instanceof LaneMarkingImpl) {
				LaneMarkingImpl lmi = (LaneMarkingImpl) lm;
				if (lmi.isFailed()) {
					if (new Hashtags(lmi.getNotes())
					   .contains(ht))
						return false;
				}
			}
		}
		return true;
	}

	/** Check if all ramp meters for a hashtag are deployable */
	private boolean areRampMetersDeployable(String ht) {
		Iterator<RampMeter> it = RampMeterHelper.iterator();
		while (it.hasNext()) {
			RampMeter rm = it.next();
			if (rm instanceof RampMeterImpl) {
				RampMeterImpl rmi = (RampMeterImpl) rm;
				if (rmi.isFailed()) {
					if (new Hashtags(rmi.getNotes())
					   .contains(ht))
						return false;
				}
			}
		}
		return true;
	}

	/** Update the plan phase */
	public void updatePhase() throws TMSException {
		PlanPhase p = phase;
		if (p != null) {
			PlanPhase np = p.getNextPhase();
			if (np != null && phaseSecs() >= p.getHoldTime())
				setPhaseNotify(np);
		}
	}

	/** Get the number of seconds in the current phase */
	private int phaseSecs() {
		long elapsed = TimeSteward.currentTimeMillis() - phase_time;
		return (int) (elapsed / 1000);
	}
}
