/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2007  Minnesota Department of Transportation
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
 * A request to select a new camera
 *
 * @author Douglas Lau
 */
public class SelectCameraRequest extends Request {

	/** Command to select a new camera */
	static protected final String CODE = "B";

	/** Camera to select */
	protected final int camera;

	/** Create a new select camera request */
	public SelectCameraRequest(int c) {
		camera = c;
	}

	/** Get the code to send to the switcher */
	public String toString() {
		return CODE + camera;
	}
}
