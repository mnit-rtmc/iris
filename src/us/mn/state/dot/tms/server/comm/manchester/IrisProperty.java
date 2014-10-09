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
 * A property to command the iris of a camera.
 *
 * @author Douglas Lau
 */
public class IrisProperty extends ManchesterProperty {

	/** Requested iris value [-1, 1] :: [close, open] */
	private final int iris;

	/** Create a new iris property */
	public IrisProperty(int i) {
		iris = i;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "iris: " + iris;
	}

	/** Get command bits */
	@Override
	protected byte commandBits() {
		return (iris < 0) ? EX_IRIS_CLOSE
		                  : EX_IRIS_OPEN;
	}

	/** Check if packet is extended function */
	@Override
	protected boolean isExtended() {
		return true;
	}
}
