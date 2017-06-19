/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.monstream;

import us.mn.state.dot.tms.server.comm.ControllerProp;

/**
 * A monitor property.
 *
 * @author Douglas Lau
 */
public class MonProp extends ControllerProp {

	/** ASCII record separator */
	static protected final char RECORD_SEP = 30;

	/** ASCII unit separator */
	static protected final char UNIT_SEP = 31;
}
