/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022-2026  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cbw;

/** 
 * Supported CBW device models.
 *
 * @author Douglas Lau
 */
public enum Model {
	/** X-301, X-WR-10R12, XRDI-WRQ-LS */
	X_301,

	/** X-310 */
	X_310,

	/** X-332 */
	X_332,

	/** X-401 */
	X_401,
	
	/** X-410 */
	X_410,

	/** X-WR-1R12 */
	X_WR_1R12;

	/** Values array */
	static private final Model[] VALUES = values();

	/** Get the Model from the value provided */
	static public Model fromValue(String v) {
		for (Model e: VALUES) {
			if (e.name().equalsIgnoreCase(v))
				return e;
		}
		return X_301;
	}

	/** Get URI path for state */
	public String statePath() {
		return (this == X_WR_1R12) ? "stateFull.xml" : "state.xml";
	}

	/** Create query component for a command request.
	 * @param relay (1-16, or null).
	 * @param on Turn relay on (true) or off (false). */
	public String commandQuery(int relay, boolean on) {
		StringBuilder sb = new StringBuilder("?");
		if (this == X_WR_1R12) {
			sb.append("relayState");
		} else if (this == X_401) {
			// NOTE: older firmware versions used "digitalIO"
			sb.append("relay");
			sb.append(relay);
		} else if (this == X_410) {
			sb.append("relay");
			sb.append(relay);
		} else {
			sb.append("relay");
			sb.append(relay);
			sb.append("State");
		}
		sb.append(on ? "=1" : "=0");
		return sb.toString();
	}
};
