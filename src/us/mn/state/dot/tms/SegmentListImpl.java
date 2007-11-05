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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.rmi.Remote;
import java.rmi.RemoteException;
import us.mn.state.dot.vault.FieldMap;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * SegmentListImpl is a collection which stores an indexed list of remote
 * objects, notably freeway continuity segments.
 *
 * @author Douglas Lau
 */
class SegmentListImpl extends AbstractListImpl {

	/** Number of bad consecutive stations to fail travel time */
	static protected final int BAD_STATION_COUNT = 2;

	/** ObjectVault table name */
	static public final String tableName = "segment_list";

	/** ArrayList to hold all the elements in the list */
	protected final ArrayList list;

	/** Freeway for this corridor */
	protected final RoadwayImpl freeway;

	/** (General) Direction of travel */
	protected final short direction;

	/** Number of lanes to start with */
	protected int startingLanes = 0;

	/** Maximum yellow fog line shift */
	protected transient int maxYellow = 0;

	/** Get an iterator for this list */
	Iterator iterator() {
		return list.iterator();
	}

	/** Create a new segment list */
	public SegmentListImpl(RoadwayImpl f, short d) throws RemoteException {
		super( true );
		list = new ArrayList( 100 );
		freeway = f;
		direction = d;
	}

	/** Create a segment list from an ObjectVault field map */
	protected SegmentListImpl( FieldMap fields ) throws RemoteException {
		super( true );
		list = (ArrayList)fields.get( "list" );
		freeway = (RoadwayImpl)fields.get("freeway");
		direction = fields.getShort( "direction" );
	}

	/** Initialize the segments */
	public void initTransients() throws TMSException, RemoteException {
		revalidate(0);
		Iterator it = list.iterator();
		while(it.hasNext()) {
			SegmentImpl segment = (SegmentImpl)it.next();
			segment.initTransients();
		}
	}

	/** Get an array of the list of segments */
	public synchronized SegmentImpl[] toArray() {
		SegmentImpl[] array = new SegmentImpl[list.size()];
		return (SegmentImpl [])list.toArray(array);
	}

	/** Return a station list */
	protected synchronized List stationList(float upstream,
		StationSegmentImpl downstream)
	{
		LinkedList l = new LinkedList();
		SegmentImpl u = null;
		Iterator it = list.iterator();
		while(it.hasNext()) {
			SegmentImpl s = (SegmentImpl)it.next();
			if(!(s instanceof StationSegmentImpl)) continue;
			if(s.getMile() == null) continue;
			if(upstream > s.getMile().floatValue()) {
				u = s;
			} else {
				if(l.isEmpty() && u != null) l.add(u);
				l.add(s);
				if(s.equals(downstream)) return l;
			}
		}
		return null;
	}

	/** Check a route a too many bad consecutive stations */
	protected boolean checkBadConsecutiveStations(List route) {
		Iterator it = route.iterator();
		int bad = 0;
		while(it.hasNext()) {
			StationSegmentImpl station =
				(StationSegmentImpl)it.next();
			if(station.getTravelSpeed() <= 0) {
				it.remove();
				bad++;
			}
			else bad = 0;
			if(bad >= BAD_STATION_COUNT) return true;
		}
		if(route.isEmpty()) return true;
		return false;
	}

	/** Return a station iterator */
	public Iterator stationIterator(float upstream,
		StationSegmentImpl downstream)
	{
		List route = stationList(upstream, downstream);
		if(route == null) return null;
		if(checkBadConsecutiveStations(route)) return null;
		return route.iterator();
	}

	/** Get the direction of travel */
	public short getDirection() { return direction; }

	/** Start the segment list */
	public synchronized void setStart(int lanes) throws TMSException {
		if(startingLanes == lanes) return;
		try {
			vault.update(this, "startingLanes", new Integer(lanes),
				getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		startingLanes = lanes;
		revalidate(0);
		notifyUpdate();
	}

	/** Get the starting number of lanes for this list */
	public int getStart() { return startingLanes; }

	/** Get the maximum yellow fog line shift */
	public int getMaxYellow() { return maxYellow; }

	/** Revalidate the segments */
	protected void revalidate(int startingSegment) {
		int lanes = startingLanes;
		int shift = 0;
		int cd = 0;
		if(startingSegment > 0) {
			SegmentImpl segment = (SegmentImpl)list.get(
				startingSegment - 1);
			lanes = segment.getLanes();
			shift = segment.getShift();
			cd = segment.getCd();
		}
		ListIterator li = list.listIterator(startingSegment);
		while(li.hasNext()) {
			SegmentImpl segment = (SegmentImpl)li.next();
			segment.validate(lanes, shift, cd);
			lanes = segment.getLanes();
			shift = segment.getShift();
			cd = segment.getCd();
		}
		calculateMaxYellow();
	}

	/** Calculate the maximum yellow fog line shift */
	protected void calculateMaxYellow() {
		int shift = 0;
		ListIterator li = list.listIterator();
		while(li.hasNext()) {
			SegmentImpl segment = (SegmentImpl)li.next();
			int s = segment.getShift();
			if(s > shift) shift = s;
		}
		maxYellow = shift;
	}

	/** Add a segment to the list */
	public synchronized boolean add(int index, SegmentImpl segment)
		throws TMSException
	{
		try { vault.add(list, index, segment, getUserName()); }
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		segMap.add(segment.getOID(), segment);
		list.add(index, segment);
		revalidate(index);
		notifyAdd(index, segment);
		return true;
	}

	/** Update an element in the list (index is zero-relative) */
	public synchronized void update( int index ) {
		Remote element = (Remote)list.get( index );
		notifySet( index, element );
	}

	/** Delete an element from the list (index is zero-relative) */
	public synchronized void delete(int index) throws TMSException,
		RemoteException
	{
		SegmentImpl seg = (SegmentImpl)list.get(index);
		seg.setDetectors(new Detector[0]);
		if(seg instanceof StationSegmentImpl) {
			StationSegmentImpl station = (StationSegmentImpl)seg;
			station.setIndex(null);
		}
		segMap.remove(seg.getOID());
		try { vault.remove(list, index, getUserName()); }
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		list.remove(index);
		revalidate(index);
		notifyRemove(index);
	}

	/** Get an element from the list */
	public synchronized TMSObject getElement(int index) {
		return (TMSObject)list.get(index);
	}

	/** Find the meterable segment with the specified cross street */
	public synchronized MeterableImpl findMeterable(RoadwayImpl xStreet,
		short xDir)
	{
		ListIterator li = list.listIterator();
		while(li.hasNext()) {
			SegmentImpl segment = (SegmentImpl)li.next();
			LocationImpl loc = (LocationImpl)segment.getLocation();
			if(segment instanceof MeterableImpl &&
				xStreet.equals(loc.getCrossStreet()) &&
				xDir == loc.getCrossDir())
			{
				return (MeterableImpl)segment;
			}
		}
		return null;
	}

	/** Subscribe a listener to this list */
	public synchronized Object[] subscribe( RemoteList listener ) {
		super.subscribe( listener );
		int count = list.size();
		if( count < 1 ) return null;
		TMSObject[] elements = new TMSObject[ count ];
		for( int i = 0; i < count; i++ )
			elements[ i ] = (TMSObject)list.get( i );
		return elements;
	}

	/** Print the corridor out as XML */
	public synchronized void printXml(PrintWriter out) {
		float mile = Float.NEGATIVE_INFINITY;
		Iterator it = iterator();
		while(it.hasNext()) {
			SegmentImpl seg = (SegmentImpl)it.next();
			if(seg instanceof StationSegmentImpl) {
				StationSegmentImpl s = (StationSegmentImpl)seg;
				mile = s.printXml(out, mile);
			}
		}
	}
}
