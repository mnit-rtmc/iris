/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.sierragx;

import us.mn.state.dot.tms.server.comm.AsciiDeviceProperty;

/**
 * Sierra Wireless GX-series property.
 *
 * @author John L. Stanley
 * @author Douglas Lau
 */
abstract public class SierraGxProperty extends AsciiDeviceProperty {

	/** Create a new GPS property for Sierra Wireless GX modem */
	protected SierraGxProperty(String cmd) {
		super(cmd);
		max_chars = 200;
	}
}
