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
package us.mn.state.dot.tms.client.dms;

import java.awt.BorderLayout;
import java.util.List;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.StyleSummary;
import us.mn.state.dot.tms.utils.I18N;

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
	protected final StyleSummary<DMS> summary;

	/** Create a new DMS tab */
 	public DMSTab(Session session, DMSManager manager, 
		List<LayerState> lstates)
	{
		super(I18N.get("dms.abbreviation"), I18N.get("dms.title"));
		dispatcher = new DMSDispatcher(session, manager);
		summary = manager.createStyleSummary();
		for(LayerState ls: lstates) {
			map_model.addLayer(ls);
			String name = ls.getLayer().getName();
			if(name.equals(I18N.get("dms.abbreviation")))
				map_model.setHomeLayer(ls);
		}
		add(dispatcher, BorderLayout.NORTH);
		add(summary, BorderLayout.CENTER);
	}

	/** Get the tab number */
	public int getNumber() {
		return 0;
	}

	/** Dispose of the DMS tab */
	public void dispose() {
		super.dispose();
		dispatcher.dispose();
		summary.dispose();
	}
}
