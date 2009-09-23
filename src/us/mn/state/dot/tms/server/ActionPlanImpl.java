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
import java.util.LinkedList;
import java.util.Map;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DmsActionHelper;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneActionHelper;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.TMSException;

/**
 * An action plan is a set of actions which can be deployed together.
 *
 * @author Douglas Lau
 */
public class ActionPlanImpl extends BaseObjectImpl implements ActionPlan {

	/** Load all the action plans */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading action plans...");
		namespace.registerType(SONAR_TYPE, ActionPlanImpl.class);
		store.query("SELECT name, description, sync_actions, active, " +
			"deployed FROM iris." + SONAR_TYPE  + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new ActionPlanImpl(
					row.getString(1),	// name
					row.getString(2),	// description
					row.getBoolean(3),	// sync_actions
					row.getBoolean(4),	// active
					row.getBoolean(5)	// deployed
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("description", description);
		map.put("sync_actions", sync_actions);
		map.put("active", active);
		map.put("deployed", deployed);
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

	/** Create a new action plan */
	public ActionPlanImpl(String n) {
		super(n);
		description = "";
	}

	/** Create a new action plan */
	protected ActionPlanImpl(String n, String dsc, boolean s, boolean a,
		boolean d)
	{
		this(n);
		description = dsc;
		sync_actions = s;
		active = a;
		deployed = d;
	}

	/** Plan description */
	protected String description;

	/** Set the description */
	public void setDescription(String d) {
		description = d;
	}

	/** Set the description */
	public void doSetDescription(String d) throws TMSException {
		if(d.equals(description))
			return;
		store.update(this, "description", d);
		setDescription(d);
	}

	/** Get the description */
	public String getDescription() {
		return description;
	}

	/** Sync actions flag */
	protected boolean sync_actions;

	/** Set the sync actions flag */
	public void setSyncActions(boolean s) {
		sync_actions = s;
	}

	/** Set the sync actions flag */
	public void doSetSyncActions(boolean s) throws TMSException {
		if(s == sync_actions)
			return;
		store.update(this, "sync_actions", s);
		setSyncActions(s);
	}

	/** Get the sync actions flag */
	public boolean getSyncActions() {
		return sync_actions;
	}

	/** Active status */
	protected boolean active;

	/** Set the active status */
	public void setActive(boolean a) {
		active = a;
	}

	/** Set the active status */
	public void doSetActive(boolean a) throws TMSException {
		if(a == active)
			return;
		store.update(this, "active", a);
		setActive(a);
	}

	/** Get the active status */
	public boolean getActive() {
		return active;
	}

	/** Deployed status */
	protected boolean deployed;

	/** Set the deployed status */
	public void setDeployed(boolean d) {
		deployed = d;
	}

	/** Set the deployed status */
	public void doSetDeployed(boolean d) throws TMSException {
		if(d == deployed)
			return;
		if(sync_actions) {
			validateDmsActions();
			validateLaneActions();
		}
		store.update(this, "deployed", d);
		setDeployed(d);
	}

	/** Validate that all DMS actions are deployable */
	protected void validateDmsActions() throws ChangeVetoException {
		final ActionPlan ap = this;
		DmsAction da = DmsActionHelper.find(new Checker<DmsAction>() {
			public boolean check(DmsAction da) {
				return da.getActionPlan() == ap &&
				       !isDeployable(da);
			}
		});
		if(da != null) {
			throw new ChangeVetoException("DMS action " +
				da.getName() + " not deployable");
		}
	}

	/** Check if a DMS action is deployable */
	protected boolean isDeployable(final DmsAction da) {
		final LinkedList<DMSImpl> dmss = new LinkedList<DMSImpl>();
		DmsSignGroupHelper.find(da.getSignGroup(), new Checker<DMS>() {
			public boolean check(DMS dms) {
				if(dms instanceof DMSImpl)
					dmss.add((DMSImpl)dms);
				return true;
			}
		});
		for(DMSImpl dms: dmss) {
			if(!dms.isDeployable(da))
				return false;
		}
		return true;
	}

	/** Validate that all lane actions are deployable */
	protected void validateLaneActions() throws ChangeVetoException {
		final ActionPlan ap = this;
		LaneAction la = LaneActionHelper.find(new Checker<LaneAction>(){
			public boolean check(LaneAction la) {
				return la.getActionPlan() == ap &&
				       !isDeployable(la);
			}
		});
		if(la != null) {
			throw new ChangeVetoException("Lane action " +
				la.getName() + " not deployable");
		}
	}

	/** Check if a lane action is deployable */
	protected boolean isDeployable(LaneAction la) {
		LaneMarking lm = la.getLaneMarking();
		if(lm instanceof LaneMarkingImpl)
			return !((LaneMarkingImpl)lm).isFailed();
		else
			return false;
	}

	/** Set the deployed state (and notify clients) */
	public void setDeployedNotify(boolean d) {
		if(d != deployed) {
			try {
				doSetDeployed(d);
				notifyAttribute("deployed");
			}
			catch(TMSException e) {
				e.printStackTrace();
			}
		}
	}

	/** Get the deployed status */
	public boolean getDeployed() {
		return deployed;
	}
}
