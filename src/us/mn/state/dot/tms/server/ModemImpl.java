/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Modem;
import us.mn.state.dot.tms.TMSException;

/**
 * A Modem represents an old-skool analog modem.
 *
 * @author Douglas Lau
 */
public class ModemImpl extends BaseObjectImpl implements Modem {

	/** Load all the modems */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading modems...");
		namespace.registerType(SONAR_TYPE, ModemImpl.class);
		store.query("SELECT name, uri, config, timeout FROM iris." +
			SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new ModemImpl(
					row.getString(1),	// name
					row.getString(2),	// uri
					row.getString(3),	// config
					row.getInt(5)		// timeout
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("uri", uri);
		map.put("config", config);
		map.put("timeout", timeout);
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

	/** Create a new modem */
	public ModemImpl(String n) {
		super(n);
	}

	/** Create a new modem */
	public ModemImpl(String n, String u, String c, int t) {
		super(n);
		uri = u;
		config = c;
		timeout = t;
	}

	/** Remote URI for modem */
	protected String uri = "";

	/** Set remote URI for modem */
	public void setUri(String u) {
		uri = u;
	}

	/** Set remote URL for modem */
	public void doSetUri(String u) throws TMSException {
		if(u.equals(uri))
			return;
		store.update(this, "uri", u);
		setUri(u);
	}

	/** Get remote URI for modem */
	public String getUri() {
		return uri;
	}

	/** Config string */
	protected String config = "";

	/** Set config string */
	public void setConfig(String c) {
		config = c;
	}

	/** Set config string */
	public void doSetConfig(String c) throws TMSException {
		if(c.equals(config))
			return;
		store.update(this, "config", c);
		setConfig(c);
	}

	/** Get config string */
	public String getConfig() {
		return config;
	}

	/** Connect timeout (milliseconds) */
	protected int timeout = 30000;

	/** Set the connect timeout (milliseconds) */
	public void setTimeout(int t) {
		timeout = t;
	}

	/** Set the connect timeout (milliseconds) */
	public void doSetTimeout(int t) throws TMSException {
		if(t == timeout)
			return;
		store.update(this, "timeout", t);
		setTimeout(t);
	}

	/** Get the connect timeout (milliseconds) */
	public int getTimeout() {
		return timeout;
	}
}
