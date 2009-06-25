/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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

/**
 * Vicon Request
 *
 * @author Douglas Lau
 */
abstract public class Request {

	/** Mask for command requests (second byte) */
	static protected final byte CMD = 0x10;

	/** Mask for extended command requests (second byte) */
	static protected final byte EXTENDED_CMD = 0x50;

	/** Format a request for the specified receiver address */
	abstract public byte[] format(int drop);
}
