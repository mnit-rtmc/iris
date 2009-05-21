/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.pelco;

/**
 * Pelco Request
 *
 * @author Douglas Lau
 * @author Timothy Johnson
 */
abstract public class Request {

	/** Value to indicate no selected camera */
	static protected final int CAMERA_NONE = -1;

	/** Minimum value allowed for a request */
	static protected final int MIN_VALUE = -256;

	/** Maximum value allowed for a request */
	static protected final int MAX_VALUE = 256;

	/** Clamp a value to within the allowed range */
	static protected int clampValue(int v) {
		return Math.max(Math.min(v, MAX_VALUE), MIN_VALUE);
	}

	/** Get the request string */
	abstract public String toString();
}
