/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2016  Minnesota Department of Transportation
 * Copyright (C) 2017       SRF Consulting Group
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

/**
 * Device poller interface.
 *
 * @author Douglas Lau
 * @author John L. Stanley
 */
public interface DevicePoller {

	/** Set the remote URI */
	void setUri(String u);

	/** Set the receive timeout (ms) */
	void setTimeout(int rt);

	/** Get the poller status */
	String getStatus();

	/** Check if the poller is currently connected */
	boolean isConnected();

	/** Get max seconds an idle (non-modem)
	 *  connection should be left open
	 *  (-1 == infinite) */
	int getPollerIdleDisconnectSec();
	
	/** Disconnect if idle */
	void disconnectIfIdle();

	/** Destroy the poller */
	void destroy();
}
