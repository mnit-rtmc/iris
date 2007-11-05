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
package us.mn.state.dot.tms.client.meter;

import java.awt.Color;
import us.mn.state.dot.map.Outline;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.device.TrafficDeviceTheme;

/**
 * Theme for displaying ramp meters on the map.
 *
 * @author Douglas Lau
 */
public class RampMeterTheme extends TrafficDeviceTheme {

	/** Outline for locked ramp meters */
	static protected final Outline LOCKED_OUTLINE = Outline.createSolid(
		Color.RED, 30);

	/** Create a new ramp meter theme */
	public RampMeterTheme() {
		super(MeterProxy.PROXY_TYPE, new MeterMarker());
		addStyle(RampMeter.STATUS_AVAILABLE, "Available",
			COLOR_AVAILABLE);
		addStyle(RampMeter.STATUS_LOCKED_OFF, "Locked Off",
			COLOR_AVAILABLE, LOCKED_OUTLINE);
		addStyle(RampMeter.STATUS_METERING, "Metering", Color.GREEN);
		addStyle(RampMeter.STATUS_QUEUE, "Queue", Color.YELLOW);
		addStyle(RampMeter.STATUS_QUEUE_BACKUP, "Queue Backup",
			Color.ORANGE);
		addStyle(RampMeter.STATUS_CONGESTED, "Congested",
			Color.MAGENTA);
		addStyle(RampMeter.STATUS_WARNING, "Warning", Color.RED);
		addStyle(RampMeter.STATUS_LOCKED_ON, "Locked On",
			Color.GREEN, LOCKED_OUTLINE);
		addStyle(RampMeter.STATUS_UNAVAILABLE, "Unavailable",
			COLOR_UNAVAILABLE);
		addStyle(RampMeter.STATUS_FAILED, "Failed", COLOR_FAILED);
		addStyle(RampMeter.STATUS_INACTIVE, "Inactive",
			COLOR_INACTIVE, INACTIVE_OUTLINE);
	}
}
