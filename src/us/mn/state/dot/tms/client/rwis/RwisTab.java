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
package us.mn.state.dot.tms.client.rwis;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.tdxml.XmlIncidentClient;
import us.mn.state.dot.tms.client.IrisTab;
import us.mn.state.dot.tms.client.incidents.IncidentListModel;
import us.mn.state.dot.trafmap.RwisLayer;
import us.mn.state.dot.tms.client.incidents.WrappingCellRenderer;

/**
 * Provides a GUI for the incident tab on the operator interface for IRIS.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class RwisTab extends IrisTab {

	/** Client for XML incident data */
	protected final XmlIncidentClient incidentClient;

	/** List model to hold current incidents */
	protected final IncidentListModel model = new IncidentListModel();

	/** Tab panel */
	protected final JPanel tabPanel;

	/** Create a new incident tab for the IRIS client */
	public RwisTab(RwisLayer layer) {
		super("RWIS", "RWIS summary");
		incidentClient = layer.getIncidentClient();
		tabPanel = createSideBar();
	}

	/** Create the side bar component */
	protected JPanel createSideBar() {
/*
		incidentClient.addTdxmlListener(model);
		JList incidents = new JList(model);
		incidents.getSelectionModel().setSelectionMode(
			ListSelectionModel.SINGLE_SELECTION);
		incidents.setCellRenderer(new WrappingCellRenderer());
		JScrollPane spIncidents = new JScrollPane(incidents);
		spIncidents.setHorizontalScrollBarPolicy(
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		spIncidents.setBorder(BorderFactory.createTitledBorder(
			"Weather Stations"));
*/
		JPanel sidePanel = new JPanel(new BorderLayout());
//		sidePanel.add(spIncidents, BorderLayout.CENTER);
		return sidePanel;
	}

	/** Get the tab number */
	public int getNumber() {
		return 3;
	}

	/** Dispose of the incident tab */
	public void dispose() {
		incidentClient.removeTdxmlListener(model);
	}

	/** Get the tab panel */
	public JPanel getTabPanel() {
		return tabPanel;
	}

	/** Get the main panel */
	public JPanel getMainPanel() {
		return null;
	}
}
