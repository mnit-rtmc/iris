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
import us.mn.state.dot.vault.FieldMap;

/**
 * MonitorImpl
 *
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class MonitorImpl extends TMSObjectImpl implements Monitor {

	/** ObjectVault table name */
	static public final String tableName = "monitor";

	/** The monitor name */
	protected final String name;

	/** The monitor description */
	protected String description;

	/** Create a new monitor */
	public MonitorImpl(String n) throws ChangeVetoException, RemoteException
	{
		super();
		validateText(n);
		name = n;
	}

	/** Create a monitor from an ObjectVault field map */
	protected MonitorImpl( FieldMap fields ) throws RemoteException {
		super();
		name = (String)fields.get( "name" );
	}

	/** Get the integer id of the monitor */
	public int getUID() {
		try {
			return Integer.parseInt(name.substring(1));
		}
		catch(NumberFormatException e) {
			return 0;
		}
	}

	/** Get the monitor ID */
	public String getId() throws RemoteException {
		return name;
	}
}
