/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.StyleSummary;
import us.mn.state.dot.tms.utils.I18N;

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
	public LcsTab(Session session, LCSArrayManager manager)
		throws IOException
	{
		super(manager.getProxyType(), I18N.get("lcs.tab"));
		dispatcher = new LcsDispatcher(session, manager);
		summary = manager.createStyleSummary();
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
