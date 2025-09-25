/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2025  Minnesota Department of Transportation
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

import java.awt.BorderLayout;
import java.util.Collection;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.DeviceAction;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.StyleSummary;
import us.mn.state.dot.tms.client.proxy.SwingProxyAdapter;

/**
 * Tab for managing gate arms.
 *
 * @author Douglas Lau
 */
public class GateArmTab extends MapTab<ActionPlan> {

	/** Gate Arm dispatch panel */
	private final GateArmDispatcher dispatcher;

	/** Summary of action plans of each status */
	private final StyleSummary<ActionPlan> summary;

	/** Device action proxy listener */
	private final SwingProxyAdapter<DeviceAction> da_listener =
		new SwingProxyAdapter<DeviceAction>()
	{
		@Override
		protected void proxyAddedSwing(DeviceAction da) {
			summary.updateCounts();
		}
		@Override
		protected void enumerationCompleteSwing(
			Collection<DeviceAction> proxies)
		{
			summary.updateCounts();
		}
		@Override
		protected void proxyRemovedSwing(DeviceAction da) {
			summary.updateCounts();
		}
	};

	/** Create a new gate arm tab */
	public GateArmTab(Session s, GatePlanManager man) {
		super(man);
		dispatcher = new GateArmDispatcher(s);
		summary = man.createStyleSummary(false, 1);
		add(dispatcher, BorderLayout.NORTH);
		add(summary, BorderLayout.CENTER);
		TypeCache<DeviceAction> c =
			s.getSonarState().getDeviceActions();
		c.addProxyListener(da_listener);
	}

	/** Get tab number for ordering */
	@Override
	public int getTabNum() {
		return 6;
	}

	/** Get the tab ID */
	@Override
	public String getTabId() {
		return "gate_arm";
	}

	/** Initialize the gate arm tab */
	@Override
	public void initialize() {
		dispatcher.initialize();
		summary.initialize();
	}

	/** Dispose of the tab */
	@Override
	public void dispose() {
		super.dispose();
		summary.dispose();
		dispatcher.dispose();
	}
}
