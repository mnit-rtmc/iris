/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.JPanel;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.trafmap.BaseMapLayer;
import us.mn.state.dot.trafmap.TunnelLayer;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.proxy.StyleSummary;

/**
 * GUI form for working with LaneControlSignal objects.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class LcsTab extends MapTab {

	/** LCS dispatcher */
	protected final LcsDispatcher dispatcher;

	/** Summary of LCS arrays of each status */
	protected final StyleSummary<LCSArray> summary;

	/** Tab panel */
	protected final JPanel tabPanel;

	/** Main panel */
	protected final JPanel mainPanel;

	/** Create a new LCS tab */
	public LcsTab(LCSArrayManager manager, TmsConnection tc)
		throws IOException
	{
		super("LCS", "Operate Lane Control Signals");
		dispatcher = new LcsDispatcher(manager, tc);
		summary = manager.createStyleSummary();
		LayerState lstate =
			BaseMapLayer.createTunnelMapLayer().createState();
		LayerState tunnel = new TunnelLayer().createState();
		map.addLayer(lstate);
		map.addLayer(tunnel);
		map.addLayer(manager.getLayer().createState());
		map.setHomeExtent(lstate.getExtent());
		mainPanel = createMapPanel(null);
		tabPanel = createSideBar();
	}

	/** Create the side bar component */
	protected JPanel createSideBar() {
		JPanel p = new JPanel(new BorderLayout());
		p.add(dispatcher, BorderLayout.NORTH);
		p.add(summary, BorderLayout.CENTER);
		return p;
	}

	/** Get the tab number */
	public int getNumber() {
		return 3;
	}

	/** Dispose of the LCS tab */
	public void dispose() {
		super.dispose();
		dispatcher.dispose();
		summary.dispose();
		mainPanel.removeAll();
	}

	/** Get the tab panel */
	public JPanel getTabPanel() {
		return tabPanel;
	}

	/** Get the main panel */
	public JPanel getMainPanel() {
		return mainPanel;
	}
}
