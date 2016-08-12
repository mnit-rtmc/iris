/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2012  Minnesota Department of Transportation
 * Copyright (C) 2015  SRF Consulting Group
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.Modem;
import us.mn.state.dot.tms.ModemState;
import us.mn.state.dot.tms.TMSException;

/**
 * A Modem represents an old-skool analog modem.
 *
 * @author Douglas Lau
 */
public class ModemImpl extends BaseObjectImpl implements Modem {

	/** Load all the modems */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, ModemImpl.class);
		store.query("SELECT name, uri, config, timeout FROM iris." +
			SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new ModemImpl(
					row.getString(1),	// name
					row.getString(2),	// uri
					row.getString(3),	// config
					row.getInt(4)		// timeout
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

	/** Set remote URI for modem */
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

	/** Create the URI */
	public URI createURI() throws URISyntaxException {
		try {
			return new URI(uri);
		}
		catch(URISyntaxException e) {
			// If the URI begins with a host IP address,
			// we need to prepend a couple of slashes
			return new URI("//" + uri);
		}
	}

	/** Config string.  The default value sets the "Disconnect activity
	 * timer" (S30) to 10 seconds.  This has been tested with a StarComm
	 * modem -- who knows if it works for other brands.  The disconnect
	 * after timeout feature is necessary for proper operation, since IRIS
	 * will never tell the modem to hang up. */
	protected String config = "ATS30=1";

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

	/** Current modem state */
	private transient ModemState state = ModemState.offline;

	/** Set the modem state */
	public void setStateNotify(ModemState ms) {
		if(ms != state) {
			state = ms;
			notifyAttribute("state");
		}
	}

	/** Get the modem state (ordinal of ModemState) */
	public int getState() {
		return state.ordinal();
	}

	/** Modem ownership flag */
	private transient boolean owned;

	/** Check if the modem is currently owned */
	public boolean isOwned() {
		return owned;
	}
	
	/** Acquire ownership of the modem */
	public boolean acquire() {
		// Name used for unique acquire/release lock
		synchronized(name) {
			if(owned || !enabled)
				return false;
			else {
				owned = true;
				return true;
			}
		}
	}

	/** Release ownership of the modem */
	public void release() {
		// Name used for unique acquire/release lock
		synchronized(name) {
			owned = false;
		}
	}

	private transient boolean enabled = true;

	public void setEnabled(boolean b) {
		enabled = b;
	}

	public boolean getEnabled() {
		return enabled;
	}
}
