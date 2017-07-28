/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.TMSException;

/**
 * Action for deploying a lane marking triggered by an action plan.
 *
 * @author Douglas Lau
 */
public class LaneActionImpl extends BaseObjectImpl implements LaneAction {

	/** Load all the lane actions */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, LaneActionImpl.class);
		store.query("SELECT name, action_plan, lane_marking, phase " +
			"FROM iris." + SONAR_TYPE  +";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LaneActionImpl(
					namespace,
					row.getString(1),	// name
					row.getString(2),	// action_plan
					row.getString(3),	// lane_marking
					row.getString(4)	// phase
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("action_plan", action_plan);
		map.put("lane_marking", lane_marking);
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

	/** Create a new lane action */
	public LaneActionImpl(String n) {
		super(n);
	}

	/** Create a new lane action */
	protected LaneActionImpl(Namespace ns, String n, String a, String lm,
		String p)
	{
		this(n, (ActionPlan) ns.lookupObject(ActionPlan.SONAR_TYPE, a),
		    (LaneMarking) ns.lookupObject(LaneMarking.SONAR_TYPE, lm),
		    lookupPlanPhase(p));
	}

	/** Create a new lane action */
	protected LaneActionImpl(String n, ActionPlan a, LaneMarking lm,
		PlanPhase p)
	{
		this(n);
		action_plan = a;
		lane_marking = lm;
		phase = p;
	}

	/** Action plan */
	protected ActionPlan action_plan;

	/** Get the action plan */
	@Override
	public ActionPlan getActionPlan() {
		return action_plan;
	}

	/** Lane marking */
	protected LaneMarking lane_marking;

	/** Get the lane marking */
	@Override
	public LaneMarking getLaneMarking() {
		return lane_marking;
	}

	/** Action plan phase to trigger action */
	private PlanPhase phase;

	/** Set the plan phase to perform action */
	@Override
	public void setPhase(PlanPhase p) {
		phase = p;
	}

	/** Set the plan phase to perform action */
	public void doSetPhase(PlanPhase p) throws TMSException {
		if(p == phase)
			return;
		store.update(this, "phase", p);
		setPhase(p);
	}

	/** Get the plan phase to perform action */
	@Override
	public PlanPhase getPhase() {
		return phase;
	}
}
