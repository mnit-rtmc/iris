/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.vicon;

/**
 * A request to tilt the selected camera
 *
 * @author Douglas Lau
 */
public class TiltRequest extends Request {

	/** Command for tilting camera up */
	static protected final String UP = "M";

	/** Command for tilting camera down */
	static protected final String DOWN = "L";

	/** Speed (and direction) to tilt camera */
	protected final int tilt;

	/** Create a new tilt request */
	public TiltRequest(int t) {
		tilt = clampValue(t);
	}

	/** Get the code to send to the switcher */
	public String toString() {
		if(tilt == 0)
			return "";
		else if(tilt > 0)
			return UP + tilt;
		else
			return DOWN + Math.abs(tilt);
	}
}
