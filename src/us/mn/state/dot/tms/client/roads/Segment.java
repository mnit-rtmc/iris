/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;

/**
 * A segment is the shape of a roadway segment on a map.
 *
 * @author Douglas Lau
 */
public class Segment implements MapObject {

	/** Identity transform */
	static protected final AffineTransform IDENTITY_TRANSFORM =
		new AffineTransform();

	/** R_Node for segment */
	protected final R_Node r_node;

	/** List of map geo locations */
	protected final List<MapGeoLoc> locs = new LinkedList<MapGeoLoc>();

	/** Create a new segment */
	public Segment() {
		r_node = null;
	}

	/** Create a new segment */
	public Segment(R_Node n) {
		r_node = n;
	}

	/** Add a point to the segment */
	public void addNode(MapGeoLoc loc) {
		locs.add(loc);
	}

	/** Get the shape to draw this object */
	public Shape getShape() {
		// FIXME: cache the shape whenever the zoom extent changes
		boolean first = true;
		Point2D.Float p = new Point2D.Float();
		Path2D.Float path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
		ListIterator<MapGeoLoc> li = locs.listIterator();
		while(li.hasNext()) {
			MapGeoLoc loc = li.next();
			if(loc.setPoint(p, 300)) {
				if(first) {
					path.moveTo(p.getX(), p.getY());
					first = false;
				} else
					path.lineTo(p.getX(), p.getY());
			}
		}
		while(li.hasPrevious()) {
			MapGeoLoc loc = li.previous();
			if(loc.setPoint(p, 75))
				path.lineTo(p.getX(), p.getY());
		}
		if(!locs.isEmpty())
			path.closePath();
		return path;
	}

	/** Get the coordinate transform */
	public AffineTransform getTransform() {
		return IDENTITY_TRANSFORM;
	}

	/** Get the inverse coordinate transform */
	public AffineTransform getInverseTransform() {
		return IDENTITY_TRANSFORM;
	}
}
