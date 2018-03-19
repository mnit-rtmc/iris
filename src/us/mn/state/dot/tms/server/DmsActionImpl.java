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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.TMSException;

/**
 * Action for sending a message to a DMS sign group triggered by an action plan.
 *
 * @author Douglas Lau
 */
public class DmsActionImpl extends BaseObjectImpl implements DmsAction {

	/** Load all the DMS actions */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, DmsActionImpl.class);
		store.query("SELECT name, action_plan, sign_group, " +
			"phase, quick_message, beacon_enabled, prefix_page, " +
			"a_priority, r_priority FROM iris." + SONAR_TYPE  +";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new DmsActionImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("action_plan", action_plan);
		map.put("sign_group", sign_group);
		map.put("phase", phase);
		map.put("quick_message", quick_message);
		map.put("beacon_enabled", beacon_enabled);
		map.put("prefix_page", prefix_page);
		map.put("a_priority", a_priority);
		map.put("r_priority", r_priority);
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

	/** Create a new DMS action */
	public DmsActionImpl(String n) {
		super(n);
	}

	/** Create a DMS action */
	private DmsActionImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // action_plan
		     row.getString(3),  // sign_group
		     row.getString(4),  // phase
		     row.getString(5),  // quick_message
		     row.getBoolean(6),	// beacon_enabled
		     row.getBoolean(7), // prefix_page
		     row.getInt(8),     // a_priority
		     row.getInt(9)      // r_priority
		);
	}

	/** Create a DMS action */
	private DmsActionImpl(String n, String a, String sg, String p,
		String qm, boolean be, boolean pp, int ap, int rp)
	{
		this(n, lookupActionPlan(a), lookupSignGroup(sg),
		    lookupPlanPhase(p), lookupQuickMessage(qm), be, pp, ap, rp);
	}

	/** Create a DMS action */
	private DmsActionImpl(String n, ActionPlan a, SignGroup sg, PlanPhase p,
		QuickMessage qm, boolean be, boolean pp, int ap, int rp)
	{
		this(n);
		action_plan = a;
		sign_group = sg;
		phase = p;
		quick_message = qm;
		beacon_enabled = be;
		prefix_page = pp;
		a_priority = ap;
		r_priority = rp;
	}

	/** Action plan */
	private ActionPlan action_plan;

	/** Get the action plan */
	@Override
	public ActionPlan getActionPlan() {
		return action_plan;
	}

	/** Sign group */
	private SignGroup sign_group;

	/** Get the sign group */
	@Override
	public SignGroup getSignGroup() {
		return sign_group;
	}

	/** Action plan phase to trigger DMS action */
	private PlanPhase phase;

	/** Set the plan phase to perform action */
	@Override
	public void setPhase(PlanPhase p) {
		phase = p;
	}

	/** Set the plan phase to perform action */
	public void doSetPhase(PlanPhase p) throws TMSException {
		if (p != phase) {
			store.update(this, "phase", p);
			setPhase(p);
		}
	}

	/** Get the plan phase to perform action */
	@Override
	public PlanPhase getPhase() {
		return phase;
	}

	/** Quick message to send when action happens */
	private QuickMessage quick_message;

	/** Set the quick message */
	@Override
	public void setQuickMessage(QuickMessage qm) {
		quick_message = qm;
	}

	/** Set the quick message */
	public void doSetQuickMessage(QuickMessage qm) throws TMSException {
		if (qm != quick_message) {
			store.update(this, "quick_message", qm);
			setQuickMessage(qm);
		}
	}

	/** Get the quick message */
	@Override
	public QuickMessage getQuickMessage() {
		return quick_message;
	}

	/** Beacon enabled flag */
	private boolean beacon_enabled;

	/** Set beacon enabled flag */
	@Override
	public void setBeaconEnabled(boolean be) {
		beacon_enabled = be;
	}

	/** Set beacon enabled flag */
	public void doSetBeaconEnabled(boolean be) throws TMSException {
		if (be != beacon_enabled) {
			store.update(this, "beacon_enabled", be);
			setBeaconEnabled(be);
		}
	}

	/** Get beacon enabled flag */
	@Override
	public boolean getBeaconEnabled() {
		return beacon_enabled;
	}

	/** Prefix page flag */
	private boolean prefix_page;

	/** Set prefix page flag */
	@Override
	public void setPrefixPage(boolean pp) {
		prefix_page = pp;
	}

	/** Set prefix page flag */
	public void doSetPrefixPage(boolean pp) throws TMSException {
		if (pp != prefix_page) {
			store.update(this, "prefix_page", pp);
			setPrefixPage(pp);
		}
	}

	/** Get prefix page flag */
	@Override
	public boolean getPrefixPage() {
		return prefix_page;
	}

	/** Message activation priority */
	private int a_priority;

	/** Set the activation priority.
	 * @param p Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DmsMsgPriority */
	@Override
	public void setActivationPriority(int p) {
		a_priority = p;
	}

	/** Set the activation priority */
	public void doSetActivationPriority(int p) throws TMSException {
		if(p == a_priority)
			return;
		store.update(this, "a_priority", p);
		setActivationPriority(p);
	}

	/** Get the activation priority.
	 * @return Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DmsMsgPriority */
	@Override
	public int getActivationPriority() {
		return a_priority;
	}

	/** Message run-time priority */
	private int r_priority;

	/** Set the run-time priority.
	 * @param p Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DmsMsgPriority */
	@Override
	public void setRunTimePriority(int p) {
		r_priority = p;
	}

	/** Set the run-time priority */
	public void doSetRunTimePriority(int p) throws TMSException {
		if (p != r_priority) {
			store.update(this, "r_priority", p);
			setRunTimePriority(p);
		}
	}

	/** Get the run-time priority.
	 * @return Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DmsMsgPriority */
	@Override
	public int getRunTimePriority() {
		return r_priority;
	}
}
