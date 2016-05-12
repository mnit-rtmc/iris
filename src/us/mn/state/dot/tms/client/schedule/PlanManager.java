/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.schedule;

import java.awt.Color;
import java.util.Iterator;
import javax.swing.JPopupMenu;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.BeaconAction;
import us.mn.state.dot.tms.BeaconActionHelper;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DmsActionHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneActionHelper;
import us.mn.state.dot.tms.MeterAction;
import us.mn.state.dot.tms.MeterActionHelper;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyManager;

/**
 * A plan manager is a container for SONAR action plan objects.
 *
 * @author Douglas Lau
 */
public class PlanManager extends ProxyManager<ActionPlan> {

	/** Create a new action plan manager */
	public PlanManager(Session s, GeoLocManager lm) {
		super(s, lm, 0, ItemStyle.ACTIVE);
	}

	/** Get the sonar type name */
	@Override
	public String getSonarType() {
		return ActionPlan.SONAR_TYPE;
	}

	/** Get the action plan cache */
	@Override
	public TypeCache<ActionPlan> getCache() {
		return session.getSonarState().getActionPlans();
	}

	/** Create a plan map tab */
	@Override
	public PlanTab createTab() {
		return new PlanTab(session, this);
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(ActionPlan proxy) {
		return null;
	}

	/** Create a theme for action plans */
	@Override
	protected PlanTheme createTheme() {
		return new PlanTheme(this);
	}

	/** Check the style of the specified proxy */
	@Override
	public boolean checkStyle(ItemStyle is, ActionPlan proxy) {
		if (!isUpdatePermitted(proxy))
			return false;
		switch (is) {
		case DMS:
			return proxy.getActive() && hasDmsAction(proxy);
		case BEACON:
			return proxy.getActive() && hasBeaconAction(proxy);
		case METER:
			return proxy.getActive() && hasMeterAction(proxy);
		case LANE:
			return proxy.getActive() && hasLaneAction(proxy);
		case TIME:
			return proxy.getActive() && hasTimeAction(proxy);
		case ACTIVE:
			return proxy.getActive();
		case ALL:
			return true;
		default:
			return false;
		}
	}

	/** Check if the user is permitted to update the given action plan */
	private boolean isUpdatePermitted(ActionPlan plan) {
		return session.isUpdatePermitted(plan, "phase");
	}

	/** Test if an action plan has time actions */
	private boolean hasTimeAction(ActionPlan p) {
		Iterator<TimeAction> it = TimeActionHelper.iterator();
		while (it.hasNext()) {
			TimeAction ta = it.next();
			if (ta.getActionPlan() == p)
				return true;
		}
		return false;
	}

	/** Test if an action plan has DMS actions */
	private boolean hasDmsAction(ActionPlan p) {
		Iterator<DmsAction> it = DmsActionHelper.iterator();
		while (it.hasNext()) {
			DmsAction da = it.next();
			if (da.getActionPlan() == p)
				return true;
		}
		return false;
	}

	/** Test if an action plan has beacon actions */
	private boolean hasBeaconAction(ActionPlan p) {
		Iterator<BeaconAction> it = BeaconActionHelper.iterator();
		while (it.hasNext()) {
			BeaconAction ba = it.next();
			if (ba.getActionPlan() == p)
				return true;
		}
		return false;
	}

	/** Test if an action plan has lane actions */
	private boolean hasLaneAction(ActionPlan p) {
		Iterator<LaneAction> it = LaneActionHelper.iterator();
		while (it.hasNext()) {
			LaneAction la = it.next();
			if (la.getActionPlan() == p)
				return true;
		}
		return false;
	}

	/** Test if an action plan has meter actions */
	private boolean hasMeterAction(ActionPlan p) {
		Iterator<MeterAction> it = MeterActionHelper.iterator();
		while (it.hasNext()) {
			MeterAction ma = it.next();
			if (ma.getActionPlan() == p)
				return true;
		}
		return false;
	}

	/** Get the description of an action plan */
	public String getDescription(ActionPlan plan) {
		return plan.getName() + " -- " + plan.getDescription();
	}

	/** Create a popup menu for a single selection */
	@Override
	protected JPopupMenu createPopupSingle(ActionPlan plan) {
		return null;
	}
}
