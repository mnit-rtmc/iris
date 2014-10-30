/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.canoga;

import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Canoga controller operation
 *
 * @author Douglas Lau
 */
abstract public class OpCanoga extends OpController<CanogaProperty> {

	/** Create a new canoga operation */
	public OpCanoga(PriorityLevel pl, ControllerImpl c) {
		super(pl, c);
	}
}
