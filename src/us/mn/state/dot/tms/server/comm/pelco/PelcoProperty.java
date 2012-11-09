/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelco;

import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * Pelco Property
 *
 * @author Douglas Lau
 * @author Timothy Johnson
 */
abstract public class PelcoProperty extends ControllerProperty {

	/** Value to indicate no selected camera */
	static protected final int CAMERA_NONE = -1;

	/** Acknowledge response */
	static protected final String ACK = "AK";

	/** Negative Acknowledge response */
	static protected final String NO_ACK = "NA";
}
