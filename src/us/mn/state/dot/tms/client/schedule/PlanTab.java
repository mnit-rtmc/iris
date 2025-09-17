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

import java.awt.BorderLayout;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.map.Layer;
import us.mn.state.dot.tms.client.proxy.StyleSummary;

/**
 * Tab for managing action plans.
 *
 * @author Douglas Lau
 */
public class PlanTab extends MapTab<ActionPlan> {

	/** Plan dispatcher */
	private final PlanDispatcher dispatcher;

	/** Summary of plans of each status */
	private final StyleSummary<ActionPlan> summary;

	/** Create a new action plan tab */
  	public PlanTab(Session session, PlanManager m) {
		super(m);
		dispatcher = new PlanDispatcher(session, m);
		summary = m.createStyleSummary(false);
		add(dispatcher, BorderLayout.NORTH);
		add(summary, BorderLayout.CENTER);
	}

	/** Get tab number for ordering */
	@Override
	public int getTabNum() {
		return 11;
	}

	/** Initialize the plan tab */
	@Override
	public void initialize() {
		dispatcher.initialize();
		summary.initialize();
	}

	/** Dispose of the plan tab */
	@Override
	public void dispose() {
		super.dispose();
		dispatcher.dispose();
		summary.dispose();
	}
}
