/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2006  Minnesota Department of Transportation
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
import java.util.Iterator;

import us.mn.state.dot.vault.ObjectVaultException;

/**
 * List of camera tours in the switcher
 *
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class TourListImpl extends SortedListImpl {

	/** Create a new tour list */
	public TourListImpl() throws RemoteException {
		super();
	}

	/** Add a tour to the list */
	public synchronized TMSObject add( String key ) throws TMSException,
		RemoteException
	{
		TourImpl tour = (TourImpl)map.get( key );
		if( tour != null ) return tour;
		tour = new TourImpl(  key );
		try { vault.save( tour, getUserName() ); }
		catch( ObjectVaultException e ) {
			throw new TMSException( e );
		}
		map.put( key, tour );
		Iterator iterator = map.keySet().iterator();
		for( int index = 0; iterator.hasNext(); index++ ) {
			String search = (String)iterator.next();
			if( key.equals( search ) ) {
				notifyAdd( index, key );
				break;
			}
		}
		return tour;
	}
}
