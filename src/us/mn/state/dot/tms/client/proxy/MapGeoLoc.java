/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2018  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.map.MapObject;
import us.mn.state.dot.tms.geo.MapVector;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;

/**
 * Helper for creating location transforms.
 *
 * @author Douglas Lau
 */
public class MapGeoLoc implements MapObject {

	/** Geo location */
	private final GeoLoc loc;

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
			MapVector nv = manager.getNormalVector(this);
			if (nv != null)
				setNormalVector(nv);
		}
		updateTransform();
		updateInverseTransform();
	}

	/** Update the layer geometry */
	public void updateGeometry() {
		if (manager != null)
			manager.updateGeometry();
	}

	/** Normal vector */
	private MapVector normal;

	/** Set the normal vector */
	public void setNormalVector(MapVector nv) {
		normal = nv;
	}

	/** Get the normal vector */
	public MapVector getNormalVector() {
		return (normal != null) ? normal : defaultNormal();
	}

	/** Get the default normal vector */
	private MapVector defaultNormal() {
		return GeoLocHelper.normalVector(loc.getRoadDir());
	}

	/** Transform for drawing device on map */
	private final AffineTransform transform = new AffineTransform();

	/** Update the traffic device transform */
	private void updateTransform() {
		SphericalMercatorPosition pos = GeoLocHelper.getPosition(loc);
		if (pos != null)
			transform.setToTranslation(pos.getX(), pos.getY());
		else
			transform.setToIdentity();
		transform.rotate(getNormalVector().getAngle());
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
