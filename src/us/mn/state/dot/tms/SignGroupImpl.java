/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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

import java.sql.ResultSet;

/**
 * A sign group is an arbitrary collection of dynamic message signs (DMS).
 *
 * @author Douglas Lau
 */
public class SignGroupImpl extends BaseObjectImpl implements SignGroup {

	/** Load all the sign groups */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading sign groups...");
		namespace.registerType(SONAR_TYPE, SignGroupImpl.class);
		store.query("SELECT name, local FROM sign_group;",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.add(new SignGroupImpl(
					row.getString(1),	// name
					row.getBoolean(2)	// local
				));
			}
		});
	}

	/** Store a group */
	public void doStore() throws TMSException {
		store.update("INSERT INTO " + getTable() + " (name, local) " +
			"VALUES ('" + name + "', '" + local + "');");
	}

	/** Get the database table name */
	public String getTable() {
		return SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new sign group */
	public SignGroupImpl(String n) {
		this(n, false);
	}

	/** Create a new sign group */
	public SignGroupImpl(String n, boolean l) {
		super(n);
		local = l;
	}

	/** Flag indicating local sign group */
	protected boolean local;

	/** Is the group local to one sign? */
	public boolean getLocal() {
		return local;
	}
}
