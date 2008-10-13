/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2007  Minnesota Department of Transportation
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
import java.util.List;
import javax.swing.JPanel;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.trafmap.ViewLayer;

/**
 * The RoadwayTab class provides the GUI for editing roadway nodes.
 *
 * @author Douglas Lau
 */
public class RoadwayTab extends MapTab {

	/** Corridor chooser component */
	protected final CorridorChooser chooser;

	/** Selected corridor list */
	protected final CorridorList clist;

	/** Panel for the map */
	protected final JPanel mapPanel;

	/** Tab panel */
	protected final JPanel tabPanel;

	/** Create a new roadway node tab */
	public RoadwayTab(R_NodeManager m, List<LayerState> lstates,
		ViewLayer vlayer)
	{
		super("Roadway", "View / edit roadway nodes");
		clist = new CorridorList(m);
		chooser = new CorridorChooser(m, map, clist);
		map.addLayers(lstates);
		tabPanel = createSideBar();
		mapPanel = createMapPanel(vlayer);
		// FIXME: this is ugly
		R_NodeProperties.map = map;
	}

	/** Create the side bar panel */
	protected JPanel createSideBar() {
		JPanel p = new JPanel(new BorderLayout());
		p.add(chooser, BorderLayout.NORTH);
		p.add(clist, BorderLayout.CENTER);
		return p;
	}

	/** Get the tab number */
	public int getNumber() {
		return 5;
	}

	/** Dispose of the DMS tab */
	public void dispose() {
		super.dispose();
		mapPanel.removeAll();
		chooser.dispose();
	}

	/** Get the tab panel */
	public JPanel getTabPanel() {
		return tabPanel;
	}

	/** Get the main panel */
	public JPanel getMainPanel() {
		return mapPanel;
	}
}
