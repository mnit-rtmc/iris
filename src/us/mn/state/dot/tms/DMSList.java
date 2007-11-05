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

/**
 * DMSList is an interface which contains the methods for
 * remotely maintaining a list of dynamic message signs.
 *
 * @author Douglas Lau
 */
public interface DMSList extends SortedList {

	/** Set one of the ring radius values */
	public void setRingRadius( int ring, int radius ) throws TMSException,
		RemoteException;

	/** Get one of the ring radius values */
	public int getRingRadius( int ring ) throws RemoteException;

	/** Set the global sign page on time (tenths of a second) */
	public void setPageOnTime( int time ) throws TMSException,
		RemoteException;

	/** Get the global sign page on time (tenths of a second) */
	public int getPageOnTime() throws RemoteException;

	/** Set the global sign page off time (tenths of a second) */
	public void setPageOffTime( int time ) throws TMSException,
		RemoteException;

	/** Get the global sign page off time (tenths of a second) */
	public int getPageOffTime() throws RemoteException;

	/** Send an alert to all signs in the specified group */
	public void sendGroup(String group, String owner, String[] text)
		throws TMSException, RemoteException;

	/** Clear all signs in the specified group */
	public void clearGroup(String group, String owner)
		throws RemoteException;
}
