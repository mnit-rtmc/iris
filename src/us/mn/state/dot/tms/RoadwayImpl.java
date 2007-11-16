/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.rmi.RemoteException;
import us.mn.state.dot.vault.FieldMap;

/**
 * The RoadwayImpl class represents a single roadway which can be used to
 * logically group traffic management devices.
 *
 * @author Douglas Lau
 */
class RoadwayImpl extends TMSObjectImpl implements Roadway, Storable {

	/** ObjectVault table name */
	static public final String tableName = "roadway";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Abbreviation regex pattern */
	static protected final Pattern ABBREV_PATTERN =
		Pattern.compile("[A-Za-z0-9]{0,6}");

	/** Create a new Roadway */
	public RoadwayImpl(String n) throws ChangeVetoException,
		RemoteException
	{
		super();
		validateText(n);
		name = n;
		abbreviated = "";
	}

	/** Create a Roadway from an ObjectVaule field map */
	protected RoadwayImpl( FieldMap fields ) throws RemoteException {
		super();
		name = (String)fields.get( "name" );
	}
	
	/** Initialize segment lists. Called after Roadway is loaded from the
	 *  database. */
	public void initTransients() throws TMSException, RemoteException {
		if(segment1 != null)
			segment1.initTransients();
		if(segment2 != null)
			segment2.initTransients();
	}

	/** Get a String representation of this roadway */
	public String toString() {
		return name;
	}

	/** Test if another roadway starts with the same name */
	public boolean matchRootName(RoadwayImpl other) {
		return name.startsWith(other.name) ||
			other.name.startsWith(name);
	}

	/** Roadway name */
	protected final String name;

	/** Get the name */
	public String getName() {
		return name;
	}

	/** Get the object key */
	public String getKey() {
		return name;
	}

	/** Abbreviated name (for detector and station names) */
	protected String abbreviated;

	/** Set the abbreviated name */
	public synchronized void setAbbreviated(String a) throws TMSException {
		if(a.equals(abbreviated))
			return;
		Matcher m = ABBREV_PATTERN.matcher(a);
		if(!m.matches())
			throw new ChangeVetoException("Invalid abbrev: " + a);
		store.update(this, "abbreviated", a);
		abbreviated = a;
	}

	/** Get the abbreviated name */
	public String getAbbreviated() {
		return abbreviated;
	}

	/** Roadway type */
	protected short type;

	/** Set the roadway type */
	public synchronized void setType(short t) throws TMSException,
		RemoteException
	{
		if(t == type)
			return;
		updateSegmentLists(t, direction);
		store.update(this, "type", t);
		type = t;
	}

	/** Get the roadway type */
	public short getType() {
		return type;
	}

	/** Check if the roadway is a freeway */
	public boolean isFreeway() {
		return type == FREEWAY || type == CD_ROAD;
	}

	/** Direction (NORTH_SOUTH or EAST_WEST) */
	protected short direction;

	/** Set the direction */
	public synchronized void setDirection(short d) throws TMSException,
		RemoteException
	{
		if(d == direction)
			return;
		updateSegmentLists(type, d);
		store.update(this, "direction", d);
		direction = d;
	}

	/** Get the direction */
	public short getDirection() {
		return direction;
	}

	/** Segment list for the NORTH / EAST direction */
	protected SegmentListImpl segment1 = null;

	/** Segment list for the SOUTH / WEST direction */
	protected SegmentListImpl segment2 = null;

	/** Is this roadway deletable? */
	public boolean isDeletable() throws TMSException {
		if(segment1 != null || segment2 != null)
			return false;
		return super.isDeletable();
	}

	/** Update segment lists */
	protected void updateSegmentLists(short t, short d)
		throws TMSException, RemoteException
	{
		boolean before = type == FREEWAY && direction > NONE;
		boolean after = t == FREEWAY && d > NONE;
		if(before == after)
			return;
		SegmentListImpl seg1 = null;
		SegmentListImpl seg2 = null;
		if(before) {
			seg1 = destroySegmentList(segment1);
			seg2 = destroySegmentList(segment2);
		}
		if(after) {
			if(d == NORTH_SOUTH) {
				seg1 = createSegmentList(segment1, NORTH);
				seg2 = createSegmentList(segment2, SOUTH);
			}
			if(d == EAST_WEST) {
				seg1 = createSegmentList(segment1, EAST);
				seg2 = createSegmentList(segment2, WEST);
			}
		}
		store.update(this, "segment1", seg1.getOID());
		store.update(this, "segment2", seg2.getOID());
		segment1 = seg1;
		segment2 = seg2;
	}

	/** Create the segment (continuity) lists */
	protected SegmentListImpl createSegmentList(SegmentListImpl s,
		short d) throws TMSException, RemoteException
	{
		if(s != null)
			throw new ChangeVetoException("Segment list exists");
		return new SegmentListImpl(this, d);
	}

	/** Destroy a segment list */
	protected SegmentListImpl destroySegmentList(SegmentListImpl s)
		throws TMSException
	{
		if(s != null && !s.isDeletable())
			throw new ChangeVetoException("Cannot delete object");
		return null;
	}

	/** Get a segment list for the specified direction */
	public SegmentListImpl getSegmentList(short d) {
		d = filterDirection(d);
		if(direction == NORTH_SOUTH) {
			if(d == NORTH)
				return segment1;
			if(d == SOUTH)
				return segment2;
		}
		if(direction == EAST_WEST) {
			if(d == EAST)
				return segment1;
			if(d == WEST)
				return segment2;
		}
		return null;
	}

	/** Filter the freeway direction which matches the given direction */
	public short filterDirection(short d) {
		if(direction == EAST_WEST) {
			// The special cases are for I-494, which is an
			// East-West freeway, but has a North-South portion
			if(d == SOUTH)
				return EAST;
			if(d == NORTH)
				return WEST;
		}
		return d;
	}
}
