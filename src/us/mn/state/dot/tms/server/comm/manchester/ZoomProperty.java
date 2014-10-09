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
 * A property to zoom a camera.
 *
 * @author Douglas Lau
 */
public class ZoomProperty extends ManchesterProperty {

	/** Zoom value (-1 to 1) */
	private final int zoom;

	/** Create a new zoom property */
	public ZoomProperty(int z) {
		zoom = z;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "zoom: " + zoom;
	}

	/** Get command bits */
	@Override
	protected byte commandBits() {
		return (zoom < 0) ? EX_ZOOM_OUT
		                  : EX_ZOOM_IN;
	}

	/** Check if packet is extended function */
	@Override
	protected boolean isExtended() {
		return true;
	}
}
