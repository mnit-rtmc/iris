/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2024  Minnesota Department of Transportation
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
import java.sql.SQLException;
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
		store.query("SELECT name, uri, config, timeout_ms, enabled " +
			"FROM iris." + SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new ModemImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("uri", uri);
		map.put("config", config);
		map.put("timeout_ms", timeout_ms);
		map.put("enabled", enabled);
		return map;
	}

	/** Create a new modem */
	private ModemImpl(ResultSet row) throws SQLException {
		this(row.getString(1),		// name
		     row.getString(2),		// uri
		     row.getString(3),		// config
		     row.getInt(4),		// timeout_ms
		     row.getBoolean(5)		// enabled
		);
	}

	/** Create a new modem */
	private ModemImpl(String n, String u, String c, int t, boolean e) {
		super(n);
		uri = u;
		config = c;
		timeout_ms = t;
		enabled = e;
	}

	/** Create a new modem */
	public ModemImpl(String n) {
		super(n);
	}

	/** Remote URI for modem */
	private String uri = "";

	/** Set remote URI for modem */
	@Override
	public void setUri(String u) {
		uri = u;
	}

	/** Set remote URI for modem */
	public void doSetUri(String u) throws TMSException {
		if (!u.equals(uri)) {
			store.update(this, "uri", u);
			setUri(u);
		}
	}

	/** Get remote URI for modem */
	@Override
	public String getUri() {
		return uri;
	}

	/** Config string.  The default value sets the "Disconnect activity
	 * timer" (S30) to 10 seconds.  This has been tested with a StarComm
	 * modem -- who knows if it works for other brands. */
	private String config = "ATS30=1";

	/** Set config string */
	@Override
	public void setConfig(String c) {
		config = c;
	}

	/** Set config string */
	public void doSetConfig(String c) throws TMSException {
		if (!c.equals(config)) {
			store.update(this, "config", c);
			setConfig(c);
		}
	}

	/** Get config string */
	@Override
	public String getConfig() {
		return config;
	}

	/** Connect timeout (milliseconds) */
	private int timeout_ms = 30000;

	/** Set the connect timeout (milliseconds) */
	@Override
	public void setTimeoutMs(int t) {
		timeout_ms = t;
	}

	/** Set the connect timeout (milliseconds) */
	public void doSetTimeoutMs(int t) throws TMSException {
		if (t != timeout_ms) {
			store.update(this, "timeout_ms", t);
			setTimeoutMs(t);
		}
	}

	/** Get the connect timeout (milliseconds) */
	@Override
	public int getTimeoutMs() {
		return timeout_ms;
	}

	/** Modem enabled boolean */
	private boolean enabled = true;

	/** Set the modem enabled boolean */
	@Override
	public void setEnabled(boolean e) {
		enabled = e;
	}

	/** Set the modem enabled boolean */
	public void doSetEnabled(boolean e) throws TMSException {
		if (e != enabled) {
			store.update(this, "enabled", e);
			setEnabled(e);
		}
	}

	/** Get the modem enabled boolean */
	@Override
	public boolean getEnabled() {
		return enabled;
	}

	/** Current modem state */
	private transient ModemState state = ModemState.offline;

	/** Set the modem state */
	public void setStateNotify(ModemState ms) {
		if (ms != state) {
			state = ms;
			notifyAttribute("state");
		}
	}

	/** Get the modem state (ordinal of ModemState) */
	@Override
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
		synchronized (name) {
			if (owned || !enabled)
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
		synchronized (name) {
			owned = false;
		}
	}
}
