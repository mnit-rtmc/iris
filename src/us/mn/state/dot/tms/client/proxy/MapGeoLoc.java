/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.proxy;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import us.mn.state.dot.geokit.SphericalMercatorPosition;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.map.MapObject;

/**
 * Helper for creating location transforms.
 *
 * @author Douglas Lau
 */
public class MapGeoLoc implements MapObject {

	/** Radians to rotate marker for a Northbound device */
	static private final double RAD_NORTH = Math.toRadians(0);

	/** Radians to rotate marker for a Southbound device */
	static private final double RAD_SOUTH = Math.toRadians(180);

	/** Radians to rotate marker for an Eastbound device */
	static private final double RAD_EAST = Math.toRadians(270);

	/** Radians to rotate marker for a Westbound device */
	static private final double RAD_WEST = Math.toRadians(90);

	/** Get tangent value for "north" direction */
	static public double northTangent() {
		return RAD_NORTH;
	}

	/** Geo location */
	private final GeoLoc loc;

	/** Spherical mercator position */
	private SphericalMercatorPosition pos;

	/** Get the geo location */
	public GeoLoc getGeoLoc() {
		return loc;
	}

	/** Create a new location transform */
	public MapGeoLoc(GeoLoc l) {
		loc = l;
		doUpdate();
	}

	/** Update the geo loc map object */
	public void doUpdate() {
		if (manager != null) {
			Double tan = manager.getTangentAngle(this);
			if (tan != null)
				setTangent(tan);
		}
		updateTransform();
		updateInverseTransform();
	}

	/** Update the layer geometry */
	public void updateGeometry() {
		if (manager != null)
			manager.updateGeometry();
	}

	/** Get the default angle (radians) */
	private double getDefaultAngle() {
		switch (Direction.fromOrdinal(loc.getRoadDir())) {
		case NORTH:
			return RAD_NORTH;
		case SOUTH:
			return RAD_SOUTH;
		case EAST:
			return RAD_EAST;
		case WEST:
			return RAD_WEST;
		default:
			return RAD_NORTH;
		}
	}

	/** Tangent angle (radians) */
	private Double tangent = null;

	/** Set the tangent angle (radians) */
	public void setTangent(double t) {
		if (Double.isInfinite(t) || Double.isNaN(t))
			System.err.println("MapGeoLoc.setTangent: Bad tangent");
		else
			tangent = t;
	}

	/** Get the tangent angle (radians) */
	public double getTangent() {
		if (tangent != null)
			return tangent;
		else
			return getDefaultAngle();
	}

	/** Set a point relative to the location, offset by the tangent angle.
	 * @param p Point to set.
	 * @param distance Distance from the location, in meter units.
	 * @return true If the point was set, otherwise false. */
	public boolean setPoint(Point2D p, float distance) {
		SphericalMercatorPosition smp = pos;
		if (smp != null) {
			double x = smp.getX();
			double y = smp.getY();
			Double t = tangent;
			if (t != null) {
				double xo = distance * Math.cos(t);
				double yo = distance * Math.sin(t);
				p.setLocation(x + xo, y + yo);
			} else
				p.setLocation(x, y);
			return true;
		} else
			return false;
	}

	/** Transform for drawing device on map */
	private final AffineTransform transform = new AffineTransform();

	/** Update the traffic device transform */
	private void updateTransform() {
		pos = GeoLocHelper.getPosition(loc);
		if (pos != null)
			transform.setToTranslation(pos.getX(), pos.getY());
		else
			transform.setToIdentity();
		transform.rotate(getTangent());
	}

	/** Get the transform to render as a map object */
	@Override
	public AffineTransform getTransform() {
		return transform;
	}

	/** Inverse transform */
	private final AffineTransform itransform = new AffineTransform();

	/** Update the inverse transform */
	private void updateInverseTransform() {
		try {
			itransform.setTransform(transform.createInverse());
		}
		catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
	}

	/** Get the inverse transform */
	@Override
	public AffineTransform getInverseTransform() {
		return itransform;
	}

	/** Get a description of the location */
	public String getDescription() {
		return GeoLocHelper.getDescription(loc);
	}

	/** Proxy manager */
	private ProxyManager<? extends SonarObject> manager;

	/** Set the proxy manager */
	public void setManager(ProxyManager<? extends SonarObject> m) {
		manager = m;
	}

	/** Get the map object shape */
	@Override
	public Shape getShape() {
		return null;
	}

	/** Get the outline shape */
	@Override
	public Shape getOutlineShape() {
		return null;
	}
}
