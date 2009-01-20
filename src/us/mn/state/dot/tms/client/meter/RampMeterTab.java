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
package us.mn.state.dot.tms.client.meter;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.List;
import javax.swing.JPanel;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.trafmap.ViewLayer;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.device.StatusSummary;
import us.mn.state.dot.tms.client.proxy.TmsMapLayer;

/**
 * Gui for opererating ramp meters.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class RampMeterTab extends MapTab {

	/** Meter device manager */
	protected final MeterManager manager;

	/** Meter status panel */
	protected final MeterStatusPanel statusPanel;

	/** Summary of meter status lists */
	protected final StatusSummary meterList;

	/** Tab panel */
	protected final JPanel tabPanel;

	/** Main panel */
	protected final JPanel mainPanel;

	/** Create a new ramp meter tab */
  	public RampMeterTab(List<LayerState> lstates, ViewLayer vlayer,
		MeterManager m, TmsConnection connection) throws IOException
	{
		super("Meter", "Operate Ramp Meters");
		manager = m;
		map.addLayers(lstates);
		map.addLayer(rampLayer.createState());
		mainPanel = createMapPanel(vlayer);
		statusPanel = new MeterStatusPanel(connection, manager);
		meterList = new StatusSummary(manager);
		tabPanel = createSideBar();
 	}

	/** Create the side bar */
	protected JPanel createSideBar() {
		JPanel p = new JPanel(new BorderLayout());
		p.add(statusPanel, BorderLayout.NORTH);
		p.add(meterList, BorderLayout.CENTER);
		return p;
	}

	/** Get the tab number */
	public int getNumber() {
		return 1;
	}

	/** Dispose of the ramp meter tab */
	public void dispose() {
		super.dispose();
		manager.getSelectionModel().setSelected(null);
		meterList.removeAll();
		statusPanel.dispose();
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
