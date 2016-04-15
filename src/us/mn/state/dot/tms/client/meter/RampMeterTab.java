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
package us.mn.state.dot.tms.client.meter;

import java.awt.BorderLayout;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.StyleSummary;

/**
 * Gui for opererating ramp meters.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class RampMeterTab extends MapTab<RampMeter> {

	/** Ramp meter dispatcher */
	private final MeterDispatcher dispatcher;

	/** Summary of meters of each status */
	private final StyleSummary<RampMeter> summary;

	/** Create a new ramp meter tab */
  	public RampMeterTab(Session session, MeterManager man) {
		super(man);
		dispatcher = new MeterDispatcher(session, man);
		summary = man.createStyleSummary(false);
		add(dispatcher, BorderLayout.NORTH);
		add(summary, BorderLayout.CENTER);
	}

	/** Initialize the ramp meter tab */
	@Override
	public void initialize() {
		dispatcher.initialize();
		summary.initialize();
	}

	/** Dispose of the ramp meter tab */
	@Override
	public void dispose() {
		super.dispose();
		manager.getSelectionModel().clearSelection();
		summary.dispose();
		dispatcher.dispose();
	}

	/** Get the tab ID */
	@Override
	public String getTabId() {
		return "ramp.meter";
	}
}
