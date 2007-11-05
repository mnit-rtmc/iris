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
 * Roadway
 *
 * @author Douglas Lau
 */
public interface Roadway extends TMSObject {

	/** Get the name */
	String getName() throws RemoteException;

	/** Set the abbreviated name */
	void setAbbreviated(String abbreviated) throws TMSException,
		RemoteException;

	/** Get the abbreviated name */
	String getAbbreviated() throws RemoteException;

	/** Undefined roadway type / direction */
	short NONE = 0;

	/** Residential (A) roadway type */
	short RESIDENTIAL = 1;

	/** Business (B) roadway type */
	short BUSINESS = 2;

	/** Collector (C) roadway type */
	short COLLECTOR = 3;

	/** Arterial (D) roadway type */
	short ARTERIAL = 4;

	/** Expressway (E) roadway type */
	short EXPRESSWAY = 5;

	/** Freeway (F) roadway type */
	short FREEWAY = 6;

	/** Collector-Distributor roadway type */
	short CD_ROAD = 7;

	/** Roadway types */
	String[] TYPES = {
		" ", "Residential", "Business", "Collector", "Arterial",
		"Expressway", "Freeway", "CD Road"
	};

	/** Set the roadway type */
	void setType(short t) throws TMSException, RemoteException;

	/** Get the roadway type */
	short getType() throws RemoteException;

	/** North direction */
	short NORTH = 1;

	/** South direction */
	short SOUTH = 2;

	/** East direction */
	short EAST = 3;

	/** West direction */
	short WEST = 4;

	/** North-South direction */
	short NORTH_SOUTH = 5;

	/** East-West direction */
	short EAST_WEST = 6;

	/** Inner Loop direction */
	short INNER_LOOP = 7;

	/** Outer Loop direction */
	short OUTER_LOOP = 8;

	/** Set direction */
	void setDirection(short d) throws TMSException, RemoteException;

	/** Get direction */
	short getDirection() throws RemoteException;
}
