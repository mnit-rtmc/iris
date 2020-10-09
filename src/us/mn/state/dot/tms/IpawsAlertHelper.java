/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.postgis.MultiPolygon;
import org.postgis.Point;
import org.postgis.Polygon;

import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;

/**
 * Helper class for IPAWS Alerts. Used on the client and server.
 *
 * @author Gordon Parikh
 */
public class IpawsAlertHelper extends BaseHelper {
	
	/** Don't instantiate */
	private IpawsAlertHelper() {
		assert false;
	}
	
	/** Lookup the alert with the specified name */
	static public IpawsAlert lookup(String name) {
		return (IpawsAlert) namespace.lookupObject(IpawsAlert.SONAR_TYPE, name);
	}

	/** Get an IpawsAlert object iterator */
	static public Iterator<IpawsAlert> iterator() {
		return new IteratorWrapper<IpawsAlert>(namespace.iterator(
				IpawsAlert.SONAR_TYPE));
	}
	
	/** Get the start date/time for an alert. Checks onset time first, then
	 *  effective time, and finally sent time (which is required).
	 */
	static public Date getAlertStart(IpawsAlert ia) {
		Date alertStart = null;
		if (ia != null) {
			alertStart = ia.getOnsetDate();
			if (alertStart == null)
				alertStart = ia.getEffectiveDate();
			if (alertStart == null)
				alertStart = ia.getSentDate();
		}
		return alertStart;
	}
	
	/** Build awt.Shape objects from a MultiPolygon, returning a list of Shape
	 *  objects with each representing a polygon.
	 */
	public static ArrayList<Shape> getShapes(IpawsAlert ia) {
		ArrayList<Shape> paths = new ArrayList<Shape>();
		if (ia == null || ia.getGeoPoly() == null)
			return paths;
		MultiPolygon mp = ia.getGeoPoly();
		
		// iterate over the polygons and points
		for (Polygon poly: mp.getPolygons()) {
			// draw a path of each polygon
			GeneralPath path = new GeneralPath();
			Point p = poly.getFirstPoint();
			if (p != null) {
				SphericalMercatorPosition smp = getSphereMercatorPos(p);
				path.moveTo(smp.getX(), smp.getY());
			}
			for (int i = 1; i < poly.numPoints(); ++i) {
				p = poly.getPoint(i);
				SphericalMercatorPosition smp = getSphereMercatorPos(p);
				path.lineTo(smp.getX(), smp.getY());
			}
			path.closePath();	// should already be closed, but just in case
			paths.add(path);
		}
		return paths;
	}

	/** Convert a PostGIS Point to a SphericalMercatorPosition object. */
	private static SphericalMercatorPosition getSphereMercatorPos(Point p) {
		// construct a lat/lon Position object first, then convert
		// that to a SphericalMercatorPosition
		Position pos = new Position(p.y, p.x);
		return SphericalMercatorPosition.convert(pos);
	}
}
