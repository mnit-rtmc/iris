/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.alert;

import java.awt.BorderLayout;
import us.mn.state.dot.tms.AlertInfo;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.StyleSummary;

/**
 * The AlertTab class provides the GUI for working with automated alert objects.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class AlertTab extends MapTab<AlertInfo> {

	/** Summary of alerts */
	private final StyleSummary<AlertInfo> summary;

	/** Alert dispatcher for dispatching and reviewing alerts */
	private final AlertDispatcher dispatcher;

	/** Create an alert tab */
	public AlertTab(Session session, AlertManager man) {
		super(man);
		summary = man.createStyleSummary(false, 1);
		dispatcher = new AlertDispatcher(session, man);
	}

	/** Get tab number for ordering */
	@Override
	public int getTabNum() {
		return 14;
	}

	/** Initialize the alert tab */
	@Override
	public void initialize() {
		summary.initialize();
		dispatcher.initialize();
		add(summary, BorderLayout.NORTH);
		add(dispatcher, BorderLayout.CENTER);
	}

	/** Dispose of the alert tab */
	@Override
	public void dispose() {
		super.dispose();
		dispatcher.dispose();
		summary.dispose();
	}

	/** Get the AlertDispatcher */
	public AlertDispatcher getAlertDispatcher() {
		return dispatcher;
	}

	/** Get the AlertDmsDispatcher */
	public AlertDmsDispatcher getDmsDispatcher() {
		return dispatcher.getDmsDispatcher();
	}
}
