/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.parking;

import java.awt.BorderLayout;
import us.mn.state.dot.tms.ParkingArea;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.StyleSummary;

/**
 * Tab for managing parking areas.
 *
 * @author Douglas Lau
 */
public class ParkingAreaTab extends MapTab<ParkingArea> {

	/** Dispatch panel */
	private final ParkingAreaDispatcher dispatcher;

	/** Summary of parking areas of each status */
	private final StyleSummary<ParkingArea> summary;

	/** Create a new gate arm tab */
  	public ParkingAreaTab(Session session, ParkingAreaManager man) {
		super(man);
		dispatcher = new ParkingAreaDispatcher(session, man);
		summary = man.createStyleSummary(false);
		add(dispatcher, BorderLayout.NORTH);
		add(summary, BorderLayout.CENTER);
	}

	/** Initialize the gate arm tab */
	@Override
	public void initialize() {
		dispatcher.initialize();
		summary.initialize();
	}

	/** Dispose of the tab */
	@Override
	public void dispose() {
		super.dispose();
		summary.dispose();
		dispatcher.dispose();
	}

	/** Get the tab ID */
	@Override
	public String getTabId() {
		return "parking.area";
	}
}
