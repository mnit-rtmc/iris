/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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
 * A station is a group of related detectors.
 *
 * @author Douglas Lau
 */
public interface Station extends TMSObject {

	/** Default speed limit */
	int DEFAULT_SPEED_LIMIT = 55;

	/** Minimum freeway speed limit */
	int MINIMUM_SPEED_LIMIT = 45;

	/** Maximum freeway speed limit */
	int MAXIMUM_SPEED_LIMIT = 75;

	/** Get the station label */
	public String getLabel() throws RemoteException;

	/** Is this station active? */
	public boolean isActive() throws RemoteException;

	/** Get the average station flow */
	public int getFlow() throws RemoteException;

	/** Get the average station speed */
	public int getSpeed() throws RemoteException;
}
