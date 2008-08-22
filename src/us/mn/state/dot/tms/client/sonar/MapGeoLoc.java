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
package us.mn.state.dot.tms.client.sonar;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Road;

/**
 * Helper for creating location transforms.
 *
 * @author Douglas Lau
 */
public class MapGeoLoc implements MapObject {

	/** Radians to rotate marker for a Northbound device */
	static protected final double RAD_NORTH = Math.toRadians(0);

	/** Radians to rotate marker for a Southbound device */
	static protected final double RAD_SOUTH = Math.toRadians(180);

	/** Radians to rotate marker for an Eastbound device */
	static protected final double RAD_EAST = Math.toRadians(270);

	/** Radians to rotate marker for a Westbound device */
	static protected final double RAD_WEST = Math.toRadians(90);

	/** Geo location */
	protected final GeoLoc loc;

	/** Create a new location transform */
	public MapGeoLoc(GeoLoc l) {
		loc = l;
		doUpdate();
	}

	/** Update the geo loc map object */
	public void doUpdate() {
		updateTransform();
		updateInverseTransform();
	}

	/** Get the default angle (radians) */
	protected double getDefaultAngle() {
		switch(loc.getFreeDir()) {
			case Road.NORTH:
				return RAD_NORTH;
			case Road.SOUTH:
				return RAD_SOUTH;
			case Road.EAST:
				return RAD_EAST;
			case Road.WEST:
				return RAD_WEST;
			default:
				return RAD_NORTH;
		}
	}

	/** Tangent angle (radians) */
	protected Double tangent = null;

	/** Set the tangent angle (radians) */
	public void setTangent(double t) {
		tangent = t;
		updateTransform();
		updateInverseTransform();
	}

	/** Get the tangent angle (radians) */
	protected double getTangent() {
		if(tangent != null)
			return tangent;
		else
			return getDefaultAngle();
	}

	/** Transform for drawing device on map */
	protected final AffineTransform transform = new AffineTransform();

	/** Update the traffic device transform */
	protected void updateTransform() {
		int easting = 0;
		int northing = 0;
		if(loc.getEasting() != null)
			easting += loc.getEasting();
		if(loc.getEastOffset() != null)
			easting += loc.getEastOffset();
		if(loc.getNorthing() != null)
			northing += loc.getNorthing();
		if(loc.getNorthOffset() != null)
			northing += loc.getNorthOffset();
		transform.setToTranslation(easting, northing);
		transform.rotate(getTangent());
	}

	/** Get the transform to render as a map object */
	public AffineTransform getTransform() {
		return transform;
	}

	/** Inverse transform */
	protected final AffineTransform itransform = new AffineTransform();

	/** Update the inverse transform */
	protected void updateInverseTransform() {
		try {
			itransform.setTransform(transform.createInverse());
		}
		catch(NoninvertibleTransformException e) {
			e.printStackTrace();
		}
	}

	/** Get the inverse transform */
	public AffineTransform getInverseTransform() {
		return itransform;
	}

	/** Get a description of the location */
	public String getDescription() {
		return GeoLocHelper.getDescription(loc);
	}
}
