/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2023  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.BeaconAction;
import us.mn.state.dot.tms.BeaconActionHelper;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DmsActionHelper;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneActionHelper;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.MeterAction;
import us.mn.state.dot.tms.MeterActionHelper;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.RampMeter;
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
		namespace.registerType(SONAR_TYPE, ActionPlanImpl.class,
			GROUP_CHECKER);
		store.query("SELECT name, description, group_n, sync_actions, "+
			"sticky, active, default_phase, phase FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
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
		map.put("description", description);
		map.put("group_n", group_n);
		map.put("sync_actions", sync_actions);
		map.put("sticky", sticky);
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
		description = "";
	}

	/** Create an action plan */
	private ActionPlanImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // description
		     row.getString(3),  // group_n
		     row.getBoolean(4), // sync_actions
		     row.getBoolean(5), // sticky
		     row.getBoolean(6), // active
		     row.getString(7),  // default_phase
		     row.getString(8)   // phase
		);
	}

	/** Create an action plan */
	private ActionPlanImpl(String n, String dsc, String gn, boolean sa,
		boolean st, boolean a, String dp, String p)
	{
		this(n, dsc, gn, sa, st, a, lookupPlanPhase(dp),
		     lookupPlanPhase(p));
	}

	/** Create an action plan */
	public ActionPlanImpl(String n, String dsc, String gn, boolean sa,
		boolean st, boolean a, PlanPhase dp, PlanPhase p)
	{
		this(n);
		description = dsc;
		group_n = gn;
		sync_actions = sa;
		sticky = st;
		active = a;
		default_phase = dp;
		phase = p;
	}

	/** Plan description */
	private String description;

	/** Set the description */
	@Override
	public void setDescription(String d) {
		description = d;
	}

	/** Set the description */
	public void doSetDescription(String d) throws TMSException {
		if (!objectEquals(d, description)) {
			store.update(this, "description", d);
			setDescription(d);
		}
	}

	/** Get the description */
	@Override
	public String getDescription() {
		return description;
	}

	/** Group name */
	private String group_n;

	/** Set the group name */
	@Override
	public void setGroupN(String g) {
		group_n = g;
	}

	/** Set the group name */
	public void doSetGroupN(String g) throws TMSException {
		if (!objectEquals(g, group_n)) {
			store.update(this, "group_n", g);
			setGroupN(g);
		}
	}

	/** Get the group name */
	@Override
	public String getGroupN() {
		return group_n;
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
			if (getSyncActions()) {
				validateDmsActions();    // throws exception
				validateBeaconActions(); // throws exception
				// FIXME: any way to validate camera actions?
				validateLaneActions();   // throws exception
				validateMeterActions();  // throws exception
			}
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
	 * Validate that all DMS actions are deployable.
	 * @throws ChangeVetoException If a single DmsAction for this
	 * ActionPlan is not deployable.
	 */
	private void validateDmsActions() throws ChangeVetoException {
		Iterator<DmsAction> it = DmsActionHelper.iterator();
		while (it.hasNext()) {
			DmsAction da = it.next();
			if (da.getActionPlan() == this && !isDeployable(da)) {
				throw new ChangeVetoException("DMS action " +
					da.getName() + " not deployable");
			}
		}
	}

	/** Check if a DMS action is deployable */
	private boolean isDeployable(DmsAction da) {
		String dht = da.getDmsHashtag();
		Iterator<DMS> it = DMSHelper.hashtagIterator(dht);
		while (it.hasNext()) {
			DMS dms = it.next();
			if (dms instanceof DMSImpl) {
				if (((DMSImpl) dms).hasError())
					return false;
			}
		}
		return true;
	}

	/**
	 * Validate that all beacon actions are deployable.
	 * @throws ChangeVetoException
	 */
	private void validateBeaconActions() throws ChangeVetoException {
		Iterator<BeaconAction> it = BeaconActionHelper.iterator();
		while (it.hasNext()) {
			BeaconAction ba = it.next();
			if (ba.getActionPlan() == this && !isDeployable(ba)) {
				throw new ChangeVetoException("Beacon action " +
					ba.getName() + " not deployable");
			}
		}
	}

	/** Check if a beacon action is deployable */
	private boolean isDeployable(BeaconAction ba) {
		Beacon b = ba.getBeacon();
		if (b instanceof BeaconImpl)
			return !((BeaconImpl) b).isFailed();
		else
			return false;
	}

	/**
	 * Validate that all lane actions are deployable.
	 * @throws ChangeVetoException If a single LaneAction is not
	 * deployable.
	 */
	private void validateLaneActions() throws ChangeVetoException {
		Iterator<LaneAction> it = LaneActionHelper.iterator();
		while (it.hasNext()) {
			LaneAction la = it.next();
			if (la.getActionPlan() == this && !isDeployable(la)) {
				throw new ChangeVetoException("Lane action " +
					la.getName() + " not deployable");
			}
		}
	}

	/** Check if a lane action is deployable */
	private boolean isDeployable(LaneAction la) {
		LaneMarking lm = la.getLaneMarking();
		if (lm instanceof LaneMarkingImpl)
			return !((LaneMarkingImpl) lm).isFailed();
		else
			return false;
	}

	/**
	 * Validate that all meter actions are deployable.
	 * @throws ChangeVetoException
	 */
	private void validateMeterActions() throws ChangeVetoException {
		Iterator<MeterAction> it = MeterActionHelper.iterator();
		while (it.hasNext()) {
			MeterAction ma = it.next();
			if (ma.getActionPlan() == this && !isDeployable(ma)) {
				throw new ChangeVetoException("Meter action " +
					ma.getName() + " not deployable");
			}
		}
	}

	/** Check if a meter action is deployable */
	private boolean isDeployable(MeterAction ma) {
		RampMeter rm = ma.getRampMeter();
		if (rm instanceof RampMeterImpl)
			return !((RampMeterImpl) rm).isFailed();
		else
			return false;
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
