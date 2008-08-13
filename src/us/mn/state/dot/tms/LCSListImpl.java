/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2008  Minnesota Department of Transportation
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
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * List of LaneControlSignals in the TMS.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class LCSListImpl extends SortedListImpl implements LCSList {

	/** Available LCS list (not assigned to a controller) */
	protected transient SubsetList available;

	/** Get the list of available LCSs*/
	public SortedList getAvailableList() {
		return available;
	}

	/** Initialize the subset list */
	protected void initialize() throws RemoteException {
		available = new SubsetList(new DeviceFilter());
	}

	/**
	 * Create a new LCSListImpl
	 *
	 * @exception RemoteException  If there is an RMI exception
	 */
	public LCSListImpl() throws RemoteException {
		super();
		initialize();
	}

	/** Load the contents of the list from the ObjectVault */
	void load(Class c, String keyField) throws ObjectVaultException,
		TMSException, RemoteException
	{
		super.load(c, keyField);
		available.addFiltered(this);
	}

	/**
	 * Add an LCS to the list
	 *
	 * @param key                 Value to use as the key for this object
	 * @param lanes               Number of lanes
	 * @return                    Object that was added
	 * @exception TMSException    If there is a problem accessing
	 * the database
	 * @exception RemoteException  If there is an RMI error
	 */
	public synchronized TMSObject add( String key, int lanes )
		 throws TMSException, RemoteException
	{
		LaneControlSignalImpl lcs =
			(LaneControlSignalImpl) map.get(key);
		if(lcs != null)
			return lcs;
		lcs = new LaneControlSignalImpl( key, lanes );
		try {
			vault.save( lcs, getUserName() );
		} catch ( ObjectVaultException e ) {
			throw new TMSException( e );
		}
		map.put( key, lcs );
		Iterator iterator = map.keySet().iterator();
		for ( int index = 0; iterator.hasNext(); index++ ) {
			String search = ( String ) iterator.next();
			if ( key.equals( search ) ) {
				notifyAdd( index, key );
				break;
			}
		}
		available.add(key, lcs);
		return lcs;
	}

	/** This constructor cannot be used for LaneControlSignals */
	public TMSObject add( String key ) throws TMSException {
		throw new ChangeVetoException(
			"LCS constructor must include the number of lanes");
	}

	/** Update a device in the list */
	public TMSObject update(String key) {
		DeviceImpl device = (DeviceImpl)super.update(key);
		available.update(device);
		return device;
	}

	/**
	 * Remove an LCS from the list
	 *
	 * @param key              The key value of the object to be removed
	 * @exception RemoteException  If there is an RMI exception
	 */
	public synchronized void remove( String key ) throws TMSException {
		GeoLocImpl geo_loc = lookupDeviceLoc(key);
		super.remove( key );
		if(geo_loc != null)
			MainServer.server.removeObject(geo_loc);
		available.remove(key);
	}
}
