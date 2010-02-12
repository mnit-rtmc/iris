/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
 * Copyright (C) 2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import us.mn.state.dot.map.marker.AbstractMarker;

/**
 * An iris marker uses system attributes to determine the default size and
 * maximum scale to draw on the map.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
abstract public class IrisMarker extends AbstractMarker {

	/** Create a new iris marker.
	 * @param c Count of nodes on marker path. */
	public IrisMarker(int c) {
		super(c);
	}

	/** Get the default marker size in pixels */
	abstract protected float getSizePixels();

	/** Create a transformed marker with the specified transform */
	public Shape createTransformedMarker(AffineTransform at) {
		return path.createTransformedShape(at);
	}
}
