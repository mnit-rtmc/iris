/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import java.awt.Color;

/**
 * Comm state enumeration.
 *
 * @author Douglas Lau
 */
public enum CommState {
	INACTIVE (new Color(0, 0, 0, 32)),
	OK       (new Color(96, 96, 255)),
	FAILED   (Color.GRAY);

	/** Icon for the comm state */
	public final ControllerIcon icon;

	/** Create a comm state */
	private CommState(Color c) {
		icon = new ControllerIcon(c);
	}
}
