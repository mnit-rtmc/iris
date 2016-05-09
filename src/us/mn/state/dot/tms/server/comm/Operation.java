/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2016  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.EventType;

/**
 * An operation is a sequence of phases to be performed on a field controller.
 *
 * @author Douglas Lau
 */
abstract public class Operation<T extends ControllerProperty> {
	abstract public void handleCommError(EventType et, String msg);
	abstract public void setSucceeded();
	abstract public void cleanup();
}
