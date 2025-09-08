/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.PlanPhaseHelper;
import us.mn.state.dot.tms.TMSException;

/**
 * A plan phase is a specific state for an action plan to which actions can be
 * associated.
 *
 * @author Douglas Lau
 */
public class PlanPhaseImpl extends BaseObjectImpl
	implements PlanPhase
{
	/** Load all the plan phases */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, selectable, hold_time, next_phase " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new PlanPhaseImpl(
					row.getString(1),  // name
					row.getBoolean(2), // selectable
					row.getObject(3),  // hold_time
					row.getString(4)   // next_phase
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("selectable", selectable);
		map.put("hold_time", hold_time);
		map.put("next_phase", next_phase);
		return map;
	}

	/** Create a new plan phase */
	public PlanPhaseImpl(String n) {
		super(n);
		hold_time = null;
		next_phase = null;
	}

	/** Create a new plan phase */
	protected PlanPhaseImpl(String n, boolean s, Object ht, String np) {
		this(n);
		selectable = s;
		hold_time = (Integer) ht;
		next_phase = np;
	}

	/** Selectable flag */
	private boolean selectable;

	/** Set selectable flag */
	@Override
	public void setSelectable(boolean s) {
		selectable = s;
	}

	/** Set selectable flag */
	public void doSetSelectable(boolean s) throws TMSException {
		if (s != selectable) {
			store.update(this, "selectable", s);
			setSelectable(s);
		}
	}

	/** Get selectable flag */
	@Override
	public boolean getSelectable() {
		return selectable;
	}

	/** Hold time */
	private Integer hold_time;

	/** Set hold time (s) before next phase */
	@Override
	public void setHoldTime(Integer ht) {
		hold_time = ht;
	}

	/** Set hold time (s) before next phase */
	public void doSetHoldTime(Integer ht) throws TMSException {
		if (!objectEquals(ht, hold_time)) {
			store.update(this, "hold_time", ht);
			setHoldTime(ht);
		}
	}

	/** Get hold time (s) before next phase */
	@Override
	public Integer getHoldTime() {
		return hold_time;
	}

	/** Next phase */
	protected String next_phase;

	/** Set next phase after hold time expires */
	@Override
	public void setNextPhase(PlanPhase np) {
		next_phase = (np != null) ? np.getName() : null;
	}

	/** Set next phase after hold time expires */
	public void doSetNextPhase(PlanPhase np) throws TMSException {
		if (!objectEquals(np, next_phase)) {
			store.update(this, "next_phase", np);
			setNextPhase(np);
		}
	}

	/** Get next phase after hold time expires */
	@Override
	public PlanPhase getNextPhase() {
		return PlanPhaseHelper.lookup(next_phase);
	}
}
