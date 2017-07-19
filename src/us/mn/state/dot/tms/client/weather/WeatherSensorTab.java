/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  AHMCT, University of California
 * Copyright (C) 2017  Iteris Inc.
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
package us.mn.state.dot.tms.client.weather;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SidePanel;
import us.mn.state.dot.tms.client.proxy.StyleSummary;

/**
 * Tab for managing weather sensors.
 *
 * @author Michael Darter
 */
public class WeatherSensorTab extends MapTab<WeatherSensor> {

	/** Summary of weather sensors of each status */
	private final StyleSummary<WeatherSensor> summary;

	/** Create a new tab */
	public WeatherSensorTab(Session session, WeatherSensorManager m) {
		super(m);
		summary = m.createStyleSummary(false);
		add(createNorthPanel(), BorderLayout.NORTH);
		add(summary, BorderLayout.CENTER);
	}

	/** Create the north panel */
	private JPanel createNorthPanel() {
		return new JPanel(new BorderLayout());
	}

	/** Initialize the tab */
	@Override
	public void initialize() {
		summary.initialize();
	}

	/** Dispose of the tab */
	@Override
	public void dispose() {
		super.dispose();
		summary.dispose();
	}

	/** Get the tab ID */
	@Override
	public String getTabId() {
		return "weather_sensor";
	}
}
