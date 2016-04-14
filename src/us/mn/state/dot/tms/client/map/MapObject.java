/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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

import java.awt.Shape;
import java.awt.geom.AffineTransform;

/**
 * Interface for objects painted on a MapBean.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public interface MapObject {

	/** Get the map object transform */
	AffineTransform getTransform();

	/** Get the inverse map object transform */
	AffineTransform getInverseTransform();

	/** Get the shape to draw the map object */
	Shape getShape();

	/** Get the shape to draw the outline */
	Shape getOutlineShape();
}
