/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
import java.awt.geom.Point2D;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;

/**
 * A segment is the shape of a roadway segment on a map.
 *
 * @author Douglas Lau
 */
public class MapSegment implements MapObject {

	/** Identity transform */
	static protected final AffineTransform IDENTITY_TRANSFORM =
		new AffineTransform();

	/** Get the coordinate transform */
	public AffineTransform getTransform() {
		return IDENTITY_TRANSFORM;
	}

	/** Get the inverse coordinate transform */
	public AffineTransform getInverseTransform() {
		return IDENTITY_TRANSFORM;
	}

	/** Segment object */
	protected final Segment segment;

	/** Lane for segment (null for all lanes) */
	protected final Integer lane;

	/** Shape to render */
	protected final Shape shape;

	/** Get the shape to draw this object */
	public Shape getShape() {
		return shape;
	}

	/** Create a new segment */
	public MapSegment(Segment s, Integer l, float inner_a, float outer_a,
		float inner_b, float outer_b)
	{
		segment = s;
		lane = l;
		shape = createShape(inner_a, outer_a, inner_b, outer_b);
	}

	/** Create a new segment */
	public MapSegment(Segment s, float inner, float outer) {
		this(s, null, inner, outer, inner, outer);
	}

	/** Create the shape to draw this object */
	protected Shape createShape(float inner_a, float outer_a, float inner_b,
		float outer_b)
	{
		Point2D.Float p = new Point2D.Float();
		Path2D.Float path = new Path2D.Float(Path2D.WIND_NON_ZERO);
		MapGeoLoc loc_a = segment.loc_up;
		MapGeoLoc loc_b = segment.loc_dn;
		loc_a.setPoint(p, outer_a);
		path.moveTo(p.getX(), p.getY());
		loc_b.setPoint(p, outer_b);
		path.lineTo(p.getX(), p.getY());
		loc_b.setPoint(p, inner_b);
		path.lineTo(p.getX(), p.getY());
		loc_a.setPoint(p, inner_a);
		path.lineTo(p.getX(), p.getY());
		path.closePath();
		return path;
	}

	/** Get the map segment tool tip */
	public String getTip() {
		StringBuilder sb = new StringBuilder();
		String label = segment.getLabel(lane);
		if(label != null)
			sb.append(label);
		Integer flow = getFlow();
		if(flow != null) {
			sb.append("\n Flow = ");
			sb.append(flow);
		}
		Integer density = getDensity();
		if(density != null) {
			sb.append("\n Density = ");
			sb.append(density);
		}
		Integer speed = getSpeed();
		if(speed != null) {
			sb.append("\n Speed = ");
			sb.append(speed);
		}
		if(sb.length() > 0)
			return sb.toString();
		else
			return null;
	}

	/** Get the segment flow */
	public Integer getFlow() {
		return segment.getFlow(lane);
	}

	/** Get the segment density */
	public Integer getDensity() {
		return segment.getDensity(lane);
	}

	/** Get the segment speed */
	public Integer getSpeed() {
		return segment.getSpeed(lane);
	}
}
