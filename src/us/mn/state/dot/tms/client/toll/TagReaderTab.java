/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toll;

import java.awt.BorderLayout;
import us.mn.state.dot.tms.TagReader;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.StyleSummary;

/**
 * Provides a GUI for the tag reader tab on the operator interface for IRIS.
 *
 * @author Douglas Lau
 */
public class TagReaderTab extends MapTab<TagReader> {

	/** Summary of tag readers */
	private final StyleSummary<TagReader> summary;

	/** Create a new tag reader tab for the IRIS client */
	public TagReaderTab(Session session, TagReaderManager man) {
		super(man);
		summary = man.createStyleSummary(false);
		add(summary, BorderLayout.CENTER);
	}

	/** Initialize the tag reader tab */
	@Override
	public void initialize() {
		summary.initialize();
	}

	/** Perform any clean up necessary */
	@Override
	public void dispose() {
		super.dispose();
		summary.dispose();
	}

	/** Get the tab ID */
	@Override
	public String getTabId() {
		return "tag.reader";
	}
}
