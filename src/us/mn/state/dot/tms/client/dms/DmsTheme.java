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
package us.mn.state.dot.tms.client.dms;

import java.awt.Color;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.client.device.TrafficDeviceTheme;

/**
 * Theme for displaying DMS on the map.
 *
 * @author Douglas Lau
 */
public class DmsTheme extends TrafficDeviceTheme {

	/** Create a new DMS theme */
	public DmsTheme() {
		super(DMSProxy.PROXY_TYPE, new DmsMarker());
		addStyle(DMS.STATUS_AVAILABLE, "Available", COLOR_AVAILABLE);
		addStyle(DMS.STATUS_DEPLOYED, "Deployed", COLOR_DEPLOYED);
		addStyle(DMS.STATUS_TRAVEL_TIME, "Travel Time", Color.ORANGE);
		addStyle(DMS.STATUS_UNAVAILABLE, "Unavailable",
			COLOR_UNAVAILABLE);
		addStyle(DMS.STATUS_FAILED, "Failed", COLOR_FAILED);
		addStyle(DMS.STATUS_INACTIVE, "Inactive", COLOR_INACTIVE,
			INACTIVE_OUTLINE);
	}
}
