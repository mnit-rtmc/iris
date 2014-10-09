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
package us.mn.state.dot.tms.server.comm.manchester;

/**
 * A property to focus a camera.
 *
 * @author Douglas Lau
 */
public class FocusProperty extends ManchesterProperty {

	/** Requested focus value [-1, 1] :: [near, far] */
	private final int focus;

	/** Create a new focus property */
	public FocusProperty(int f) {
		focus = f;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "focus: " + focus;
	}

	/** Get command bits */
	@Override
	protected byte commandBits() {
		return (focus < 0) ? EX_FOCUS_NEAR
		                   : EX_FOCUS_FAR;
	}

	/** Check if packet is extended function */
	@Override
	protected boolean isExtended() {
		return true;
	}
}
