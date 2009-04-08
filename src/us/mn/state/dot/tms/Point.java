/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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

import java.awt.geom.Point2D;

import us.mn.state.dot.tms.kml.KmlPoint;
import us.mn.state.dot.tms.kml.KmlRenderer;

/**
 * A 2D point.
 *
 * @author Michael Darter
 * @created 11/26/08
 * @company AHMCT, University of California, Davis
 */
public class Point extends Point2D.Double 
	implements KmlPoint
{
	/** constructor */
	public Point(double x, double y) {
		super(x, y);
	}

	/** constructor */
	public Point() {
		super(0, 0);
	}

	/** get X */
	public double getX() {
		return super.getX();
	}

	/** get Y */
	public double getY() {
		return super.getY();
	}

	/** get Z */
	public double getZ() {
		return 0;
	}

	/** render to kml (KmlPoint interface) */
	public String renderKml() {
		return KmlRenderer.render(this);
	}

	/** render innert elements to kml (KmlPoint interface) */
	public String renderInnerKml() {
		return "";
	}
}
