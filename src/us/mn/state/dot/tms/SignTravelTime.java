/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.util.Map;

/**
 * SignTravelTime is a class which encapsulates all the properties of a single
 * travel time message on a dynamic message sign (DMS).
 *
 * @author Douglas Lau
 */
public class SignTravelTime extends SignMessage {

	/** IRIS is owner of travel time messages */
	static public final String IRIS_OWNER = "IRIS";

	/** Duration of travel time messages (2 minutes) */
	static public final int MESSAGE_DURATION = 2;

	/** Create a new sign travel time message */
	public SignTravelTime(MultiString m, Map<Integer, BitmapGraphic> b) {
		super(IRIS_OWNER, m, b, MESSAGE_DURATION);
	}
}
