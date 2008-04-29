/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2008  Minnesota Department of Transportation
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
 * A Location contains attributes necessary to describe a map location.
 *
 * @author Douglas Lau
 */
public interface Location extends TMSObject {

	/** Get a description of the location */
	public String getDescription() throws RemoteException;

	/** Get the freeway corridor */
	public String getCorridor() throws RemoteException;

	/** Get a description of the cross-street location */
	public String getCrossDescription() throws RemoteException;

	/** Set the freeway name */
	public void setFreeway(String name) throws TMSException,
		RemoteException;

	/** Get the freeway name */
	public String getFreeway() throws RemoteException;

	/** Set the freeway direction */
	public void setFreeDir(short d) throws TMSException, RemoteException;

	/** Get the freeway direction */
	public short getFreeDir() throws RemoteException;

	/** Set the cross-street name */
	public void setCrossStreet(String name) throws TMSException,
		RemoteException;

	/** Get the cross-street name */
	public String getCrossStreet() throws RemoteException;

	/** Set the cross street direction */
	public void setCrossDir(short d) throws TMSException, RemoteException;

	/** Get the cross street direction */
	public short getCrossDir() throws RemoteException;

	/** Set the cross street modifier */
	public void setCrossMod(short m) throws TMSException, RemoteException;

	/** Get the cross street modifier */
	public short getCrossMod() throws RemoteException;

	/** Set the UTM Easting */
	public void setEasting(int x) throws TMSException, RemoteException;

	/** Get the UTM Easting */
	public int getEasting() throws RemoteException;

	/** Set the UTM Easting offset */
	public void setEastOffset(int x) throws TMSException, RemoteException;

	/** Get the UTM Easting offset */
	public int getEastOffset() throws RemoteException;

	/** Set the UTM Northing */
	public void setNorthing(int y) throws TMSException, RemoteException;

	/** Get the UTM Northing */
	public int getNorthing() throws RemoteException;

	/** Set the UTM Northing offset */
	public void setNorthOffset(int y) throws TMSException, RemoteException;

	/** Get the UTM Northing offset */
	public int getNorthOffset() throws RemoteException;
}
