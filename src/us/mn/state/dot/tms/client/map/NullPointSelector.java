/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.map;

import java.awt.geom.Point2D;

/**
 * A null point selector is defined for when no point selection is occurring.
 *
 * @author Douglas Lau
 */
public class NullPointSelector implements PointSelector {

	/** Select a point with the mouse pointer */
	public boolean selectPoint(Point2D p) {
		return false;
	}

	/** Finish point selection */
	public void finish() { }
}
