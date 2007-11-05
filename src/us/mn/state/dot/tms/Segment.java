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

/**
 * A segment represents the geometry and device location of a piece of freeway.
 *
 * @author Douglas Lau
 */
public interface Segment extends TMSObject {

	/** Is this a left-side segment? */
	public boolean isLeft() throws RemoteException;

	/** Get the change in the number of mainline lanes */
	public int getDelta() throws RemoteException;

	/** Get the change in the number of collector-distributor lanes */
	public int getCdDelta() throws RemoteException;

	/** Get the miles downstream of reference point */
	public Float getMile() throws RemoteException;

	/** Set the miles downstream of reference point */
	public void setMile(Float m) throws TMSException, RemoteException;

	/** Get the segment location */
	public Location getLocation() throws RemoteException;

	/** Get all detectors with a matching location */
	public Detector[] getMatchingDetectors() throws RemoteException;

	/** Set the array of segment detectors */
	public void setDetectors(Detector[] detectors) throws TMSException,
		RemoteException;

	/** Get the array of segment detectors */
	public Detector[] getDetectors() throws RemoteException;

	/** Validate the segment */
	public boolean validate(int lanes, int shift, int cd)
		throws RemoteException;

	/** Get the number of mainline lanes after this segment */
	public int getLanes() throws RemoteException;

	/** Get the yellow fog line (left side) reference shift */
	public int getShift() throws RemoteException;

	/** Get the number of collector-distributor lanes */
	public int getCd() throws RemoteException;

	/** Get the administrator notes */
	public String getNotes() throws RemoteException;

	/** Set the administrator notes */
	public void setNotes(String n) throws TMSException, RemoteException;
}
