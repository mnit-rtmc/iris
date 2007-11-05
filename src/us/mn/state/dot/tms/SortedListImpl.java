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
import java.util.LinkedList;
import java.util.TreeMap;
import java.rmi.RemoteException;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * This is a list of TMS objects, sorted by String id. It is the implementation
 * of the SortedList RMI interface.
 *
 * @author Douglas Lau
 */
abstract class SortedListImpl extends AbstractListImpl implements SortedList {

	/** TreeMap to hold all the elements in the list */
	protected final TreeMap<String, TMSObjectImpl> map;

	/** Create a new sorted list */
	public SortedListImpl() throws RemoteException {
		super(false);
		map = new TreeMap<String, TMSObjectImpl>();
	}

	/** Load the contents of the list from the ObjectVault */
	void load(Class c, String keyField) throws ObjectVaultException,
		TMSException, RemoteException
	{
		map.clear();
		Iterator it = vault.lookup(c, keyField);
		while(it.hasNext()) {
			TMSObjectImpl object =
				(TMSObjectImpl)vault.load(it.next());
			object.initTransients();
			map.put(object.getKey(), object);
		}
	}

	/** Get an iterator of the elements in the list */
	protected Iterator<TMSObjectImpl> iterator() {
		return map.values().iterator();
	}

	/** Get a thread-safe iterator over the list */
	protected synchronized Iterator<TMSObjectImpl> getIterator() {
		return new LinkedList<TMSObjectImpl>(map.values()).iterator();
	}

	/** Update an element in the list */
	public synchronized TMSObject update(String key) {
		TMSObject element = map.get(key);
		if(element == null)
			return null;
		Iterator<String> it = map.keySet().iterator();
		for(int i = 0; it.hasNext(); i++) {
			String search = it.next();
			if(key.equals(search)) {
				notifySet(i, element.toString());
				return element;
			}
		}
		System.err.println("Cannot update: " + key);
		return null;
	}

	/** Remove an element from the list */
	public synchronized void remove(String key) throws TMSException {
		TMSObjectImpl element = map.get(key);
		if(element == null)
			throw new ChangeVetoException("Cannot find: " + key);
		if(!element.isDeletable())
			throw new ChangeVetoException("Cannot delete object");
		try {
			vault.delete(element, getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		Iterator<String> it = map.keySet().iterator();
		for(int i = 0; it.hasNext(); i++) {
			String search = it.next();
			if(key.equals(search)) {
				it.remove();
				notifyRemove(i);
				element.notifyDelete();
				return;
			}
		}
	}

	/** Get a single element from its key */
	public synchronized final TMSObject getElement(String key) {
		if(key == null)
			return null;
		return map.get(key);
	}

	/** Subscribe a listener to this list */
	public synchronized final Object[] subscribe(RemoteList listener) {
		super.subscribe(listener);
		if(map.size() < 1)
			return null;
		String [] list = new String[map.size()];
		Iterator<String> it = map.keySet().iterator();
		for(int i = 0; it.hasNext(); i++)
			list[i] = it.next();
		return list;
	}
}
