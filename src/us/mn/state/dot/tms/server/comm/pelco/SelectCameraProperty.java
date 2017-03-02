/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2017  Minnesota Department of Transportation
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

import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * A property to select a new camera
 *
 * @author Douglas Lau
 * @author Timothy Johnson
 */
public class SelectCameraProperty extends PelcoProperty {

	/** Command to select a new camera */
	static private final String CODE = "#a";

	/** Camera number to select */
	private final int cam_num;

	/** Create a new select camera property */
	public SelectCameraProperty(int c) {
		cam_num = c;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		os.write(new String(cam_num + CODE).getBytes());
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "select camera " + cam_num;
	}
}
