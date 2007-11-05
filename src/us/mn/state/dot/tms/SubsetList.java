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

import java.util.Iterator;
import java.rmi.RemoteException;

/**
 * SubsetList is a class for keeping track of certain subsets of other
 * lists, such as unassigned merge detectors, etc.
 *
 * @author Douglas Lau
 */
public class SubsetList extends SortedListImpl {

	/** Filter for this subset list */
	protected final SubsetFilter filter;

	/** Create the subset list */
	public SubsetList(SubsetFilter f) throws RemoteException {
		super();
		filter = f;
	}

	/** Don't use this method (stub for SortedList) */
	public TMSObject add(String key) {
		return null;
	}

	/** Add an element to the list */
	protected synchronized void add(String key, TMSObjectImpl obj) {
		if(map.get(key) == obj)
			return;
		map.put(key, obj);
		Iterator<String> it = map.keySet().iterator();
		for(int i = 0; it.hasNext(); i++) {
			String search = it.next();
			if(key.equals(search)) {
				notifyAdd(i, key);
				return;
			}
		}
	}

	/** Remove an element from the list */
	public synchronized void remove(String key) {
		TMSObjectImpl element = map.get(key);
		if(element == null)
			return;
		Iterator<String> it = map.keySet().iterator();
		for(int i = 0; it.hasNext(); i++) {
			String search = it.next();
			if(key.equals(search)) {
				it.remove();
				notifyRemove(i);
				return;
			}
		}
	}

	/** Add all elements of a given list which pass the filter */
	void addFiltered(AbstractListImpl list) {
		Iterator<TMSObjectImpl> it = list.iterator();
		while(it.hasNext()) {
			TMSObjectImpl obj = it.next();
			if(filter.allow(obj))
				add(obj.getKey(), obj);
		}
	}

	/** Update an object in the list (add or remove) */
	void update(TMSObjectImpl obj) {
		String key = obj.getKey();
		if(filter.allow(obj))
			add(key, obj);
		else
			remove(key);
	}
}
