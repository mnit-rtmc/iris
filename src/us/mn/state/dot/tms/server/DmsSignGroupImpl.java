/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.TMSException;

/**
 * A DMS sign group is a relation between dynamic message signs (DMS) and
 * sign groups.
 *
 * @author Douglas Lau
 */
public class DmsSignGroupImpl extends BaseObjectImpl implements DmsSignGroup {

	/** Load all the DMS sign groups */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, DmsSignGroupImpl.class);
		store.query("SELECT name, dms, sign_group" +
			" FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new DmsSignGroupImpl(
					row.getString(1),	// name
					row.getString(2),	// dms
					row.getString(3)	// sign_group
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("dms", dms);
		map.put("sign_group", sign_group);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new DMS sign group */
	public DmsSignGroupImpl(String n) {
		super(n);
	}

	/** Create a new DMS sign group */
	private DmsSignGroupImpl(String n, DMS d, SignGroup g) {
		super(n);
		dms = d;
		sign_group = g;
	}

	/** Create a new DMS sign group */
	private DmsSignGroupImpl(String n, String d, String g) {
		this(n, lookupDMS(d), lookupSignGroup(g));
	}

	/** DMS name */
	private DMS dms;

	/** Get the DMS ID */
	@Override
	public DMS getDms() {
		return dms;
	}

	/** Sign group */
	private SignGroup sign_group;

	/** Get the sign group name */
	@Override
	public SignGroup getSignGroup() {
		return sign_group;
	}
}
