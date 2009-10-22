/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import java.io.IOException;
import java.util.List;
import javax.swing.BoxLayout;
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
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		manager = m;
		for(LayerState ls: lstates) {
			map_model.addLayer(ls);
			if(ls.getLayer().getName().equals(m.getProxyType()))
				map_model.setHomeLayer(ls);
		}
		creator = new IncidentCreator(manager.getTheme());
		dispatcher = new IncidentDispatcher(session, manager);
		summary = manager.createStyleSummary();
		add(creator);
		add(dispatcher);
		add(summary);
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
	}
}
