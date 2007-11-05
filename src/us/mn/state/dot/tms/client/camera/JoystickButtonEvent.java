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
package us.mn.state.dot.tms.client.camera;

import java.util.EventObject;

/**
 * An event that indicates that a joystick button has been pressed or released.
 *
 * @author Douglas Lau
 */
public class JoystickButtonEvent extends EventObject {

	/** Button in question */
	public final int button;

	/** New state of button */
	public final boolean pressed;

	/** Create a new joystick button event */
	public JoystickButtonEvent(Object source, int b, boolean p) {
		super(source);
		button = b;
		pressed = p;
	}
}
