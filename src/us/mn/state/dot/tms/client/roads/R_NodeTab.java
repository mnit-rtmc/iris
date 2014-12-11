/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.awt.BorderLayout;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;

/**
 * The R_NodeTab class provides the GUI for editing roadway nodes.
 *
 * @author Douglas Lau
 */
public class R_NodeTab extends MapTab<R_Node> {

	/** R_Node panel */
	private final R_NodePanel panel;

	/** Corridor list */
	private final CorridorList clist;

	/** Create a new roadway node tab */
	public R_NodeTab(Session session, R_NodeManager man) {
		super(man);
		panel = new R_NodePanel(session);
		clist = new CorridorList(session, man, panel);
		add(panel, BorderLayout.NORTH);
		add(clist, BorderLayout.CENTER);
	}

	/** Initialize the roadway node tab */
	@Override
	public void initialize() {
		panel.initialize();
		clist.initialize();
	}

	/** Dispose of the roadway tab */
	@Override
	public void dispose() {
		super.dispose();
		clist.dispose();
		panel.dispose();
	}

	/** Get the tab ID */
	@Override
	public String getTabId() {
		return "r_node";
	}
}
