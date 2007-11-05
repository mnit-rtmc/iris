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

import java.rmi.RemoteException;
import java.util.List;

import us.mn.state.dot.vault.FieldMap;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * CameraImpl
 *
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class TourImpl extends TMSObjectImpl implements Tour {

	/** ObjectVault table name */
	static public final String tableName = "tour";

	/** The tour name */
	protected final String name;

	/** Flag for system tours */
	protected boolean system;

	/** The default dwell time */
	static public final int DEFAULT_DWELL = 6;

	/** The dwell time */
	protected int dwell;

	/** Create a new tour with the default dwell time */
	public TourImpl(String n) throws ChangeVetoException,
		RemoteException
	{
		super();
		validateText(n);
		name = n;
		dwell = DEFAULT_DWELL;
	}

	/** Create a tour from an ObjectVault field map */
	protected TourImpl(FieldMap fields) throws RemoteException {
		super();
		name = (String)fields.get("name");
	}

	/**
	 * Get the cameras in the tour.
	 * @return the cameras in the tour
	 */
	public List getCameras() {
		//FIXME
		return null;
	}

	/**
	 * Set the system flag.
	 * @param b the new system flag.
	 */
	public void setSystem(boolean s) throws TMSException {
		if(s == system)
			return;
		try {
			vault.update(this, "system", new Boolean(s),
				getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		system = s;
	}

	/**
	 * Is this tour a system tour?
	 * @return true if it is a system tour
	 */
	public boolean isSystem() {
		return system;
	}

	/**
	 * Add a camera to the tour.
	 * @param cam the camera to be added.
	 */
	public void addCamera(Camera cam) throws TMSException {
		//FIXME
	}

	/**
	 * Remove a camera from the tour.
	 * @param cam the camera to be removed.
	 */
	public void removeCamera(Camera cam) throws TMSException {
		//FIXME
	}

	/**
	 * Set the cameras in the tour.
	 * @param cameras the new array of cameras in the tour.
	 */
	public void setCameras(List cameras) throws TMSException {
		if(isSystem())
			throw new ChangeVetoException(
				"System tours are not editable.");
		else {
			//FIXME
		}
	}

	/**
	 * Get the dwell time for the tour.  The dwell time is the number
	 * of milliseconds before switching to the next camera in the tour.
	 * @return the dwell time.
	 */
	public int getDwellTime() {
		return dwell;
	}

	/**
	 * Set the dwell time for the tour.  The dwell time is the number of
	 * milliseconds to wait before switching to the next camera in the tour.
	 * @param dwell the new dwell time in milliseconds.
	 */
	public void setDwellTime(int d) throws TMSException {
		if(dwell == d)
			return;
		//FIXME
	}

	/** Get a String representation of this tour */
	public String toString() {
		return name;
	}

	/** Get the name */
	public String getName() {
		return name;
	}
}
