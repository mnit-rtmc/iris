/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.TMSException;

/**
 * A sign group is an arbitrary collection of dynamic message signs (DMS).
 *
 * @author Douglas Lau
 */
public class SignGroupImpl extends BaseObjectImpl implements SignGroup {

	/** Load all the sign groups */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, SignGroupImpl.class);
		store.query("SELECT name, local FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new SignGroupImpl(
					row.getString(1),	// name
					row.getBoolean(2)	// local
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("local", local);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
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
