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
		store.query("SELECT name, action_plan, lane_marking, on_deploy"+
			" FROM iris." + SONAR_TYPE  +";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LaneActionImpl(
					namespace,
					row.getString(1),	// name
					row.getString(2),	// action_plan
					row.getString(3),	// lane_marking
					row.getBoolean(4)	// on_deploy
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
		map.put("on_deploy", on_deploy);
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
		boolean od)
	{
		this(n, (ActionPlan)ns.lookupObject(ActionPlan.SONAR_TYPE, a),
		    (LaneMarking)ns.lookupObject(LaneMarking.SONAR_TYPE, lm),
		    od);
	}

	/** Create a new lane action */
	protected LaneActionImpl(String n, ActionPlan a, LaneMarking lm,
		boolean od)
	{
		this(n);
		action_plan = a;
		lane_marking = lm;
		on_deploy = od;
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

	/** Flag to trigger when action plan deployed / undeployed */
	protected boolean on_deploy;

	/** Set the "on deploy" trigger flag */
	public void setOnDeploy(boolean od) {
		on_deploy = od;
	}

	/** Set the "on deploy" trigger flag */
	public void doSetOnDeploy(boolean od) throws TMSException {
		if(od == on_deploy)
			return;
		store.update(this, "on_deploy", od);
		setOnDeploy(od);
	}

	/** Get the "on deploy" trigger flag */
	public boolean getOnDeploy() {
		return on_deploy;
	}
}
