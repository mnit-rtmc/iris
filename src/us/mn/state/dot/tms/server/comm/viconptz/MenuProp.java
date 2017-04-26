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
package us.mn.state.dot.tms.server.comm.viconptz;

import us.mn.state.dot.tms.DeviceRequest;

/**
 * Vicon property for menu enter / cancel function.
 *
 * @author Douglas Lau
 */
public class MenuProp extends ViconPTZProp {

	/** Toggle auto iris (or menu cancel) */
	static private final byte IRIS_AUTO = 1 << 1;

	/** Toggle auto pan (or menu enter) */
	static private final byte PAN_AUTO = 1 << 2;

	/** Menu device request */
	private final DeviceRequest req;

	/** Create a new menu property */
	public MenuProp(DeviceRequest dr) {
		assert dr == DeviceRequest.CAMERA_MENU_ENTER ||
		       dr == DeviceRequest.CAMERA_MENU_CANCEL;
		req = dr;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "menu: " + req;
	}

	/** Get pan/tilt flags */
	@Override
	protected byte panTiltFlags() {
		return (req == DeviceRequest.CAMERA_MENU_ENTER)
		      ? PAN_AUTO
		      :	IRIS_AUTO;
	}
}
