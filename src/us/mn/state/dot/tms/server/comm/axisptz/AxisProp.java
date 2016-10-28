/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.axisptz;

import java.io.InputStream;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * Axis Property.
 *
 * @author Douglas Lau
 */
abstract public class AxisProp extends ControllerProperty {

	/** Command string */
	private final String command;

	/** Param string */
	private final StringBuilder param = new StringBuilder();

	/** Create a new axis property */
	protected AxisProp(String cmd) {
		command = cmd;
	}

	/** Get as a string */
	@Override
	public String toString() {
		return param.toString();
	}

	/** Get the path for a property */
	@Override
	public String getPath() {
		return "/axis-cgi/com/" + command + '?' + param;
	}

	/** Add a param */
	public void addParam(String n, String v) {
		if (param.length() > 0)
			param.append('&');
		param.append(n);
		param.append('=');
		param.append(v);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os) {
		// nothing to do -- params encoded in path
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is) {
		// nothing to do -- decoded from HTTP result code
	}
}
