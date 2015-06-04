/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2015  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.comm.ControllerException;

/**
 * SNMP ReadOnly exception.
 *
 * @author Douglas Lau
 */
public class ReadOnly extends ControllerException {

	/** Create a new ReadOnly exception */
	public ReadOnly(String n) {
		super("SNMP: READ ONLY: " + n);
	}
}
