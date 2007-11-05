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
import java.util.List;

/**
 * This class represents a built in tour in the switcher.
 *
 * @author Erik Engstrom
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public interface Tour extends TMSObject {

	/** Get the name */
	public String getName() throws RemoteException;

	/**
	 * Get the cameras in the tour.
	 * @return the cameras in the tour
	 */
	public List getCameras() throws RemoteException;

	/**
	 * Set the system flag.
	 * @param b the new system flag.
	 */
	public void setSystem(boolean b) throws TMSException, RemoteException;

	/**
	 * Is this tour a system tour?
	 * @return true if it is a system tour
	 */
	public boolean isSystem() throws RemoteException;

	/**
	 * Add a camera to the tour.
	 * @param cam the camera to be added.
	 */
	public void addCamera(Camera cam) throws TMSException, RemoteException;

	/**
	 * Remove a camera from the tour.
	 * @param cam the camera to be removed.
	 */
	public void removeCamera(Camera cam) throws TMSException,
		RemoteException;

	/**
	 * Set the cameras in the tour.
	 * @param cameras the new array of cameras in the tour.
	 */
	public void setCameras(List cameras) throws TMSException,
		RemoteException;

	/**
	 * Get the dwell time for the tour.  The dwell time is the number
	 * of milliseconds before switching to the next camera in the tour.
	 * @return the dwell time.
	 */
	public int getDwellTime() throws RemoteException;

	/**
	 * Set the dwell time for the tour.  The dwell time is the number of
	 * milliseconds to wait before switching to the next camera in the tour.
	 * @param dwell the new dwell time in milliseconds.
	 */
	public void setDwellTime(int dwell) throws TMSException,
		RemoteException;
}
