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
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.TMSException;

/**
 * Action for deploying a lane marking triggered by an action plan.
 *
 * @author Douglas Lau
 */
public class LaneActionImpl extends BaseObjectImpl implements LaneAction {

	/** Load all the lane actions */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading lane actions...");
		namespace.registerType(SONAR_TYPE, LaneActionImpl.class);
		store.query("SELECT name, action_plan, lane_marking, state " +
			"FROM iris." + SONAR_TYPE  +";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LaneActionImpl(
					namespace,
					row.getString(1),	// name
					row.getString(2),	// action_plan
					row.getString(3),	// lane_marking
					row.getInt(4)		// state
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("action_plan", action_plan);
		map.put("lane_marking", lane_marking);
		map.put("state", state);
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

	/** Create a new lane action */
	public LaneActionImpl(String n) {
		super(n);
	}

	/** Create a new lane action */
	protected LaneActionImpl(Namespace ns, String n, String a, String lm,
		int st)
	{
		this(n, (ActionPlan)ns.lookupObject(ActionPlan.SONAR_TYPE, a),
		    (LaneMarking)ns.lookupObject(LaneMarking.SONAR_TYPE, lm),
		    st);
	}

	/** Create a new lane action */
	protected LaneActionImpl(String n, ActionPlan a, LaneMarking lm,
		int st)
	{
		this(n);
		action_plan = a;
		lane_marking = lm;
		state = st;
	}

	/** Action plan */
	protected ActionPlan action_plan;

	/** Get the action plan */
	public ActionPlan getActionPlan() {
		return action_plan;
	}

	/** Lane marking */
	protected LaneMarking lane_marking;

	/** Get the lane marking */
	public LaneMarking getLaneMarking() {
		return lane_marking;
	}

	/** Action plan state to trigger action */
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
}
