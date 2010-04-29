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
package us.mn.state.dot.tms.server.comm.vicon;

/**
 * A request to pan the selected camera
 *
 * @author Douglas Lau
 */
public class PanRequest extends ViconRequest {

	/** Command to pan camera left */
	static protected final String LEFT = "I";

	/** Command to pan camera right */
	static protected final String RIGHT = "J";

	/** Speed (and direction) to pan camera */
	protected final int pan;

	/** Create a new pan request */
	public PanRequest(int p) {
		pan = clampValue(p);
	}

	/** Get the code to send to the switcher */
	public String toString() {
		if(pan == 0)
			return "";
		else if(pan > 0)
			return RIGHT + pan;
		else
			return LEFT + Math.abs(pan);
	}
}
