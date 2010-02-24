/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.List;
import javax.swing.JPanel;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.StyleSummary;

/**
 * Tab for managing incidents.
 *
 * @author Douglas Lau
 */
public class IncidentTab extends MapTab {

	/** Incident manager */
	protected final IncidentManager manager;

	/** Incident creator */
	protected final IncidentCreator creator;

	/** Incident dispatcher */
	protected final IncidentDispatcher dispatcher;

	/** Summary of incidents of each status */
	protected final StyleSummary<Incident> summary;

	/** Create a new incident tab */
  	public IncidentTab(Session session, IncidentManager m,
		List<LayerState> lstates) throws IOException
	{
		super("Incident", "Manage Incidents");
		manager = m;
		for(LayerState ls: lstates) {
			map_model.addLayer(ls);
			String name = ls.getLayer().getName();
			if(name.equals("Camera"))
				map_model.setHomeLayer(ls);
		}
		creator = new IncidentCreator(manager.getTheme(),
			manager.getSelectionModel(),session.getR_NodeManager());
		dispatcher = new IncidentDispatcher(session, manager);
		summary = manager.createStyleSummary();
		add(createNorthPanel(), BorderLayout.NORTH);
		add(summary, BorderLayout.CENTER);
	}

	/** Create the north panel */
	protected JPanel createNorthPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(creator, BorderLayout.NORTH);
		panel.add(dispatcher, BorderLayout.CENTER);
		return panel;
	}

	/** Get the tab number */
	public int getNumber() {
		return 2;
	}

	/** Dispose of the incident tab */
	public void dispose() {
		super.dispose();
		manager.getSelectionModel().clearSelection();
		summary.dispose();
		dispatcher.dispose();
		creator.dispose();
	}

	/** Set the map */
	public void setMap(MapBean map) {
		super.setMap(map);
		creator.setMap(map);
		creator.setEnabled(canAdd() && map != null);
	}

	/** Check if the user can add an incident */
	protected boolean canAdd() {
		return dispatcher.canAdd("oname");
	}
}
