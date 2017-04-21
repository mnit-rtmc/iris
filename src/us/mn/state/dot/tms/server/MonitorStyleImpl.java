/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.MonitorStyle;
import us.mn.state.dot.tms.TMSException;

/**
 * Video monitor styles.
 *
 * @author Douglas Lau
 */
public class MonitorStyleImpl extends BaseObjectImpl implements MonitorStyle {

	/** Load all the monitor styles */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, MonitorStyleImpl.class);
		store.query("SELECT name, force_aspect, accent " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new MonitorStyleImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("force_aspect", force_aspect);
		map.put("accent", accent);
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

	/** Create a new encoder type */
	public MonitorStyleImpl(String n) {
		super(n);
		force_aspect = false;
		accent = DEFAULT_ACCENT;
	}

	/** Create a monitor style */
	private MonitorStyleImpl(ResultSet row) throws SQLException {
		this(row.getString(1),		// name
		     row.getBoolean(2),		// force_aspect
		     row.getString(3)		// accent
		);
	}

	/** Create a new monitor style */
	private MonitorStyleImpl(String n, boolean fa, String a) {
		this(n);
		force_aspect = fa;
		accent = a;
	}

	/** Force-aspect ratio flag */
	private boolean force_aspect;

	/** Set force-aspect ratio flag */
	@Override
	public void setForceAspect(boolean fa) {
		force_aspect = fa;
	}

	/** Set force-aspect ratio flag */
	public void doSetForceAspect(boolean fa) throws TMSException {
		if (fa == force_aspect)
			return;
		store.update(this, "force_aspect", fa);
		setForceAspect(fa);
	}

	/** Get force-aspect ratio flag */
	@Override
	public boolean getForceAspect() {
		return force_aspect;
	}

	/** Accent color */
	private String accent = "";

	/** Set the accent color (hex: RRGGBB) */
	@Override
	public void setAccent(String a) {
		accent = a;
	}

	/** Set the accent color (hex: RRGGBB) */
	public void doSetAccent(String a) throws TMSException {
		if (!a.equals(accent)) {
			store.update(this, "accent", a);
			setAccent(a);
		}
	}

	/** Get the accent color (hex: RRGGBB) */
	@Override
	public String getAccent() {
		return accent;
	}
}
