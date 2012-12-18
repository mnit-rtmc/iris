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
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.TMSException;

/**
 * A system attribute is a name mapped to a string value.
 * @see SystemAttributeHelper, SystemAttribute
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SystemAttributeImpl extends BaseObjectImpl
	implements SystemAttribute
{
	/** System attribute debug log */
	static private final DebugLog SYS_LOG = new DebugLog("sys_attr");

	/** Load all */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, SystemAttributeImpl.class);
		store.query("SELECT name, value FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new SystemAttributeImpl(
					row.getString(1), // name
					row.getString(2)  // value
				));
			}
		});
		validateDatabaseVersion();
	}

	/** Validate the database version */
	static protected void validateDatabaseVersion() {
		String c_version = "@@VERSION@@";
		String db_version = SystemAttrEnum.DATABASE_VERSION.getString();
		if(!validateVersions(c_version, db_version)) {
			StringBuilder b = new StringBuilder();
			b.append("Failure: database_version (");
			b.append(db_version);
			b.append(") does not match the required value (");
			b.append(c_version);
			b.append(").  Shutting down.");
			System.err.println(b.toString());
			System.exit(1);
		}
	}

	/** Validate the database version */
	static protected boolean validateVersions(String v0, String v1) {
		String[] va0 = v0.split("\\.");
		String[] va1 = v1.split("\\.");
		// Versions must be "major.minor.micro"
		if(va0.length != 3 || va1.length != 3)
			return false;
		// Check that major versions match
		if(!va0[0].equals(va1[0]))
			return false;
		// Check that minor versions match
		if(!va0[1].equals(va1[1]))
			return false;
		// It's OK if micro versions don't match
		return true;
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("value", value);
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

	/** Create a new attribute */
	public SystemAttributeImpl(String n) {
		super(n);
	}

	/** Create a new attribute */
	protected SystemAttributeImpl(String att_name, String arg_value) {
		super(att_name);
		value = arg_value;
	}

	/** attribute value */
	protected String value = "";

	/** Set the attribute value */
	public void setValue(String arg_value) {
		logChange(arg_value);
		value = arg_value;
	}

	/** Log system attribute change. */
	private void logChange(String newval) {
		if(!value.equals(newval)) {
			SYS_LOG.log("System attribute changed: " + name +
				", old=" + value + ", new=" + newval);
		}
	}

	/** Set the attribute value, doSet is required for
	 *  database backed sonar objects
	 */
	public void doSetValue(String arg_value) throws TMSException {
		if(arg_value == null)
			return;
		if(value.equals(arg_value))
			return;
		store.update(this, "value", arg_value);
		setValue(arg_value);
	}

	/** Get the attribute value */
	public String getValue() {
		return value;
	}
}
