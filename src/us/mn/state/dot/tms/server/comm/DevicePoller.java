/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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

import java.io.IOException;

/**
 * DevicePoller is the base interface for polling devices (or controllers).
 *
 * @author Douglas Lau
 */
public interface DevicePoller {

	/** Get the poller status */
	String getStatus();

	/** Check if poller is ready for operation */
	boolean isReady();

	/** Check if poller is connected */
	boolean isConnected();

	/** Check if the poller was hung up */
	boolean wasHungUp();

	/** Set the receive timeout */
	void setTimeout(int t) throws IOException;

	/** Destroy the poller */
	void destroy();
}
