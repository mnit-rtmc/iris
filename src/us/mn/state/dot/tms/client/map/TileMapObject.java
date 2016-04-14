/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
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

import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

/**
 * Map tile object.
 *
 * @author Douglas Lau
 */
public class TileMapObject implements MapObject {

	/** Transform for drawing image */
	private final AffineTransform transform;

	/** Get the map object transform */
	@Override
	public AffineTransform getTransform() {
		return transform;
	}

	/** Get the inverse map object transform */
	@Override
	public AffineTransform getInverseTransform() {
		return null;
	}

	/** Get the shape to draw the map object */
	@Override
	public Shape getShape() {
		return null;
	}

	/** Get the shape to draw the outline */
	@Override
	public Shape getOutlineShape() {
		return null;
	}

	/** Create a new tile map object */
	public TileMapObject(Image img, int x, int y) {
		image = img;
		transform = new AffineTransform();
		transform.translate(x, y);
	}

	/** Tile image */
	private final Image image;

	/** Get the tile image */
	public Image getImage() {
		return image;
	}
}
