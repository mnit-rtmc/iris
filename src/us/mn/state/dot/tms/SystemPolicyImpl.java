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
import java.sql.ResultSet;
import java.util.HashMap;

/**
 * The system policy is a mapping of system-wide policy parameter names to
 * integer values.
 *
 * @author Douglas Lau
 */
public class SystemPolicyImpl extends TMSObjectImpl implements SystemPolicy {

	/** Connection to SQL database */
	protected final SQLConnection store;

	/** Mapping of system policy parameters to values */
	protected final HashMap<String, Integer> params =
		new HashMap<String, Integer>();

	/** Create a new system policy object */
	public SystemPolicyImpl(SQLConnection s) throws TMSException,
		RemoteException
	{
		store = s;
		store.query("SELECT * FROM system_policy;", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				String name = row.getString(1);
				int value = row.getInt(2);
				params.put(name, value);
			}
		});
	}

	/** Insert a new policy parameter */
	protected void insertParameter(String name, int value)
		throws TMSException
	{
		store.update("INSERT INTO system_policy " +
			"(name, value) VALUES " + "('" + name +
			"', '" + value + "');");
	}

	/** Update a system-wide policy parameter */
	protected void updateParameter(String name, int value)
		throws TMSException
	{
		store.update("UPDATE system_policy SET value = '" +
			value + "' WHERE name = '" + name + "';");
	}

	/** Set a new value of a system-wide policy parameter */
	public synchronized void setValue(String name, int value)
		throws TMSException
	{
		Integer val = params.get(name);
		if(val == null)
			insertParameter(name, value);
		else if(value != val)
			updateParameter(name, value);
		params.put(name, value);
	}

	/** Get the current value of a system-wide policy parameter */
	public synchronized int getValue(String name) {
		Integer val = params.get(name);
		if(val == null)
			return 0;
		else
			return val;
	}
}
