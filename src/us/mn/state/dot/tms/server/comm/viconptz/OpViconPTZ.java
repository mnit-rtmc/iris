/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.viconptz;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Vicon operation.
 *
 * @author Douglas Lau
 */
abstract public class OpViconPTZ extends OpDevice<ViconPTZProperty> {

	/** Vicon PTZ debug log */
	static private final DebugLog VICON_LOG = new DebugLog("viconptz");

	/** Log a property store */
	protected void logStore(ViconPTZProperty prop) {
		if (VICON_LOG.isOpen())
			VICON_LOG.log(controller.getName() + ":= " + prop);
	}

	/** Create a new vicon PTZ operation */
	public OpViconPTZ(CameraImpl c) {
		super(PriorityLevel.COMMAND, c);
	}
}
