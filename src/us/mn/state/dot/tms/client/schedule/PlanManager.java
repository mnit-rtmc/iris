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
package us.mn.state.dot.tms.client.schedule;

import java.awt.Color;
import java.util.Iterator;
import javax.swing.JPopupMenu;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ActionPlanHelper;
import us.mn.state.dot.tms.DeviceAction;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyManager;

/**
 * A plan manager is a container for SONAR action plan objects.
 *
 * @author Douglas Lau
 */
public class PlanManager extends ProxyManager<ActionPlan> {

	/** Create a descriptor for action plans */
	static private ProxyDescriptor<ActionPlan> descriptor(Session s) {
		return new ProxyDescriptor<ActionPlan>(
			s.getSonarState().getActionPlans(), false
		);
	}

	/** Create a new action plan manager */
	public PlanManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 0, ItemStyle.ACTIVE);
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
		if (!isWritePermitted(proxy))
			return false;
		switch (is) {
		case BEACON:
			return proxy.getActive() &&
			       ActionPlanHelper.countBeacons(proxy) > 0;
		case CAMERA:
			return proxy.getActive() &&
			       ActionPlanHelper.countCameras(proxy) > 0;
		case DMS:
			return proxy.getActive() &&
			       ActionPlanHelper.countDms(proxy) > 0;
		case METER:
			return proxy.getActive() &&
			       ActionPlanHelper.countRampMeters(proxy) > 0;
		case TIME:
			return proxy.getActive() && hasTimeAction(proxy);
		case ACTIVE:
			return proxy.getActive();
		case UNDEPLOYED:
			return proxy.getActive() && isUndeployed(proxy);
		case ALL:
			return true;
		default:
			return false;
		}
	}

	/** Check if the user is permitted to update the given action plan */
	private boolean isWritePermitted(ActionPlan plan) {
		return session.isWritePermitted(plan, "phase");
	}

	/** Test if an aciton plan is deployed */
	private boolean isUndeployed(ActionPlan p) {
		return PlanPhase.UNDEPLOYED.equals(p.getPhase().getName());
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

	/** Get the description of an action plan */
	@Override
	public String getDescription(ActionPlan plan) {
		String n = plan.getNotes();
		return (n != null)
		      ? plan.getName() + " -- " + n
		      : plan.getName();
	}

	/** Create a popup menu for a single selection */
	@Override
	protected JPopupMenu createPopupSingle(ActionPlan plan) {
		return null;
	}
}
