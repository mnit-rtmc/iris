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
import java.util.List;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
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

	/** Create a new LCS tab */
	public LcsTab(Session session, LCSArrayManager manager,
		List<LayerState> lstates) throws IOException
	{
		super(session, "LCS", "Operate Lane Control Signals");
		dispatcher = new LcsDispatcher(session, manager);
		summary = manager.createStyleSummary();
		for(LayerState ls: lstates) {
			map_model.addLayer(ls);
			String name = ls.getLayer().getName();
			if(name.equals(manager.getProxyType()))
				map_model.setHomeLayer(ls);
		}
		add(dispatcher, BorderLayout.NORTH);
		add(summary, BorderLayout.CENTER);
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
	}
}
