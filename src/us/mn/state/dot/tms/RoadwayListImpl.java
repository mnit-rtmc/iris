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

import java.io.IOException;
import java.util.Iterator;
import java.rmi.RemoteException;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * RoadwayListImpl is the implementation of the RoadwayList RMI
 * interface. It maintains a list of all roadways plus a list of roadways
 * which are classified as freeways.
 *
 * @author Douglas Lau
 */
class RoadwayListImpl extends SortedListImpl implements RoadwayList {

	/** Subset list of freeways only */
	protected transient SubsetList freeways;

	/** Get the freeway list */
	public SortedList getFreewayList() { return freeways; }

	/** Initialize the subset list */
	protected void initialize() throws RemoteException {
		freeways = new SubsetList( new SubsetFilter() {
			public boolean allow( TMSObjectImpl obj ) {
				RoadwayImpl road = (RoadwayImpl)obj;
				return road.isFreeway();
			}
		} );
	}

	/** Create a new roadway list */
	public RoadwayListImpl() throws RemoteException {
		super();
		initialize();
	}

	/** Load the contents of the list from the ObjectVault */
	void load(Class c, String keyField) throws ObjectVaultException,
		TMSException, RemoteException
	{
		super.load(c, keyField);
		freeways.addFiltered(this);
	}

	/** Add a roadway to the list */
	public synchronized TMSObject add( String key ) throws TMSException,
		RemoteException
	{
		RoadwayImpl road = (RoadwayImpl)map.get( key );
		if( road != null ) return road;
		road = new RoadwayImpl( key );
		try { vault.save( road, getUserName() ); }
		catch( ObjectVaultException e ) {
			throw new TMSException( e );
		}
		map.put( key, road );
		Iterator iterator = map.keySet().iterator();
		for( int index = 0; iterator.hasNext(); index++ ) {
			String search = (String)iterator.next();
			if( key.equals( search ) ) {
				notifyAdd( index, key );
				break;
			}
		}
		return road;
	}

	/** Update a roadway in the list */
	public synchronized TMSObject update( String key ) {
		RoadwayImpl roadway = (RoadwayImpl)super.update( key );
		if( roadway == null ) return null;
		if(roadway.isFreeway())
			freeways.add( key, roadway );
		else freeways.remove( key );
		return roadway;
	}

	/** Remove a roadway from the list */
	public synchronized void remove( String key ) throws TMSException {
		super.remove( key );
		freeways.remove( key );
	}
}
