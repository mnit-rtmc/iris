/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * SamplePoller is an interface for MessagePoller classes which can poll traffic
 * data sampling devices (detectors).
 *
 * @author Douglas Lau
 */
public interface SamplePoller {

	/** Reset controller */
	void resetController(ControllerImpl c);

	/** Query sample data */
	void querySamples(ControllerImpl c, int interval, Completer comp);
}
