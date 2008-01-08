/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2008  Minnesota Department of Transportation
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

import java.rmi.RemoteException;

/**
 * A Location contains attributes necessary to describe a map location.
 *
 * @author Douglas Lau
 */
public class LocationImpl extends TMSObjectImpl implements Location, Cloneable,
	Storable
{
	/** ObjectVault table name */
	static public final String tableName = "location";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Create a new location */
	public LocationImpl() throws RemoteException {
		super();
	}

	/** Get a description of the location */
	public String getDescription() {
		RoadwayImpl f = freeway;
		StringBuffer b = new StringBuffer();
		if(f != null) {
			String free = f.getName() + " " + DIRECTION[free_dir];
			b.append(free.trim());
		}
		String c = getCrossDescription();
		if(c != null) {
			if(b.length() > 0)
				b.append(' ');
			b.append(c);
		}
		if(b.length() > 0)
			return b.toString();
		else
			return "Unknown location";
	}

	/** Get the freeway corridor ID */
	public String getCorridorID() {
		RoadwayImpl f = freeway;
		if(f == null)
			return "null";
		StringBuilder b = new StringBuilder();
		String ab = f.getAbbreviated();
		if(ab != null)
			b.append(ab);
		else
			return "null";
		short fd = f.filterDirection(free_dir);
		if(fd > 0 && fd < DIRECTION.length)
			b.append(DIRECTION[fd]);
		return b.toString();
	}

	/** Get the freeway corridor */
	public String getCorridor() {
		RoadwayImpl f = freeway;
		if(f == null)
			return null;
		StringBuilder b = new StringBuilder();
		b.append(f.getName());
		short fd = f.filterDirection(free_dir);
		if(fd > 0 && fd < DIRECTION.length) {
			b.append(' ');
			b.append(DIRECTION[fd]);
		}
		return b.toString();
	}

	/** Check if another location is on the same corridor */
	public boolean isSameCorridor(LocationImpl other) {
		RoadwayImpl f = freeway;
		if(f == null)
			return false;
		return (f == other.freeway) &&
			(f.filterDirection(free_dir) ==
			 f.filterDirection(other.free_dir));
	}

	/** Get a description of the cross-street location */
	public String getCrossDescription() {
		RoadwayImpl c = cross_street;
		if(c != null) {
			String cross = MODIFIER[cross_mod] + " " +
				c.getName() + " " + DIRECTION[cross_dir];
			return cross.trim();
		} else
			return null;
	}

	/** Freeway location */
	protected RoadwayImpl freeway;

	/** Set the freeway location */
	public void setFreeway(String name) throws TMSException {
		setFreeway((RoadwayImpl)roadList.getElement(name));
	}

	/** Set the freeway location */
	protected synchronized void setFreeway(RoadwayImpl f)
		throws TMSException
	{
		if(f == freeway)
			return;
		if(f == null)
			store.update(this, "freeway", "0");
		else
			store.update(this, "freeway", f.getOID());
		freeway = f;
	}

	/** Get the freeway locaiton */
	public Roadway getFreeway() {
		return freeway;
	}

	/** Freeway direction */
	protected short free_dir;

	/** Set the freeway direction */
	public synchronized void setFreeDir(short d) throws TMSException {
		if(d == free_dir)
			return;
		if(d < 0 || d > DIR_FREEWAY.length)
			throw new ChangeVetoException("Invalid direction");
		store.update(this, "free_dir", d);
		free_dir = d;
	}

	/** Get the freeway direction */
	public short getFreeDir() {
		return free_dir;
	}

	/** Nearest cross-street */
	protected RoadwayImpl cross_street;

	/** Set the cross-street location */
	public void setCrossStreet(String name) throws TMSException {
		setCrossStreet((RoadwayImpl)roadList.getElement(name));
	}

	/** Set the cross-street location */
	protected synchronized void setCrossStreet(RoadwayImpl x)
		throws TMSException
	{
		if(x == cross_street)
			return;
		if(x == null)
			store.update(this, "cross_street", "0");
		else
			store.update(this, "cross_street", x.getOID());
		cross_street = x;
	}

	/** Get the cross-street location */
	public Roadway getCrossStreet() {
		return cross_street;
	}

	/** Cross street direction */
	protected short cross_dir;

	/** Set the cross street direction */
	public synchronized void setCrossDir(short d) throws TMSException {
		if(d == cross_dir)
			return;
		if(d < 0 || d > DIRECTION.length)
			throw new ChangeVetoException("Invalid direction");
		store.update(this, "cross_dir", d);
		cross_dir = d;
	}

	/** Get the cross street direction */
	public short getCrossDir() {
		return cross_dir;
	}

	/** Cross street modifier */
	protected short cross_mod;

	/** Set the cross street modifier */
	public synchronized void setCrossMod(short m) throws TMSException {
		if(m == cross_mod)
			return;
		if(m < 0 || m > MODIFIER.length)
			throw new ChangeVetoException("Invalid modifier");
		store.update(this, "cross_mod", m);
		cross_mod = m;
	}

	/** Get the cross street modifier */
	public short getCrossMod() {
		return cross_mod;
	}

	/** UTM Easting */
	protected int easting;

	/** Set the UTM Easting */
	public synchronized void setEasting(int x) throws TMSException {
		if(x == easting)
			return;
		if(x < 0)
			throw new ChangeVetoException("Invalid Easting");
		store.update(this, "easting", x);
		easting = x;
	}

	/** Get the UTM Easting */
	public int getEasting() {
		return easting;
	}

	/** UTM Easting offset */
	protected int east_off;

	/** Set the UTM Easting offset */
	public synchronized void setEastOffset(int x) throws TMSException {
		if(x == east_off)
			return;
		store.update(this, "east_off", x);
		east_off = x;
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

	/** UTM Northing */
	protected int northing;

	/** Set the UTM Northing */
	public synchronized void setNorthing(int y) throws TMSException {
		if(y == northing)
			return;
		if(y < 0)
			throw new ChangeVetoException("Invalid Northing");
		store.update(this, "northing", y);
		northing = y;
	}

	/** Get the UTM Northing */
	public int getNorthing() {
		return northing;
	}

	/** UTM Northing offset */
	protected int north_off;

	/** Set the UTM Northing offset */
	public synchronized void setNorthOffset(int y) throws TMSException {
		if(y == north_off)
			return;
		store.update(this, "north_off", y);
		north_off = y;
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

	/** Check if the UTM coordinates are zero */
	public boolean isZero() {
		return (northing == 0) && (north_off == 0) &&
			(easting == 0) && (east_off == 0);
	}

	/** Calculate the distance to another location (in meters) */
	public double metersTo(LocationImpl other) {
		int x = getTrueEasting() - other.getTrueEasting();
		int y = getTrueNorthing() - other.getTrueNorthing();
		return Math.hypot(x, y);
	}

	/** Test if another location matches */
	public boolean matches(LocationImpl loc) {
		RoadwayImpl f = freeway;
		RoadwayImpl c = cross_street;
		if(f == null || c == null)
			return false;
		return f.equals(loc.getFreeway()) &&
			(f.filterDirection(free_dir) ==
			 f.filterDirection(loc.getFreeDir())) &&
			c.equals(loc.getCrossStreet()) &&
			cross_dir == loc.getCrossDir() &&
			cross_mod == loc.getCrossMod();
	}

	/** Test if another location matches (including CD roads) */
	public boolean matchesRoot(LocationImpl loc) {
		RoadwayImpl f = freeway;
		RoadwayImpl of = loc.freeway;
		RoadwayImpl c = cross_street;
		if(f == null || of == null || c == null)
			return false;
		return f.matchRootName(of) &&
			(f.filterDirection(free_dir) ==
			 f.filterDirection(loc.getFreeDir())) &&
			c.equals(loc.getCrossStreet()) &&
			cross_dir == loc.getCrossDir() &&
			cross_mod == loc.getCrossMod();
	}

	/** Test if another location has freeway/cross-street swapped */
	protected boolean isSwapped(LocationImpl other) {
		RoadwayImpl f = freeway;
		RoadwayImpl c = cross_street;
		RoadwayImpl of = other.freeway;
		RoadwayImpl oc = other.cross_street;
		if(f == null || c == null || of == null || oc == null)
			return false;
		return (cross_mod == other.cross_mod) &&
			f.matchRootName(oc) && c.matchRootName(of);
	}

	/** Test if a ramp matches another ramp location */
	public boolean rampMatches(LocationImpl other) {
		return (cross_dir == other.free_dir) &&
			(free_dir == other.cross_dir) &&
			isSwapped(other);
	}

	/** Test if an access node matches a ramp location */
	public boolean accessMatches(LocationImpl other) {
		RoadwayImpl f = freeway;
		RoadwayImpl c = cross_street;
		RoadwayImpl of = other.freeway;
		RoadwayImpl oc = other.cross_street;
		if(f == null || c == null || of == null || oc == null)
			return false;
		return (cross_mod == other.cross_mod) &&
			f.matchRootName(of) && c.matchRootName(oc);
	}
}
