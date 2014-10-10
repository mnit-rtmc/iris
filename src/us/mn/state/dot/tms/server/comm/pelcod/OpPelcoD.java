/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelcod;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Pelco D operation.
 *
 * @author Douglas Lau
 */
abstract public class OpPelcoD extends OpDevice<PelcoDProperty> {

	/** Pelco D debug log */
	static private final DebugLog PELCOD_LOG = new DebugLog("pelcod");

	/** Log a property store */
	protected void logStore(PelcoDProperty prop) {
		if (PELCOD_LOG.isOpen())
			PELCOD_LOG.log(controller.getName() + ":= " + prop);
	}

	/** Create a new Pelco D operation */
	protected OpPelcoD(CameraImpl c) {
		super(PriorityLevel.COMMAND, c);
	}
}
