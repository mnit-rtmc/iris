/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.IncidentDetail;
import us.mn.state.dot.tms.TMSException;

/**
 * Incident detail provide more information about an incident.  This is used
 * to log information such as "debris" or "grass fire".
 *
 * @author Douglas Lau
 */
public class IncidentDetailImpl extends BaseObjectImpl
	implements IncidentDetail
{
	/** Load all the incident details */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, IncidentDetailImpl.class);
		store.query("SELECT name, description FROM event." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new IncidentDetailImpl(
					row.getString(1),	// name
					row.getString(2)	// description
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("description", description);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "event." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new incident detail */
	public IncidentDetailImpl(String n) {
		super(n);
		description = "";
	}

	/** Create a new incident detail */
	protected IncidentDetailImpl(String n, String d) {
		this(n);
		description = d;
	}

	/** Description */
	protected String description;

	/** Set the description */
	public void setDescription(String d) {
		description = d;
	}

	/** Set the description */
	public void doSetDescription(String d) throws TMSException {
		if(d.equals(description))
			return;
		store.update(this, "description", d);
		setDescription(d);
	}

	/** Get the description */
	public String getDescription() {
		return description;
	}
}
