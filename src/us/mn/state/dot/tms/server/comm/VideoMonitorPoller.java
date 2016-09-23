/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.VideoMonitorImpl;

/**
 * VideoMonitorPoller is an interface for pollers which can send video monitor
 * switching messages.
 *
 * @author Douglas Lau
 */
public interface VideoMonitorPoller {

	/** Set the camera to display on the specified monitor */
	void setMonitorCamera(ControllerImpl c, VideoMonitorImpl vm,
		String cam);

	/** Send a device request
	 * @param vm The VideoMonitor object.
	 * @param r The desired DeviceRequest. */
	void sendRequest(VideoMonitorImpl vm, DeviceRequest r);
}
