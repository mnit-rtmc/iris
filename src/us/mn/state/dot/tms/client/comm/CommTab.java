/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.StyleSummary;

/**
 * Tab for managing comm_links and controllers.
 *
 * @author Douglas Lau
 */
public class CommTab extends MapTab<Controller> {

	/** Summary of controllers of each status */
	private final StyleSummary<Controller> summary;

	/** Create a new comm tab */
  	public CommTab(Session session, ControllerManager m) {
		super(m);
		summary = m.createStyleSummary(false);
		add(createNorthPanel(), BorderLayout.NORTH);
		add(summary, BorderLayout.CENTER);
	}

	/** Create the north panel */
	private JPanel createNorthPanel() {
		JPanel p = new JPanel(new BorderLayout());
		return p;
	}

	/** Initialize the comm tab */
	@Override
	public void initialize() {
		summary.initialize();
	}

	/** Dispose of the comm tab */
	@Override
	public void dispose() {
		super.dispose();
		summary.dispose();
	}

	/** Get the tab ID */
	@Override
	public String getTabId() {
		return "comm";
	}
}
