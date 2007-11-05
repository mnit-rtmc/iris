/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2007  Minnesota Department of Transportation
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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;
import java.rmi.RemoteException;

/**
 * This is a list of StationSegment objects, sorted by Integer id.
 *
 * @author Douglas Lau
 */
class StationListImpl extends AbstractListImpl implements StationList {

	/** TreeMap to hold all the elements in the list */
	protected final TreeMap<Integer, TMSObjectImpl> map;

	/** Most recent time stamp of calculated data */
	protected transient Calendar stamp;

	/** Create a new station list */
	public StationListImpl() throws RemoteException {
		super(false);
		map = new TreeMap<Integer, TMSObjectImpl>();
		stamp = Calendar.getInstance();
	}

	/** Get an iterator of the stations in the list */
	Iterator<TMSObjectImpl> iterator() {
		return map.values().iterator();
	}

	/** Append a station to the list */
	public synchronized void add(Integer index, StationSegmentImpl station)
		throws TMSException
	{
		if(map.containsKey(index)) throw new ChangeVetoException(
			"Duplicate station index: " + index);
		map.put(index, station);
		Iterator<Integer> it = map.keySet().iterator();
		for(int i = 0; it.hasNext(); i++) {
			Integer search = it.next();
			if(index.equals(search)) {
				notifyAdd(i, station.toString());
				return;
			}
		}
	}

	/** Remove a station from the list */
	public synchronized void remove(Integer index) throws TMSException {
		StationSegmentImpl station = (StationSegmentImpl)map.get(index);
		if(station == null)
			throw new ChangeVetoException("Cannot find: " + index);
		Iterator<Integer> it = map.keySet().iterator();
		for(int i = 0; it.hasNext(); i++) {
			Integer search = it.next();
			if(index.equals(search)) {
				it.remove();
				notifyRemove(i);
				return;
			}
		}
	}

	/** Get a single element from its key */
	public synchronized StationSegment getElement(Integer index) {
		if(index == null)
			return null;
		else
			return (StationSegment)map.get(index);
	}

	/** Subscribe a listener to this list */
	public synchronized Object[] subscribe(RemoteList listener) {
		super.subscribe(listener);
		if(map.size() < 1) return null;
		String[] list = new String[map.size()];
		Iterator<TMSObjectImpl> it = iterator();
		for(int i = 0; it.hasNext(); i++)
			list[i] = it.next().toString();
		return list;
	}

	/** Get the station with the associated detector */
	public synchronized StationSegmentImpl getStation(DetectorImpl det) {
		if(det == null) return null;
		Iterator<TMSObjectImpl> it = iterator();
		while(it.hasNext()) {
			StationSegmentImpl s = (StationSegmentImpl)it.next();
			if(s.hasDetector(det)) return s;
		}
		return null;
	}

	/** Calculate the current data for all stations in the list */
	public synchronized void calculateData(Calendar s) {
		if(stamp.after(s)) {
			System.err.println("StationData OUT OF ORDER: " +
				stamp.getTime() + " > " + s.getTime());
			return;
		}
		stamp = s;
		Iterator<TMSObjectImpl> it = iterator();
		while(it.hasNext()) {
			StationSegmentImpl station =
				(StationSegmentImpl)it.next();
			station.calculateData();
		}
		notifyStatus();
	}
}
