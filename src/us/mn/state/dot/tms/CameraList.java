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
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * List of CCTV cameras in the traffic management system
 *
 * @author Douglas Lau
 */
public class CameraList extends SortedListImpl {

	/** Create a new camera list */
	public CameraList() throws RemoteException {
		super();
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
		return camera;
	}

	/** Remove a camera from the list */
	public synchronized void remove(String key) throws TMSException {
		super.remove(key);
		deviceList.remove(key);
	}
}
