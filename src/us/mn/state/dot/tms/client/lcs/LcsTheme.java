/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

import java.awt.Color;
import us.mn.state.dot.map.Outline;
import us.mn.state.dot.tms.LaneControlSignal;
import us.mn.state.dot.tms.client.device.TrafficDeviceTheme;

/**
 * Theme for displaying LaneControlSignals.
 *
 * @author Douglas lau
 */
public class LcsTheme extends TrafficDeviceTheme {

	/** Outline for stroking LCS devices */
	static protected final Outline OUTLINE = Outline.createSolid(
		Color.BLACK, 5);

	/** Create a new LCS theme */
	public LcsTheme() {
		super(LcsProxy.PROXY_TYPE, new LcsMarker());
		addStyle(LaneControlSignal.STATUS_OFF, "Off", COLOR_AVAILABLE,
			OUTLINE);
		addStyle(LaneControlSignal.STATUS_ON, "On", COLOR_DEPLOYED,
			OUTLINE);
		addStyle(LaneControlSignal.STATUS_FAILED, "Failed",
			COLOR_FAILED, OUTLINE);
		addStyle(LaneControlSignal.STATUS_INACTIVE, "Inactive",
			COLOR_INACTIVE, INACTIVE_OUTLINE);
		// FIXME: temporary hack
		addStyle(LaneControlSignal.STATUS_UNAVAILABLE, "Inactive",
			COLOR_INACTIVE, INACTIVE_OUTLINE);
	}
}
