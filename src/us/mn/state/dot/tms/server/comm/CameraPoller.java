/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
 * Copyright (C) 2014  AHMCT, University of California
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
import us.mn.state.dot.tms.server.CameraImpl;

/**
 * CameraPoller is an interface for pollers which can send camera control
 * messages.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public interface CameraPoller {

	/** Send a PTZ camera move command */
	void sendPTZ(CameraImpl c, float p, float t, float z);

	/** Send a store camera preset command */
	void sendStorePreset(CameraImpl c, int preset);

	/** Send a recall camera preset command */
	void sendRecallPreset(CameraImpl c, int preset);

	/** Send a device request
	 * @param c The CameraImpl object.
	 * @param r The desired DeviceRequest. */
	void sendRequest(CameraImpl c, DeviceRequest r);
}
