/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.gate;

import javax.swing.JPopupMenu;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ActionPlanHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * A gate plan manager is a container for SONAR gate arm action plans.
 *
 * @author Douglas Lau
 */
public class GatePlanManager extends ProxyManager<ActionPlan> {

	/** Create a descriptor for action plans */
	static private ProxyDescriptor<ActionPlan> descriptor(Session s) {
		return new ProxyDescriptor<ActionPlan>(
			s.getSonarState().getActionPlans(), false
		);
	}

	/** Create a new gate action plan manager */
	public GatePlanManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 0, ItemStyle.GATE_ARM);
	}

	/** Create a gate arm map tab */
	@Override
	public GateArmTab createTab() {
		return new GateArmTab(session, this);
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(ActionPlan proxy) {
		return null;
	}

	/** Create a theme for gate arm action plans */
	@Override
	protected ProxyTheme<ActionPlan> createTheme() {
		ProxyTheme<ActionPlan> theme = new ProxyTheme<ActionPlan>(this,
			new GateArmMarker());
		theme.addStyle(ItemStyle.CLOSED, ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(ItemStyle.CHANGE, ProxyTheme.COLOR_CHANGE);
		theme.addStyle(ItemStyle.OPEN, ProxyTheme.COLOR_DEPLOYED);
		return theme;
	}

	/** Check the style of the specified proxy */
	@Override
	public boolean checkStyle(ItemStyle is, ActionPlan proxy) {
		if (!isWritePermitted(proxy))
			return false;
		if (!proxy.getActive())
			return false;
		if (ActionPlanHelper.countGateArms(proxy) == 0)
			return false;
		String pp = proxy.getPhase().getName();
		switch (is) {
		case CLOSED:
			return PlanPhase.GATE_ARM_CLOSED.equals(pp);
		case CHANGE:
			return PlanPhase.GATE_ARM_CHANGE.equals(pp);
		case OPEN:
			return PlanPhase.GATE_ARM_OPEN.equals(pp);
		default:
			return false;
		}
	}

	/** Check if the user is permitted to update the given action plan */
	private boolean isWritePermitted(ActionPlan plan) {
		return session.isWritePermitted(plan, "phase");
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
