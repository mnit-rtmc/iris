/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.snmp;

import us.mn.state.dot.tms.utils.Json;

/**
 * DisplayString object.
 *
 * @author Douglas Lau
 */
public class DisplayStringObject {

	/** Json display string key */
	private final String key;

	/** DisplayString MIB node */
	public final DisplayString node;

	/** Create a DisplayString object */
	public DisplayStringObject(String k, MIBNode n) {
		key = k;
		node = new DisplayString(n);
	}

	/** Get display string value */
	public String getValue() {
		String val = node.getValue();
		return (val.length() > 0) ? val : null;
	}

	/** Get JSON representation */
	public String toJson() {
		return Json.str(key, getValue());
	}
}
