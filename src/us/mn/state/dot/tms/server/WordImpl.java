/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Iteris Inc.
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
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.Word;

/**
 * A word is a character string.
 * @author Michael Darter
 */
public class WordImpl extends BaseObjectImpl implements Word {

	/** Load all words */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, WordImpl.class);
		store.query("SELECT name, abbr, allowed FROM " +
			"iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new WordImpl(
					row.getString(1),	// name
					row.getString(2),	// abbr
					row.getBoolean(3)	// allowed
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("abbr", abbr);
		map.put("allowed", allowed);
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

	/** Create a word (by SONAR clients) */
	public WordImpl(String n) {
		super(n);
	}

	/** Create a word */
	protected WordImpl(String n, String ab, boolean al) {
		super(n);
		abbr = ab;
		allowed = al;
	}

	/** Abbreviation */
	private String abbr;

	/** Get abbreviation */
	public String getAbbr() {
		return abbr;
	}

	/** Set abbreviation */
	public void setAbbr(String a) {
		abbr = a;
	}

	/** Set abbreviation */
	public void doSetAbbr(String a) throws TMSException {
		if (a != abbr) {
			store.update(this, "abbr", a);
			setAbbr(a);
		}
	}

	/** Allowed or banned */
	private boolean allowed;

	/** Get word type: allowed or banned */
	public boolean getAllowed() {
		return allowed;
	}

	/** Set word type: allowed or banned */
	public void setAllowed(boolean a) {
		allowed = a;
	}

	/** Set the allow state
	 * @param a Allow or disallow. */
	public void doSetAllowed(boolean a) throws TMSException {
		if(a != allowed) {
			store.update(this, "allowed", a);
			setAllowed(a);
		}
	}
}
