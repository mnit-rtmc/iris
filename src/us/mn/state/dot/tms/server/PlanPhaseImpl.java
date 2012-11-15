/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2012  Minnesota Department of Transportation
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
		namespace.registerType(SONAR_TYPE, PlanPhaseImpl.class);
		store.query("SELECT name, hold_time, next_phase FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new PlanPhaseImpl(
					row.getString(1),	// name
					row.getInt(2),		// hold_time
					row.getString(3)	// next_phase
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("hold_time", hold_time);
		map.put("next_phase", next_phase);
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

	/** Create a new plan phase */
	public PlanPhaseImpl(String n) {
		super(n);
		next_phase = null;
	}

	/** Create a new plan phase */
	protected PlanPhaseImpl(String n, int ht, String np) {
		this(n);
		hold_time = ht;
		next_phase = np;
	}

	/** Hold time */
	private int hold_time = 0;

	/** Set the hold time (seconds) */
	public void setHoldTime(int ht) {
		hold_time = ht;
	}

	/** Set the hold time (seconds) */
	public void doSetHoldTime(int ht) throws TMSException {
		if(ht == hold_time)
			return;
		if(ht < 0)
			throw new ChangeVetoException("Invalid time: " + ht);
		store.update(this, "hold_time", ht);
		setHoldTime(ht);
	}

	/** Get the hold time (seconds) */
	public int getHoldTime() {
		return hold_time;
	}

	/** Next phase */
	protected String next_phase;

	/** Set the next phase */
	public void setNextPhase(PlanPhase np) {
		next_phase = np != null ? np.getName() : null;
	}

	/** Set the next phase */
	public void doSetNextPhase(PlanPhase np) throws TMSException {
		if(np == null && next_phase == null)
			return;
		if(np != null && np.getName().equals(next_phase))
			return;
		store.update(this, "next_phase", np);
		setNextPhase(np);
	}

	/** Get the next phase */
	public PlanPhase getNextPhase() {
		return PlanPhaseHelper.lookup(next_phase);
	}
}
