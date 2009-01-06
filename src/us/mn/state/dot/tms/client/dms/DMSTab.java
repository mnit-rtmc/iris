/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JPanel;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.trafmap.ViewLayer;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * The DMSTab class provides the GUI for working with DMS objects.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class DMSTab extends MapTab {

	/** DMS dispatcher component */
	protected final DMSDispatcher dispatcher;

	/** Summary of DMSs of each status */
	protected final DmsStatusSummary chooser;

	/** Tab panel */
	protected final JPanel tabPanel;

	/** Main panel */
	protected final JPanel mainPanel;

	/** Create a new DMS tab */
	public DMSTab(List<LayerState> lstates, ViewLayer vlayer,
		DMSHandler handler, SonarState st, TmsConnection tmsConnection)
	{
		super(I18NMessages.get("dms.abbreviation"),
			I18NMessages.get("dms.title"));
		dispatcher = new DMSDispatcher(handler, st, tmsConnection);
		chooser = new DmsStatusSummary(handler);
		map.addLayers(lstates);
		tabPanel = createSideBar();
		mainPanel = createMapPanel(vlayer);
	}

	/** Create the side bar panel */
	protected JPanel createSideBar() {
		JPanel p = new JPanel(new BorderLayout());
		p.add(dispatcher, BorderLayout.NORTH);
		p.add(chooser, BorderLayout.CENTER);
		return p;
	}

	/** Get the tab number */
	public int getNumber() {
		return 0;
	}

	/** Dispose of the DMS tab */
	public void dispose() {
		super.dispose();
		mainPanel.removeAll();
		dispatcher.dispose();
		chooser.dispose();
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
