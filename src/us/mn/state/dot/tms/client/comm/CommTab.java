/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
import java.io.IOException;
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
public class CommTab extends MapTab {

	/** Controller manager */
	protected final ControllerManager manager;

	/** Summary of controllers of each status */
	protected final StyleSummary<Controller> summary;

	/** Create a new comm tab */
  	public CommTab(Session session, ControllerManager m)
		throws IOException
	{
		super("Comm", "Manage Comm Links and Controllers");
		manager = m;
		summary = manager.createStyleSummary();
		add(createNorthPanel(), BorderLayout.NORTH);
		add(summary, BorderLayout.CENTER);
	}

	/** Create the north panel */
	protected JPanel createNorthPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		return panel;
	}

	/** Get the tab number */
	public int getNumber() {
		return 7;
	}

	/** Dispose of the comm tab */
	public void dispose() {
		super.dispose();
		manager.getSelectionModel().clearSelection();
		summary.dispose();
	}
}
