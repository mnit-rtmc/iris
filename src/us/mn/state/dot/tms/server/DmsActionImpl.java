/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2023  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * Action for sending a message to a DMS sign group triggered by an action plan.
 *
 * @author Douglas Lau
 */
public class DmsActionImpl extends BaseObjectImpl implements DmsAction {

	/** Create a unique DmsAction record name */
	static public String createUniqueName(String template) {
		UniqueNameCreator unc = new UniqueNameCreator(template, 30,
			(n)->lookupDmsAction(n));
		return unc.createUniqueName();
	}

	/** Load all the DMS actions */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, DmsActionImpl.class);
		store.query("SELECT name, action_plan, phase, dms_hashtag," +
			"msg_pattern, msg_priority FROM iris." + SONAR_TYPE +
			";", new ResultFactory()
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
		map.put("phase", phase);
		map.put("dms_hashtag", dms_hashtag);
		map.put("msg_pattern", msg_pattern);
		map.put("msg_priority", msg_priority);
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
		     row.getString(3),  // phase
		     row.getString(4),  // dms_hashtag
		     row.getString(5),  // msg_pattern
		     row.getInt(6)      // msg_priority
		);
	}

	/** Create a DMS action */
	private DmsActionImpl(String n, String a, String p, String ht,
		String pat, int mp)
	{
		this(n, lookupActionPlan(a), lookupPlanPhase(p), ht,
		     lookupMsgPattern(pat), mp);
	}

	/** Create a DMS action */
	public DmsActionImpl(String n, ActionPlan a, PlanPhase p, String ht,
		MsgPattern pat, int mp)
	{
		this(n);
		action_plan = a;
		phase = p;
		dms_hashtag = ht;
		msg_pattern = pat;
		msg_priority = mp;
	}

	/** Action plan */
	private ActionPlan action_plan;

	/** Get the action plan */
	@Override
	public ActionPlan getActionPlan() {
		return action_plan;
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

	/** DMS hashtag */
	private String dms_hashtag;

	/** Set the DMS hashtag */
	@Override
	public void setDmsHashtag(String ht) {
		dms_hashtag = ht;
	}

	/** Set the DMS hashtag */
	public void doSetDmsHashtag(String ht) throws TMSException {
		String t = DMSHelper.normalizeHashtag(ht);
		if (!objectEquals(t, ht))
			throw new ChangeVetoException("Bad hashtag");
		if (!objectEquals(ht, dms_hashtag)) {
			store.update(this, "dms_hashtag", ht);
			setDmsHashtag(ht);
		}
	}

	/** Get the DMS hashtag */
	@Override
	public String getDmsHashtag() {
		return dms_hashtag;
	}

	/** Message to send when action happens */
	private MsgPattern msg_pattern;

	/** Set the message pattern */
	@Override
	public void setMsgPattern(MsgPattern pat) {
		msg_pattern = pat;
	}

	/** Set the message pattern */
	public void doSetMsgPattern(MsgPattern pat) throws TMSException {
		if (pat != msg_pattern) {
			store.update(this, "msg_pattern", pat);
			setMsgPattern(pat);
		}
	}

	/** Get the message pattern */
	@Override
	public MsgPattern getMsgPattern() {
		return msg_pattern;
	}

	/** Message priority */
	private int msg_priority;

	/** Set the message priority.
	 * @param mp Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.SignMsgPriority */
	@Override
	public void setMsgPriority(int mp) {
		msg_priority = mp;
	}

	/** Set the message priority */
	public void doSetMsgPriority(int mp) throws TMSException {
		if (mp != msg_priority) {
			store.update(this, "msg_priority", mp);
			setMsgPriority(mp);
		}
	}

	/** Get the message priority.
	 * @return Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.SignMsgPriority */
	@Override
	public int getMsgPriority() {
		return msg_priority;
	}
}
