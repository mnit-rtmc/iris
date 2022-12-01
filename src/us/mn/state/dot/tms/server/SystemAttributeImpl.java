/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2022  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.SystemAttrEnum;
import static us.mn.state.dot.tms.SystemAttrEnum.DETECTOR_AUTO_FAIL_ENABLE;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.DevelCfg;

/**
 * A system attribute is a name mapped to a string value.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @see us.mn.state.dot.tms.SystemAttribute
 * @see us.mn.state.dot.tms.SystemAttributeHelper
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
	static private void validateDatabaseVersion() {
		String v_code = DevelCfg.get("db.version", "@@VERSION@@");
		String v_db = SystemAttrEnum.DATABASE_VERSION.getString();
		if (!validateVersions(v_code, v_db)) {
			StringBuilder b = new StringBuilder();
			b.append("Failure: database_version (");
			b.append(v_db);
			b.append(") does not match the required value (");
			b.append(v_code);
			b.append(").  Shutting down.");
			System.err.println(b.toString());
			// Sleep 30 seconds to avoid spamming the log file
			TimeSteward.sleep_well(30000);
			System.exit(1);
		}
	}

	/** Validate the database version */
	static private boolean validateVersions(String v_code, String v_db) {
		if (v_code != null && v_db != null) {
			String[] code = v_code.split("\\.");
			String[] db = v_db.split("\\.");
			// Check that major and minor versions are the same
			return code.length >= 2 &&
			       db.length >= 2 &&
			       code[0].equals(db[0]) &&
			       code[1].equals(db[1]);
		} else
			return false;
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("value", value);
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

	/** Create a new attribute */
	public SystemAttributeImpl(String n) {
		super(n);
	}

	/** Create a new attribute */
	private SystemAttributeImpl(String att_name, String arg_value) {
		super(att_name);
		value = arg_value;
	}

	/** Log system attribute change. */
	private void logChange(String newval) {
		if (!value.equals(newval)) {
			SYS_LOG.log("System attribute changed: " + name +
				", old=" + value + ", new=" + newval);
		}
	}

	/** Attribute value */
	private String value = "";

	/** Set the attribute value */
	@Override
	public void setValue(String arg_value) {
		logChange(arg_value);
		value = arg_value;
		if (DETECTOR_AUTO_FAIL_ENABLE.aname().equals(name) &&
		   !DETECTOR_AUTO_FAIL_ENABLE.getBoolean())
		{
			DetectorImpl.updateAutoFailAll();
		}
	}

	/** Set the attribute value, doSet is required for
	 *  database backed sonar objects
	 */
	public void doSetValue(String arg_value) throws TMSException {
		if (!objectEquals(arg_value, value)) {
			store.update(this, "value", arg_value);
			setValue(arg_value);
		}
	}

	/** Get the attribute value */
	@Override
	public String getValue() {
		return value;
	}
}
