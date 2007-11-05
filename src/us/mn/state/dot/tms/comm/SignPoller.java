/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm;

import us.mn.state.dot.tms.Completer;
import us.mn.state.dot.tms.ControllerImpl;

/**
 * SignPoller is an interface for MessagePoller classes which can poll various
 * types of sign devices.
 *
 * @author Douglas Lau
 */
public interface SignPoller {

	/** Perform a sign status poll */
	void pollSigns(ControllerImpl c, Completer comp);
}
