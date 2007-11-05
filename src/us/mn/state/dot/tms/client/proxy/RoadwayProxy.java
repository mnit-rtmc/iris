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
package us.mn.state.dot.tms.client.proxy;

import java.rmi.RemoteException;
import us.mn.state.dot.tms.Roadway;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.TmsObjectProxy;

/**
 * Creates a proxy for a Roadway object.  Commonly called method results are
 * cached otherwise method calls are delegated to the internal roadway.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class RoadwayProxy extends TmsObjectProxy implements Roadway {

	/** The roadway being proxied */
	private final Roadway roadway;

	private short direction;

	/** The cached name of the roadway */
	private String name;

	/** Create a new RoadwayProxy */
	public RoadwayProxy( Roadway roadway ) throws RemoteException {
		super( roadway );
		this.roadway = roadway;
		update();
	}

	/** Update the roadway fields */
	public void update() throws RemoteException {
		if ( roadway != null ) {
			direction = roadway.getDirection();
			name = roadway.getName();
		} else {
			direction = -1;
			name = "";
		}
	}

	/** Get the abbreviated name */
	public String getAbbreviated() throws RemoteException {
		return roadway.getAbbreviated();
	}

	/** Get direction */
	public short getDirection() {
		return direction;
	}

	/** Get the name */
	public String getName() {
		return name;
	}

	/** Get the roadway type */
	public short getType() throws RemoteException {
		return roadway.getType();
	}

	/** Set the abbreviated name */
	public void setAbbreviated(String str) throws TMSException,
		RemoteException
	{
		roadway.setAbbreviated( str );
	}

	/** Set direction */
	public void setDirection(short d) throws TMSException,
		RemoteException
	{
		roadway.setDirection(d);
	}

	/** Set the roadway type */
	public void setType(short param) throws TMSException, RemoteException {
		roadway.setType( param );
	}
}
