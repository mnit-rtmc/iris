/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.AccessLevel;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.Permission;
import us.mn.state.dot.tms.Role;
import us.mn.state.dot.tms.TMSException;

/**
 * Role access permission.
 *
 * @author Douglas lau
 */
public class PermissionImpl extends BaseObjectImpl implements Permission,
	Comparable<PermissionImpl>
{
	/** Load all the permissions */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, role, base_resource, hashtag, " +
			"access_level FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new PermissionImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("role", role);
		map.put("base_resource", base_resource);
		map.put("hashtag", hashtag);
		map.put("access_level", access_level.ordinal());
		return map;
	}

	/** Create a new permission */
	public PermissionImpl(String n) {
		super(n);
	}

	/** Create a permission from a database row */
	private PermissionImpl(ResultSet row) throws SQLException {
		this(row.getString(1), // name
		     row.getString(2), // role
		     row.getString(3), // base_resource
		     row.getString(4), // hashtag
		     row.getInt(5)     // access_level
		);
	}

	/** Create a permission from database lookup */
	private PermissionImpl(String n, String r, String br, String h, int al) {
		this(n);
		role = lookupRole(r);
		base_resource = br;
		hashtag = h;
		access_level = AccessLevel.fromOrdinal(al);
	}

	/** Compare to another permission */
	@Override
	public int compareTo(PermissionImpl o) {
		return name.compareTo(o.name);
	}

	/** Test if the permission equals another permission */
	@Override
	public boolean equals(Object o) {
		if (o instanceof PermissionImpl) {
			PermissionImpl other = (PermissionImpl) o;
			return name.equals(other.name);
		} else
			return false;
	}

	/** User role */
	private Role role;

	/** Get the role */
	@Override
	public Role getRole() {
		return role;
	}

	/** Base resource */
	private String base_resource;

	/** Get the base resource */
	@Override
	public String getBaseResource() {
		return base_resource;
	}

	/** Hashtag */
	private String hashtag;

	/** Set the hashtag */
	@Override
	public void setHashtag(String ht) {
		hashtag = ht;
	}

	/** Set the hashtag */
	public void doSetHashtag(String ht) throws TMSException {
		String t = Hashtags.normalize(ht);
		if (!objectEquals(t, ht))
			throw new ChangeVetoException("Bad hashtag");
		if (!objectEquals(ht, hashtag)) {
			store.update(this, "hashtag", ht);
			setHashtag(ht);
		}
	}

	/** Get the hashtag */
	@Override
	public String getHashtag() {
		return hashtag;
	}

	/** Access level */
	private AccessLevel access_level = AccessLevel.VIEW;

	/** Set the access level */
	@Override
	public void setAccessLevel(int al) {
		access_level = AccessLevel.fromOrdinal(al);
	}

	/** Set the access level */
	public void doSetAccessLevel(int al) throws TMSException {
		AccessLevel a = AccessLevel.fromOrdinal(al);
		if (a != access_level) {
			store.update(this, "access_level", al);
			setAccessLevel(al);
		}
	}

	/** Get the access level */
	@Override
	public int getAccessLevel() {
		return access_level.ordinal();
	}
}
