/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.StyleSummary;

/**
 * Tab for working with LCS objects.
 *
 * @author Douglas Lau
 */
public class LcsTab extends MapTab<Lcs> {

	/** LCS dispatcher */
	private final LcsDispatcher dispatcher;

	/** Summary of LCS arrays of each status */
	private final StyleSummary<Lcs> summary;

	/** Create a new LCS tab */
	public LcsTab(Session session, LcsManager man) {
		super(man);
		dispatcher = new LcsDispatcher(session, man);
		summary = man.createStyleSummary(false);
		add(dispatcher, BorderLayout.NORTH);
		add(summary, BorderLayout.CENTER);
	}

	/** Initialize the LCS tab */
	@Override
	public void initialize() {
		dispatcher.initialize();
		summary.initialize();
	}

	/** Dispose of the LCS tab */
	@Override
	public void dispose() {
		super.dispose();
		dispatcher.dispose();
		summary.dispose();
	}

	/** Get the tab ID */
	@Override
	public String getTabId() {
		return "lcs";
	}
}
