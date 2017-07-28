/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.BeaconAction;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.TMSException;

/**
 * Action for deploying a beacon triggered by an action plan.
 *
 * @author Douglas Lau
 */
public class BeaconActionImpl extends BaseObjectImpl implements BeaconAction {

	/** Load all the beacon actions */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, BeaconActionImpl.class);
		store.query("SELECT name, action_plan, beacon, phase " +
			"FROM iris." + SONAR_TYPE  +";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new BeaconActionImpl(
					namespace,
					row.getString(1),	// name
					row.getString(2),	// action_plan
					row.getString(3),	// beacon
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
		map.put("beacon", beacon);
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

	/** Create a new beacon action */
	public BeaconActionImpl(String n) {
		super(n);
	}

	/** Create a new beacon action */
	private BeaconActionImpl(Namespace ns, String n, String a, String b,
		String p)
	{
		this(n, (ActionPlan) ns.lookupObject(ActionPlan.SONAR_TYPE, a),
		    (Beacon) ns.lookupObject(Beacon.SONAR_TYPE, b),
		    lookupPlanPhase(p));
	}

	/** Create a new beacon action */
	private BeaconActionImpl(String n, ActionPlan a, Beacon b,
		PlanPhase p)
	{
		this(n);
		action_plan = a;
		beacon = b;
		phase = p;
	}

	/** Action plan */
	private ActionPlan action_plan;

	/** Get the action plan */
	@Override
	public ActionPlan getActionPlan() {
		return action_plan;
	}

	/** Beacon */
	private Beacon beacon;

	/** Get the beacon */
	@Override
	public Beacon getBeacon() {
		return beacon;
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
