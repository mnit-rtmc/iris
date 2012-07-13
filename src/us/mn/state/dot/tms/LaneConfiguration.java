/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

/**
 * A lane configuration represents a cross-section of a corridor, with the left
 * and right lane shift from the corridor shift origin.
 *
 * @author Douglas Lau
 */
public class LaneConfiguration {

	/** Left lane shift from corridor shift origin */
	public final int leftShift;

	/** Right lane shift from corridor shift origin */
	public final int rightShift;

	/** Create a new lane configuration */
	public LaneConfiguration(int left, int right) {
		leftShift = left;
		rightShift = right;
	}

	/** Get the number of lanes */
	public int getLanes() {
		return rightShift - leftShift;
	}
}
