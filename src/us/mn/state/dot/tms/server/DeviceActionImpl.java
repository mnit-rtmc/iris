/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DeviceAction;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * Action for activating devices triggered by an action plan.
 *
 * @author Douglas Lau
 */
public class DeviceActionImpl extends BaseObjectImpl implements DeviceAction {

	/** Create a unique DeviceAction record name */
	static public String createUniqueName(String template) {
		UniqueNameCreator unc = new UniqueNameCreator(template, 30,
			(n)->lookupDeviceAction(n));
		return unc.createUniqueName();
	}

	/** Load all the device actions */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, action_plan, phase, hashtag," +
			"msg_pattern, msg_priority FROM iris." + SONAR_TYPE +
			";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new DeviceActionImpl(row));
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
		map.put("hashtag", hashtag);
		map.put("msg_pattern", msg_pattern);
		map.put("msg_priority", msg_priority);
		return map;
	}

	/** Create a new device action */
	public DeviceActionImpl(String n) {
		super(n);
	}

	/** Create a device action */
	private DeviceActionImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // action_plan
		     row.getString(3),  // phase
		     row.getString(4),  // hashtag
		     row.getString(5),  // msg_pattern
		     row.getInt(6)      // msg_priority
		);
	}

	/** Create a device action */
	private DeviceActionImpl(String n, String a, String p, String ht,
		String pat, int pr)
	{
		this(n, lookupActionPlan(a), lookupPlanPhase(p), ht,
		     lookupMsgPattern(pat), pr);
	}

	/** Create a device action */
	public DeviceActionImpl(String n, ActionPlanImpl a, PlanPhase p,
		String ht, MsgPattern pat, int pr)
	{
		this(n);
		action_plan = a;
		phase = p;
		hashtag = ht;
		msg_pattern = pat;
		msg_priority = pr;
	}

	/** Action plan */
	private ActionPlanImpl action_plan;

	/** Get the action plan */
	@Override
	public ActionPlan getActionPlan() {
		return action_plan;
	}

	/** Action plan phase to trigger device action */
	private PlanPhase phase;

	/** Set the plan phase to perform action */
	@Override
	public void setPhase(PlanPhase p) {
		action_plan.testGateArmDisable(name, "phase");
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

	/** Hashtag */
	private String hashtag;

	/** Set the hashtag */
	@Override
	public void setHashtag(String ht) {
		action_plan.testGateArmDisable(name, "hashtag");
		hashtag = ht;
	}

	/** Set the hashtag */
	public void doSetHashtag(String ht) throws TMSException {
		String t = Hashtags.normalize(ht);
		if (!objectEquals(t, ht))
			throw new ChangeVetoException("Bad hashtag");
		if (!objectEquals(ht, hashtag)) {
			store.update(this, "hashtag", ht);
			setHashtag(ht);
		}
	}

	/** Get the hashtag */
	@Override
	public String getHashtag() {
		return hashtag;
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

	/** Message priority (1-255) */
	private int msg_priority;

	/** Set the message priority.
	 * @param pr Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.SignMsgPriority */
	@Override
	public void setMsgPriority(int pr) {
		action_plan.testGateArmDisable(name, "msg_priority");
		msg_priority = pr;
	}

	/** Set the message priority */
	public void doSetMsgPriority(int pr) throws TMSException {
		if (pr != msg_priority) {
			store.update(this, "msg_priority", pr);
			setMsgPriority(pr);
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
