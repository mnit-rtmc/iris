/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.CameraAction;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.TMSException;

/**
 * Action for recalling a camera preset triggered by an action plan.
 *
 * @author Douglas Lau
 */
public class CameraActionImpl extends BaseObjectImpl implements CameraAction {

	/** Load all the camera actions */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, CameraActionImpl.class);
		store.query("SELECT name, action_plan, preset, phase " +
			"FROM iris." + SONAR_TYPE  +";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new CameraActionImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("action_plan", action_plan);
		map.put("preset", preset);
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

	/** Create a new camera action */
	public CameraActionImpl(String n) {
		super(n);
	}

	/** Create a new camera action */
	private CameraActionImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // action_plan
		     row.getString(3),  // preset
		     row.getString(4)   // phase
		);
	}

	/** Create a new camera action */
	private CameraActionImpl(String n, String ap, String cp, String p) {
		this(n);
		action_plan = lookupActionPlan(ap);
		preset = lookupPreset(cp);
		phase = lookupPlanPhase(p);
	}

	/** Action plan */
	private ActionPlan action_plan;

	/** Get the action plan */
	@Override
	public ActionPlan getActionPlan() {
		return action_plan;
	}

	/** Camera preset */
	private CameraPreset preset;

	/** Get the camera preset */
	@Override
	public CameraPreset getPreset() {
		return preset;
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
}
