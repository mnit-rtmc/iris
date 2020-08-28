/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2020  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * Device poller interface.
 *
 * @author Douglas Lau
 * @author John L. Stanley
 */
public interface DevicePoller {

	/** Get the poller status */
	String getStatus();

	/** Check if the poller is currently connected */
	boolean isConnected();

	/** Get max seconds an idle connection should be left open
	 * (0 indicates indefinite). */
	int getIdleDisconnectSec();

	/** Start communication test */
	void startTesting(ControllerImpl c);

	/** Destroy the poller */
	void destroy();
}
