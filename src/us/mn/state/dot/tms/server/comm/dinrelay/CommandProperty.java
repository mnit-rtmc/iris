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
package us.mn.state.dot.tms.server.comm.dinrelay;

import java.io.InputStream;

/**
 * DIN relay outlet command property.
 *
 * @author Douglas Lau
 */
public class CommandProperty extends DinRelayProperty {

	/** Create a request string */
	static private String requestString(int outlet, boolean on) {
		return "outlet?" + outlet + "=" + (on ? "ON" : "OFF");
	}

	/** Create a new outlet command property */
	public CommandProperty(int outlet, boolean on) {
		super(requestString(outlet, on));
	}

	/** Decode a STORE response */
	public void decodeStore(InputStream is, int drop) {
		// ignore response
	}
}
