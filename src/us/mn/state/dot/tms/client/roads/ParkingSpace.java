/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.map.MapObject;
import us.mn.state.dot.tms.geo.MapVector;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A parking space to render on a map.
 *
 * @author Douglas Lau
 */
public class ParkingSpace implements MapObject {

	/** Identity transform */
	static private final AffineTransform IDENTITY_TRANSFORM =
		new AffineTransform();

	/** Get the coordinate transform */
	@Override
	public AffineTransform getTransform() {
		return IDENTITY_TRANSFORM;
	}

	/** Get the inverse coordinate transform */
	@Override
	public AffineTransform getInverseTransform() {
		return IDENTITY_TRANSFORM;
	}

	/** Segment object */
	private final Segment segment;

	/** Get the r_node */
	public R_Node getR_Node() {
		return segment.getModel().r_node;
	}

	/** Lane of segment */
	private final Integer lane;

	/** Shape to render */
	private final Shape shape;

	/** Get the shape to draw this object */
	@Override
	public Shape getShape() {
		return shape;
	}

	/** Get the outline to draw this object */
	@Override
	public Shape getOutlineShape() {
		return shape;
	}

	/** Create a new parking space */
	public ParkingSpace(Segment s, Integer l, float scale, MapVector nv) {
		segment = s;
		lane = l;
		float width = calculateWidth(scale);
		float length = calculateLength(scale);
		normal = (nv != null) ? nv : getNormalVector();
		shape = createShape(width, length);
	}

	/** Normal vector */
	public final MapVector normal;

	/** Get the normal vector */
	private MapVector getNormalVector() {
		double x = segment.pos_a.getX() - segment.pos_b.getX();
		double y = segment.pos_a.getY() - segment.pos_b.getY();
		return new MapVector(x, y).perpendicular();
	}

	/** Calculate the segment width */
	private float calculateWidth(float scale) {
		return scale * 4;
	}

	/** Calculate the segment length */
	private float calculateLength(float scale) {
		return scale * 30;
	}

	/** Get the A offset */
	private int getOffsetA() {
		return (lane != null) ? (lane - 2) : -1;
	}

	/** Get the B offset */
	private int getOffsetB() {
		return (lane != null) ? (lane - 1) : 1;
	}

	/** Create the shape to draw this object */
	private Shape createShape(float width, float length) {
		float len_a = length * getOffsetA();
		float len_b = length * getOffsetB();
		double x = segment.pos_b.getX();
		double y = segment.pos_b.getY();
		double th = normal.getAngle();
		MapVector a = new MapVector( width, len_a).rotate(th);
		MapVector b = new MapVector(-width, len_a).rotate(th);
		MapVector c = new MapVector(-width, len_b).rotate(th);
		MapVector d = new MapVector( width, len_b).rotate(th);
		Path2D.Float path = new Path2D.Float(Path2D.WIND_NON_ZERO);
		path.moveTo(x + a.x, y + a.y);
		path.lineTo(x + b.x, y + b.y);
		path.lineTo(x + c.x, y + c.y);
		path.lineTo(x + d.x, y + d.y);
		path.closePath();
		return path;
	}

	/** Get the map segment tool tip */
	public String getTip() {
		String loc = GeoLocHelper.getCrossLandmark(
			segment.getModel().r_node.getGeoLoc());
		return (loc.length() > 0)
		      ? loc + ": " + getStatus()
		      : getStatus();
	}

	/** Get the parking space status */
	private String getStatus() {
		StringBuilder sb = new StringBuilder();
		Integer r = getReading();
		if (null == r)
			sb.append(I18N.get("parking.unknown"));
		else {
			sb.append((r < 150)
			      ? I18N.get("parking.vacant")
			      : I18N.get("parking.occupied"));
			sb.append("\n");
			sb.append(I18N.get("parking.reading"));
			sb.append(": ");
			sb.append(r);
		}
		return sb.toString();
	}

	/** Get the magnetometer reading */
	private Integer getReading() {
		Float o = getOcc();
		return (o != null) ? Math.round(o * 18) : null;
	}

	/** Get the occupancy */
	public Float getOcc() {
		return segment.getOcc(lane);
	}
}
