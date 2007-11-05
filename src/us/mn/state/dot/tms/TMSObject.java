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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * TMSObject
 *
 * @author Douglas Lau
 */
public interface TMSObject extends Remote {

	/** Cross street direction strings to use for detector names */
	public String[] DIRECTION =
		{ " ", "NB", "SB", "EB", "WB" };

	/** Freeway direction strings to use for detector names */
	public String[] DIR_FREEWAY = {
		"", "N", "S", "E", "W", "N-S", "E-W", "IN", "OUT"
	};

	/** Freeway direction strings (long) */
	public String[] DIR_LONG = {
		" ", "Northbound", "Southbound", "Eastbound", "Westbound",
		"North-South", "East-West", "Inner Loop", "Outer Loop"
	};

	/** Short modifiers (for detector names) */
	public String[] MOD_SHORT = {
		"", "N", "S", "E", "W", "Nj", "Sj", "Ej", "Wj"
	};

	/** Cross street modifier strings to use for locations */
	public String[] MODIFIER = {
		"@",
		"N of", "S of", "E of", "W of",
		"N Jct", "S Jct", "E Jct", "W Jct"
	};

	/** Is this object deletable? */
	public boolean isDeletable() throws TMSException, RemoteException;

	/** Add an observer to this object */
	public void addObserver( RemoteObserver o ) throws RemoteException;

	/** Delete an observer from this object */
	public void deleteObserver( RemoteObserver o ) throws RemoteException;

	/** Notify all observers of an update */
	public void notifyUpdate() throws RemoteException;

	/** Get the object ID */
	public Integer getOID() throws RemoteException;
}
