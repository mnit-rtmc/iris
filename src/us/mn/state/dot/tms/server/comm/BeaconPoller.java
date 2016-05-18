/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.BeaconImpl;

/**
 * BeaconPoller is an interface for pollers which can send messages to beacon
 * devices.
 *
 * @author Douglas Lau
 */
public interface BeaconPoller {

	/** Send a device request */
	void sendRequest(BeaconImpl b, DeviceRequest r);

	/** Set the flashing state of a beacon */
	void setFlashing(BeaconImpl b, boolean f);
}
