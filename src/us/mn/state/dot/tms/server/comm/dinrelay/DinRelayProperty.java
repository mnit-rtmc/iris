/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2022  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * DIN relay property.
 *
 * @author Douglas Lau
 */
public class DinRelayProperty extends ControllerProperty {

	/** Relative path */
	private final String path;

	/** Get the path + query for a property */
	@Override
	public String getPathQuery() {
		return path;
	}

	/** Create a new DIN relay property */
	public DinRelayProperty(String p) {
		path = p;
	}
}
