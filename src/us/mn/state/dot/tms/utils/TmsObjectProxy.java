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
package us.mn.state.dot.tms.utils;

import java.rmi.RemoteException;
import us.mn.state.dot.tms.RemoteObserver;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.TMSObject;

/**
 * Proxy for a TMSObject.  Delegates all method calls to remote object.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class TmsObjectProxy implements TMSObject {

	/** The object being proxied */
	private final TMSObject tmsObject;

	/** Create a new TMS object proxy */
	public TmsObjectProxy(TMSObject object) {
		tmsObject = object;
	}

	/** Add an observer to this object */
	public void addObserver(RemoteObserver o) throws RemoteException {
		tmsObject.addObserver(o);
	}

	/** Delete an observer from this object */
	public void deleteObserver(RemoteObserver o) throws RemoteException {
		tmsObject.deleteObserver(o);
	}

	/** Is this object deletable? */
	public boolean isDeletable() throws TMSException, RemoteException {
		return tmsObject.isDeletable();
	}

	/** Notify all observers of an update */
	public void notifyUpdate() throws RemoteException {
		tmsObject.notifyUpdate();
	}

	/** Get the object ID */
	public Integer getOID() throws RemoteException {
		return tmsObject.getOID();
	}

	/** Update the proxy status information */
	public void updateStatusInfo() throws RemoteException {}

	/** Update the proxy update information */
	public void updateUpdateInfo() throws RemoteException {}
}
