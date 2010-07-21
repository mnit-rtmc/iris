/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.org815;

import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.IDebugLog;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation for ORG-815 device
 *
 * @author Douglas Lau
 */
abstract public class OpOrg815 extends OpController {

	/** ORG-815 debug log */
	static protected final IDebugLog ORG815_LOG = new IDebugLog("org815");

	/** Create a new ORG-815 operation */
	protected OpOrg815(PriorityLevel p, ControllerImpl c) {
		super(p, c);
	}
}
