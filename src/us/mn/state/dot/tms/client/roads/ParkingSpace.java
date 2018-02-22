/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
	public ParkingSpace(Segment s, float scale, Double t) {
		segment = s;
		float width = calculateWidth(scale);
		float length = calculateLength(scale);
		shape = createShape(width, length);
		tangent = (t != null) ? t : getTangent();
	}

	/** Tangent angle (radians) */
	public final double tangent;

	/** Get the tangent angle (radians) */
	private double getTangent() {
		return segment.tangent_b - Math.PI / 4;
	}

	/** Calculate the segment width */
	private float calculateWidth(float scale) {
		return scale * 4;
	}

	/** Calculate the segment length */
	private float calculateLength(float scale) {
		return scale * 30;
	}

	/** Create the shape to draw this object */
	private Shape createShape(float width, float length) {
		Path2D.Float path = new Path2D.Float(Path2D.WIND_NON_ZERO);
		Point2D.Float p = new Point2D.Float();
		setPoint(p, segment.pos_b, width, length);
		path.moveTo(p.getX(), p.getY());
		setPoint(p, segment.pos_b, -width, length);
		path.lineTo(p.getX(), p.getY());
		setPoint(p, segment.pos_b, -width, -length);
		path.lineTo(p.getX(), p.getY());
		setPoint(p, segment.pos_b, width, -length);
		path.lineTo(p.getX(), p.getY());
		path.closePath();
		return path;
	}

	/** Set a point relative to the location, offset by the tangent angle.
	 * @param p Point to set. */
	private void setPoint(Point2D p, SphericalMercatorPosition pos,
		float w, float l)
	{
		assert (pos != null);
		double x = pos.getX();
		double y = pos.getY();
		double s = Math.sin(tangent);
		double c = Math.cos(tangent);
		double xo = w * c + l * s;
		double yo = w * s + l * c;
		p.setLocation(x + xo, y + yo);
	}

	/** Get the map segment tool tip */
	public String getTip() {
		return getDescription() + ": " + getStatus();
	}

	/** Get description */
	private String getDescription() {
		return GeoLocHelper.getCrossOrLandmark(
			segment.getModel().r_node.getGeoLoc());
	}

	/** Get the parking space status */
	private String getStatus() {
		Float o = getOcc();
		if (null == o)
			return I18N.get("parking.unknown");
		else {
			int s = Math.round(o * 18);
			if (s < 150)
				return I18N.get("parking.vacant");
			else
				return I18N.get("parking.occupied");
		}
	}

	/** Get the occupancy */
	public Float getOcc() {
		return segment.getOcc(null);
	}
}
