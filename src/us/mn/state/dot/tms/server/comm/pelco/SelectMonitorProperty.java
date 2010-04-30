/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2010  Minnesota Department of Transportation
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

/**
 * A property to select a new monitor
 *
 * @author Douglas Lau
 * @author Timothy Johnson
 */
public class SelectMonitorProperty extends PelcoProperty {

	/** Command to select a new monitor */
	static protected final String CODE = "Ma";

	/** Monitor to select */
	protected final int monitor;

	/** Create a new select monitor property */
	public SelectMonitorProperty(int m) {
		monitor = m;
	}

	/** Get the code to send to the switcher */
	public String toString() {
		return monitor + CODE;
	}
}
