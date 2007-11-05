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

import java.util.ArrayList;
import java.util.Iterator;
import java.rmi.RemoteException;
import us.mn.state.dot.vault.FieldMap;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * IndexedListImpl is a collection which stores an indexed list of remote
 * objects, such as Detectors, Stations, Communication Lines, etc.
 *
 * @author Douglas Lau
 */
abstract public class IndexedListImpl extends AbstractListImpl
	implements IndexedList
{
	/** ObjectVault table name */
	static public final String tableName = "indexed_list";

	/** ArrayList to hold all the elements in the list */
	protected final ArrayList<TMSObjectImpl> list;

	/** Create a new indexed list */
	public IndexedListImpl(boolean s) throws RemoteException {
		super(s);
		list = new ArrayList<TMSObjectImpl>(100);
	}

	/** Create an indexed list from an ObjectVault field map */
	protected IndexedListImpl( FieldMap fields ) throws RemoteException {
		super( true );
		final ArrayList l = (ArrayList)fields.get("list");
		list = new ArrayList<TMSObjectImpl>(l.size());
		for(Object o: l)
			list.add((TMSObjectImpl)o);
	}

	/** Load the contents of the list from the ObjectVault */
	void load(Class c, String keyField) throws ObjectVaultException,
		TMSException, RemoteException
	{
		if(stored) return;
		list.clear();
		Iterator it = vault.lookup(c, keyField);
		while(it.hasNext()) {
			TMSObjectImpl object =
				(TMSObjectImpl)vault.load(it.next());
			object.initTransients();
			list.add(object);
		}
	}

	/** Get an iterator of the elements in the list (for SubsetList) */
	Iterator<TMSObjectImpl> iterator() {
		return list.iterator();
	}

	/** Update an element in the list */
	public synchronized TMSObject update( int index ) {
		index--;
		TMSObject element = list.get(index);
		notifySet( index, element.toString() );
		return element;
	}

	/** Remove the last element from the list */
	public synchronized void removeLast() throws TMSException {
		if( list.isEmpty() ) throw new
			ChangeVetoException( "List is empty" );
		int count = list.size() - 1;
		TMSObjectImpl element = list.get(count);
		if( !element.isDeletable() ) throw new
			ChangeVetoException( "Cannot delete object" );
		try {
			if( stored ) vault.remove( list, count,
				getUserName() );
			else vault.delete( element, getUserName() );
		}
		catch( ObjectVaultException e ) {
			throw new TMSException( e );
		}
		list.remove( count );
		notifyRemove( count );
		element.notifyDelete();
	}

	/** Get an element by index */
	public synchronized TMSObject getElement( int index ) {
		return list.get(index - 1);
	}

	/** Subscribe a listener to this list */
	public synchronized Object[] subscribe( RemoteList listener ) {
		super.subscribe( listener );
		int count = list.size();
		if( count < 1 ) return null;
		String[] names = new String [ count ];
		for( int i = 0; i < count; i++ )
			names[ i ] = list.get( i ).toString();
		return names;
	}

	/** Get the number of items in the list */
	public synchronized final int size() {
		return list.size();
	}
}
