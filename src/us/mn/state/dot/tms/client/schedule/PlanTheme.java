/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2019  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.schedule;

import java.awt.Color;
import javax.swing.Icon;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.beacon.BeaconMarker;
import us.mn.state.dot.tms.client.camera.CameraMarker;
import us.mn.state.dot.tms.client.dms.DmsMarker;
import us.mn.state.dot.tms.client.map.Style;
import us.mn.state.dot.tms.client.map.VectorSymbol;
import us.mn.state.dot.tms.client.meter.MeterMarker;
import us.mn.state.dot.tms.client.meter.MeterTheme;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * Theme for action plans.
 *
 * @author Douglas Lau
 */
public class PlanTheme extends ProxyTheme<ActionPlan> {

	/** Legend color */
	static private final Color COLOR = new Color(255, 255, 0, 128);

	/** Color to display schedule icon */
	static private final Color COLOR_SCHEDULE = new Color(208, 0, 208);

	/** Color for active camera style */
	static private final Color COLOR_CAMERA = new Color(0, 192, 255);

	/** Color for undeployed plans */
	static private final Color COLOR_UNDEPLOYED = new Color(255, 0, 0);

	/** Symbol for DMS */
	static private final VectorSymbol SYM_DMS = new VectorSymbol(
		new DmsMarker());

	/** Symbol for beacons */
	static private final VectorSymbol SYM_BEACON = new VectorSymbol(
		new BeaconMarker());

	/** Symbol for cameras */
	static private final VectorSymbol SYM_CAMERA = new VectorSymbol(
		new CameraMarker());

	/** Symbol for ramp meters */
	static private final VectorSymbol SYM_METER = new VectorSymbol(
		new MeterMarker());

	/** Symbol for time actions */
	static private final VectorSymbol SYM_TIME = new VectorSymbol(
		new TimeMarker());

	/** Create a new plan theme */
	public PlanTheme(PlanManager man) {
		super(man, new PlanMarker());
		// These are all the filtering buttons
		addStyle(ItemStyle.DMS, COLOR_SCHEDULED);
		addStyle(ItemStyle.BEACON, COLOR_DEPLOYED);
		addStyle(ItemStyle.CAMERA, COLOR_CAMERA);
		addStyle(ItemStyle.METER, MeterTheme.COLOR_METERING);
		addStyle(ItemStyle.TIME, COLOR_SCHEDULE);
		addStyle(ItemStyle.ACTIVE, COLOR);
		addStyle(ItemStyle.UNDEPLOYED, COLOR_UNDEPLOYED);
		addStyle(ItemStyle.ALL);
	}

	/** Get a legend icon for a style */
	@Override
	public Icon getLegend(Style sty) {
		ItemStyle is = ItemStyle.lookupStyle(sty.toString());
		switch (is) {
		case DMS:
			return SYM_DMS.getLegend(sty);
		case BEACON:
			return SYM_BEACON.getLegend(sty);
		case CAMERA:
			return SYM_CAMERA.getLegend(sty);
		case METER:
			return SYM_METER.getLegend(sty);
		case TIME:
			return SYM_TIME.getLegend(sty);
		default:
			return super.getLegend(sty);
		}
	}
}
