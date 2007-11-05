/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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

import java.util.Iterator;
import java.util.TreeMap;
import java.rmi.RemoteException;

/**
 * This is a map containing all freeway Segment objects.
 *
 * @author Douglas Lau
 */
class SegmentMapImpl extends AbstractListImpl implements SegmentMap {

	/** TreeMap to hold all the segments */
	protected final TreeMap<Integer, TMSObjectImpl> map;

	/** Create a new segment map */
	public SegmentMapImpl() throws RemoteException {
		super(false);
		map = new TreeMap<Integer, TMSObjectImpl>();
	}

	/** Get an iterator of the segments in the list */
	Iterator<TMSObjectImpl> iterator() {
		return map.values().iterator();
	}

	/** Add a segment into the map */
	public synchronized void add(Integer oid, SegmentImpl segment)
		throws TMSException
	{
		if(map.containsKey(oid))
			throw new ChangeVetoException("Duplicate OID: " + oid);
		map.put(oid, segment);
		Iterator<Integer> it = map.keySet().iterator();
		for(int i = 0; it.hasNext(); i++) {
			Integer search = it.next();
			if(oid.equals(search)) {
				notifyAdd(i, oid);
				return;
			}
		}
	}

	/** Remove a segment from the map */
	public synchronized void remove(Integer oid) throws TMSException {
		SegmentImpl segment = (SegmentImpl)map.get(oid);
		if(segment == null)
			throw new ChangeVetoException("Invalid OID: " + oid);
		Iterator<Integer> it = map.keySet().iterator();
		for(int i = 0; it.hasNext(); i++) {
			Integer search = it.next();
			if(oid.equals(search)) {
				it.remove();
				notifyRemove(i);
				return;
			}
		}
	}

	/** Get a single segment from its key */
	public synchronized Segment getElement(Integer oid) {
		if(oid == null)
			return null;
		return (Segment)map.get(oid);
	}

	/** Subscribe a listener to this list */
	public synchronized Object[] subscribe(RemoteList listener) {
		super.subscribe(listener);
		if(map.size() < 1)
			return null;
		Object[] list = new Object[map.size()];
		Iterator<TMSObjectImpl> it = iterator();
		for(int i = 0; it.hasNext(); i++)
			list[i] = it.next().getOID();
		return list;
	}

	/** Get the segment with the associated detector */
	public synchronized SegmentImpl getSegment(DetectorImpl det) {
		if(det == null)
			return null;
		Iterator<TMSObjectImpl> it = iterator();
		while(it.hasNext()) {
			SegmentImpl segment = (SegmentImpl)it.next();
			if(segment.hasDetector(det))
				return segment;
		}
		return null;
	}
}
