/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
 * SamplePoller is an interface for pollers which can send messages to traffic
 * data sampling devices (detectors).
 *
 * @author Douglas Lau
 */
public interface SamplePoller {

	/** Reset controller.
	 * @param c Controller to poll. */
	void resetController(ControllerImpl c);

	/** Send sample settings to a controller.
	 * @param c Controller to poll. */
	void sendSettings(ControllerImpl c);

	/** Query sample data.
 	 * @param c Controller to poll.
 	 * @param p Sample period in seconds. */
	void querySamples(ControllerImpl c, int p);
}
