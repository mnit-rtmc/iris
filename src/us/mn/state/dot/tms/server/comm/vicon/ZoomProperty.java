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
 * A request to zoom the selected camera
 *
 * @author Douglas Lau
 */
public class ZoomProperty extends ViconProperty {

	/** Command for zooming camera in */
	static protected final String IN = "O";

	/** Command for zooming camera out */
	static protected final String OUT = "N";

	/** Speed (and direction) to zoom camera */
	protected final int zoom;

	/** Create a new zoom request */
	public ZoomProperty(int z) {
		zoom = z;
	}

	/** Get the code to send to the switcher */
	public String toString() {
		if(zoom == 0)
			return "";
		else if(zoom > 0)
			return IN;
		else
			return OUT;
	}
}
