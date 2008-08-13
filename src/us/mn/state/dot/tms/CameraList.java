/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
 * List of CCTV cameras in the traffic management system
 *
 * @author Douglas Lau
 */
public class CameraList extends SortedListImpl implements DeviceList {

	/** Available camera list (not assigned to a controller) */
	protected transient SubsetList available;

	/** Get the list of available cameras */
	public SortedList getAvailableList() {
		return available;
	}

	/** Initialize the subset list */
	protected void initialize() throws RemoteException {
		available = new SubsetList(new DeviceFilter());
	}

	/** Create a new camera list */
	public CameraList() throws RemoteException {
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

	/** Add a camera to the list */
	public synchronized TMSObject add( String key ) throws TMSException,
		RemoteException
	{
		CameraImpl camera = (CameraImpl)map.get( key );
		if( camera != null ) return camera;
		camera = new CameraImpl( key );
		try { vault.save( camera, getUserName() ); }
		catch( ObjectVaultException e ) {
			throw new TMSException( e );
		}
		map.put( key, camera );
		Iterator iterator = map.keySet().iterator();
		for( int index = 0; iterator.hasNext(); index++ ) {
			String search = (String)iterator.next();
			if( key.equals( search ) ) {
				notifyAdd( index, key );
				break;
			}
		}
		available.add(key, camera);
		return camera;
	}

	/** Update a device in the list */
	public TMSObject update(String key) {
		DeviceImpl device = (DeviceImpl)super.update(key);
		available.update(device);
		return device;
	}

	/** Remove a camera from the list */
	public synchronized void remove(String key) throws TMSException {
		GeoLocImpl geo_loc = lookupDeviceLoc(key);
		super.remove(key);
		if(geo_loc != null)
			MainServer.server.removeObject(geo_loc);
		deviceList.remove(key);
		available.remove(key);
	}
}
