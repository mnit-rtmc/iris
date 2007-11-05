/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2007  Minnesota Department of Transportation
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

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.rmi.RemoteException;
import us.mn.state.dot.tms.Location;
import us.mn.state.dot.tms.Roadway;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;
import us.mn.state.dot.tms.utils.TmsObjectProxy;

/**
 * Proxy for a Locaiton object.
 *
 * @author Douglas Lau
 */
public class LocationProxy extends TmsObjectProxy implements Location {

	/** Radians to rotate marker for a Northbound device */
	static protected final double RAD_NORTH = Math.toRadians(0);

	/** Radians to rotate marker for a Southbound device */
	static protected final double RAD_SOUTH = Math.toRadians(180);

	/** Radians to rotate marker for an Eastbound device */
	static protected final double RAD_EAST = Math.toRadians(270);

	/** Radians to rotate marker for a Westbound device */
	static protected final double RAD_WEST = Math.toRadians(90);

	/** Remote location */
	protected final Location location;

	/** Create a new location proxy */
	public LocationProxy(Location l) {
		super(l);
		location = l;
	}

	/** Refresh the update information */
	public void updateUpdateInfo() throws RemoteException {
		description = location.getDescription();
		corridor = location.getCorridor();
		if(corridor == null)
			corridor = "";
		cross_desc = location.getCrossDescription();
		freeway = new RoadwayProxy(location.getFreeway());
		free_dir = location.getFreeDir();
		cross_mod = location.getCrossMod();
		cross_street = new RoadwayProxy(location.getCrossStreet());
		cross_dir = location.getCrossDir();
		easting = location.getEasting();
		east_off = location.getEastOffset();
		northing = location.getNorthing();
		north_off = location.getNorthOffset();
		updateTransform();
		updateInverseTransform();
	}

	/** Cached location description */
	protected String description;

	/** Get the string location */
	public String getDescription() {
		return description;
	}

	/** Cached corridor */
	protected String corridor;

	/** Cached cross-street description */
	protected String cross_desc;

	/** Get the cross-street description */
	public String getCrossDescription() {
		return cross_desc;
	}

	/** Cached freeway */
	protected RoadwayProxy freeway;

	/** Set the freeway */
	public void setFreeway(String id) throws TMSException, RemoteException {
		location.setFreeway(id);
	}

	/** Get the freeway */
	public Roadway getFreeway() {
		return freeway;
	}

	/** Cached freeway direction */
	protected short free_dir;

	/** Set the freeway direction */
	public void setFreeDir(short d) throws TMSException, RemoteException {
		location.setFreeDir(d);
	}

	/** Get the freeway direction */
	public short getFreeDir() {
		return free_dir;
	}

	/** Get the freeway corridor */
	public String getCorridor() {
		return corridor;
	}

	/** Cached cross street */
	protected RoadwayProxy cross_street;

	/** Set the cross-street location */
	public void setCrossStreet(String name) throws TMSException,
		RemoteException
	{
		location.setCrossStreet(name);
	}

	/** Get the cross-street */
	public Roadway getCrossStreet() {
		return cross_street;
	}

	/** Cached cross street direction */
	protected short cross_dir;

	/** Set the cross street direction */
	public void setCrossDir(short d) throws TMSException, RemoteException {
		location.setCrossDir(d);
	}

	/** Get the cross street direction */
	public short getCrossDir() {
		return cross_dir;
	}

	/** Cross street modifier */
	protected short cross_mod;

	/** Set the cross street modifier */
	public void setCrossMod(short m) throws TMSException, RemoteException {
		location.setCrossMod(m);
	}

	/** Get the cross street modifier */
	public short getCrossMod() {
		return cross_mod;
	}

	/** Cached UTM easting */
	protected int easting;

	/** Set the UTM Easting */
	public void setEasting(int x) throws TMSException, RemoteException {
		location.setEasting(x);
	}

	/** Get the UTM Easting */
	public int getEasting() {
		return easting;
	}

	/** Cached UTM easting offset */
	protected int east_off;

	/** Set the UTM Easting offset */
	public void setEastOffset(int x) throws TMSException, RemoteException {
		location.setEastOffset(x);
	}

	/** Get the UTM Easting offset */
	public int getEastOffset() {
		return east_off;
	}

	/** Get the true UTM Easting (without offset) */
	public int getTrueEasting() {
		if(easting > 0)
			return easting;
		else
			return east_off;
	}

	/** Cached UTM northing */
	protected int northing;

	/** Set the UTM Northing */
	public void setNorthing(int y) throws TMSException, RemoteException {
		location.setNorthing(y);
	}

	/** Get the UTM Northing */
	public int getNorthing() {
		return northing;
	}

	/** Cached UTM northing offset */
	protected int north_off;

	/** Set the UTM Northing offset */
	public void setNorthOffset(int y) throws TMSException, RemoteException {
		location.setNorthOffset(y);
	}

	/** Get the UTM Northing offset */
	public int getNorthOffset() {
		return north_off;
	}

	/** Get the true UTM Northing (without offset) */
	public int getTrueNorthing() {
		if(northing > 0)
			return northing;
		else
			return north_off;
	}

	/** Check if the UTM coordinates is zero */
	public boolean isZero() {
		return (northing == 0) && (north_off == 0) &&
			(easting == 0) && (east_off == 0);
	}

	/** Check if the location has a "true" GPS reading */
	public boolean hasGPS() {
		return northing > 0 && easting > 0;
	}

	/** Get the vector from the origin */
	public Vector getVector() {
		int x = getTrueEasting();
		int y = getTrueNorthing();
		return new Vector(x, y);
	}

	/** Get the default angle (radians) */
	public double getDefaultAngle() {
		switch(free_dir) {
			case Roadway.NORTH:
				return RAD_NORTH;
			case Roadway.SOUTH:
				return RAD_SOUTH;
			case Roadway.EAST:
				return RAD_EAST;
			case Roadway.WEST:
				return RAD_WEST;
		}
		return RAD_NORTH;
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
		transform.setToTranslation(easting + east_off,
			northing + north_off);
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
}
