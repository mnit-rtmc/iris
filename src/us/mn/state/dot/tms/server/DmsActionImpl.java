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
			"on_deploy, quick_message, priority FROM iris." +
			SONAR_TYPE  +";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new DmsActionImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// action_plan
					row.getString(3),	// sign_group
					row.getBoolean(4),	// on_deploy
					row.getString(5),	// quick_message
					row.getInt(6)		// priority
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
		map.put("on_deploy", on_deploy);
		map.put("quick_message", quick_message);
		map.put("priority", priority);
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
		boolean od, String qm, int p)
	{
		this(n, (ActionPlan)ns.lookupObject(ActionPlan.SONAR_TYPE, a),
		    (SignGroup)ns.lookupObject(SignGroup.SONAR_TYPE, sg), od,
		    (QuickMessage)ns.lookupObject(QuickMessage.SONAR_TYPE, qm),
		    p);
	}

	/** Create a new DMS action */
	protected DmsActionImpl(String n, ActionPlan a, SignGroup sg,
		boolean od, QuickMessage qm, int p)
	{
		this(n);
		action_plan = a;
		sign_group = sg;
		on_deploy = od;
		quick_message = qm;
		priority = p;
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

	/** Message priority */
	protected int priority;

	/** Set the message priority.
	 * @param p Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DMSMessagePriority */
	public void setPriority(int p) {
		priority = p;
	}

	/** Set the message priority */
	public void doSetPriority(int p) throws TMSException {
		if(p == priority)
			return;
		store.update(this, "priority", p);
		setPriority(p);
	}

	/** Get the message priority.
	 * @return Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DMSMessagePriority */
	public int getPriority() {
		return priority;
	}
}
