/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.StyleSummary;

/**
 * The DMSTab class provides the GUI for working with DMS objects.
 *
 * @author Douglas Lau
 */
public class DMSTab extends MapTab<DMS> {

	/** DMS dispatcher component */
	private final DMSDispatcher dispatcher;

	/** Summary of DMSs of each status */
	private final StyleSummary<DMS> summary;

	/** Create a new DMS tab */
 	public DMSTab(Session session, DMSManager man) {
		super(man);
		dispatcher = new DMSDispatcher(session, man);
		summary = man.createStyleSummary(true);
		add(dispatcher, BorderLayout.NORTH);
		add(summary, BorderLayout.CENTER);
	}

	/** Initialize the map tab */
	@Override
	public void initialize() {
		dispatcher.initialize();
		summary.initialize();
	}

	/** Dispose of the DMS tab */
	@Override
	public void dispose() {
		super.dispose();
		dispatcher.dispose();
		summary.dispose();
	}
}
