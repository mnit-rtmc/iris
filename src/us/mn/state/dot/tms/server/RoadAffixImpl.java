/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2019  Minnesota Department of Transportation
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

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.RoadAffix;
import us.mn.state.dot.tms.TMSException;

/**
 * A road affix is a prefix or suffix to a road name which can be replaced or
 * trimmed for traveler information display.
 *
 * @author Douglas Lau
 */
public class RoadAffixImpl extends BaseObjectImpl implements RoadAffix {

	/** Load all the road affixes */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, RoadAffixImpl.class);
		store.query("SELECT name, prefix, fixup, allow_retain " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new RoadAffixImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("prefix", prefix);
		map.put("fixup", fixup);
		map.put("allow_retain", allow_retain);
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

	/** Create a new road affix */
	public RoadAffixImpl(String n) {
		super(n);
	}

	/** Create a road affix */
	private RoadAffixImpl(String n, boolean p, String f, boolean r) {
		super(n);
		prefix = p;
		fixup = f;
		allow_retain = r;
	}

	/** Create a road affix */
	private RoadAffixImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getBoolean(2), // prefix
		     row.getString(3),  // fixup
		     row.getBoolean(4)  // allow_retain
		);
	}

	/** Flag to indicate prefix (true) or suffix (false) */
	private boolean prefix;

	/** Set flag to indicate prefix (true) or suffix (false) */
	@Override
	public void setPrefix(boolean p) {
		prefix = p;
	}

	/** Set flag to indicate prefix (true) or suffix (false) */
	public void doSetPrefix(boolean p) throws TMSException {
		if (p != prefix) {
			store.update(this, "prefix", p);
			setPrefix(p);
		}
	}

	/** Get flag to indicate prefix (true) or suffix (false) */
	@Override
	public boolean getPrefix() {
		return prefix;
	}

	/** Traveler information fixup */
	private String fixup;

	/** Set the traveler information fixup */
	@Override
	public void setFixup(String f) {
		fixup = f;
	}

	/** Set the traveler information fixup */
	public void doSetFixup(String f) throws TMSException {
		if (!objectEquals(f, fixup)) {
			if (f != null && f.equals(""))
				throw new ChangeVetoException("Invalid fixup");
			store.update(this, "fixup", f);
			setFixup(f);
		}
	}

	/** Get the traveler information fixup */
	@Override
	public String getFixup() {
		return fixup;
	}

	/** Flag to allow retaining the affix */
	private boolean allow_retain;

	/** Set flag to allow retaining the affix */
	@Override
	public void setAllowRetain(boolean r) {
		allow_retain = r;
	}

	/** Set flag to allow retaining the affix */
	public void doSetAllowRetain(boolean r) throws TMSException {
		if (r != allow_retain) {
			store.update(this, "allow_retain", r);
			setAllowRetain(r);
		}
	}

	/** Get flag to allow retaining the affix */
	@Override
	public boolean getAllowRetain() {
		return allow_retain;
	}
}
