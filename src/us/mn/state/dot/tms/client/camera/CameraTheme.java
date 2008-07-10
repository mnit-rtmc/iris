/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import java.awt.Color;

import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.TrafficDevice;
import us.mn.state.dot.tms.client.device.TrafficDeviceProxy;
import us.mn.state.dot.tms.client.device.TrafficDeviceTheme;

/**
 * Theme for cameras
 * 
 * @author Douglas Lau
 */
public class CameraTheme extends TrafficDeviceTheme {

	/** Create a new camera theme */
	public CameraTheme() {
		super(CameraProxy.PROXY_TYPE, new CameraMarker());
		addStyle(Camera.STATUS_AVAILABLE, "Active",
			new Color(0, 192, 255));
		addStyle(Camera.STATUS_INACTIVE, "Inactive", COLOR_INACTIVE,
			INACTIVE_OUTLINE);
		addStyle(Camera.STATUS_NOT_PUBLISHED, "Not Published",
			Color.BLACK);
	}
}
