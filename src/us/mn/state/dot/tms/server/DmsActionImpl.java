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

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ActionPlanState;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DmsAction;
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
		System.err.println("Loading DMS actions...");
		namespace.registerType(SONAR_TYPE, DmsActionImpl.class);
		store.query("SELECT name, action_plan, sign_group, " +
			"state, quick_message, a_priority, r_priority " +
			"FROM iris." + SONAR_TYPE  +";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new DmsActionImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// action_plan
					row.getString(3),	// sign_group
					row.getInt(4),		// state
					row.getString(5),	// quick_message
					row.getInt(6),		// a_priority
					row.getInt(7)		// r_priority
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("action_plan", action_plan);
		map.put("sign_group", sign_group);
		map.put("state", state);
		map.put("quick_message", quick_message);
		map.put("a_priority", a_priority);
		map.put("r_priority", r_priority);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new DMS action */
	public DmsActionImpl(String n) {
		super(n);
	}

	/** Create a new DMS action */
	protected DmsActionImpl(Namespace ns, String n, String a, String sg,
		int st, String qm, int ap, int rp)
	{
		this(n, (ActionPlan)ns.lookupObject(ActionPlan.SONAR_TYPE, a),
		    (SignGroup)ns.lookupObject(SignGroup.SONAR_TYPE, sg), st,
		    (QuickMessage)ns.lookupObject(QuickMessage.SONAR_TYPE, qm),
		    ap, rp);
	}

	/** Create a new DMS action */
	protected DmsActionImpl(String n, ActionPlan a, SignGroup sg, int st,
		QuickMessage qm, int ap, int rp)
	{
		this(n);
		action_plan = a;
		sign_group = sg;
		state = st;
		quick_message = qm;
		a_priority = ap;
		r_priority = rp;
	}

	/** Action plan */
	protected ActionPlan action_plan;

	/** Get the action plan */
	public ActionPlan getActionPlan() {
		return action_plan;
	}

	/** Sign group */
	protected SignGroup sign_group;

	/** Get the sign group */
	public SignGroup getSignGroup() {
		return sign_group;
	}

	/** Action plan state to trigger DMS action */
	protected int state;

	/** Set the plan state to perform action */
	public void setState(int s) {
		state = s;
	}

	/** Set the plan state to perform action */
	public void doSetState(int s) throws TMSException {
		if(s == state)
			return;
		if(ActionPlanState.fromOrdinal(s) == null)
			throw new ChangeVetoException("Invalid plan state");
		store.update(this, "state", s);
		setState(s);
	}

	/** Get the plan state to perform action */
	public int getState() {
		return state;
	}

	/** Quick message to send when action happens */
	protected QuickMessage quick_message;

	/** Set the quick message */
	public void setQuickMessage(QuickMessage qm) {
		quick_message = qm;
	}

	/** Set the quick message */
	public void doSetQuickMessage(QuickMessage qm) throws TMSException {
		if(qm == quick_message)
			return;
		store.update(this, "quick_message", qm);
		setQuickMessage(qm);
	}

	/** Get the quick message */
	public QuickMessage getQuickMessage() {
		return quick_message;
	}

	/** Message activation priority */
	protected int a_priority;

	/** Set the activation priority.
	 * @param p Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DMSMessagePriority */
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
	 * @see us.mn.state.dot.tms.DMSMessagePriority */
	public int getActivationPriority() {
		return a_priority;
	}

	/** Message run-time priority */
	protected int r_priority;

	/** Set the run-time priority.
	 * @param p Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DMSMessagePriority */
	public void setRunTimePriority(int p) {
		r_priority = p;
	}

	/** Set the run-time priority */
	public void doSetRunTimePriority(int p) throws TMSException {
		if(p == r_priority)
			return;
		store.update(this, "r_priority", p);
		setRunTimePriority(p);
	}

	/** Get the run-time priority.
	 * @return Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DMSMessagePriority */
	public int getRunTimePriority() {
		return r_priority;
	}
}
