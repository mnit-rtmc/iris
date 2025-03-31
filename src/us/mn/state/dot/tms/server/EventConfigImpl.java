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
import us.mn.state.dot.tms.EventConfig;
import us.mn.state.dot.tms.TMSException;

/**
 * Event type configuration.
 *
 * @author Douglas Lau
 */
public class EventConfigImpl extends BaseObjectImpl implements EventConfig {

	/** Load all the event configs */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, enable_store, enable_purge, " +
			"purge_days FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new EventConfigImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("enable_store", enable_store);
		map.put("enable_purge", enable_purge);
		map.put("purge_days", purge_days);
		return map;
	}

	/** Create an event config */
	private EventConfigImpl(ResultSet row) throws SQLException {
		this(row.getString(1),   // name
		     row.getBoolean(2),  // enable_store
		     row.getBoolean(3),  // enable_purge
		     row.getInt(4)       // purge_days
		);
	}

	/** Create an event config */
	private EventConfigImpl(String n, boolean es, boolean ep,  int pd) {
		super(n);
		enable_store = es;
		enable_purge = ep;
		purge_days = pd;
	}

	/** Enable store flag */
	private boolean enable_store;

	/** Set enable store flag */
	@Override
	public void setEnableStore(boolean es) {
		enable_store = es;
	}

	/** Set enable store flag */
	public void doSetEnableStore(boolean es) throws TMSException {
		if (es != enable_store) {
			store.update(this, "enable_store", es);
			setEnableStore(es);
		}
	}

	/** Get enable store flag */
	@Override
	public boolean getEnableStore() {
		return enable_store;
	}

	/** Enable purge flag */
	private boolean enable_purge;

	/** Set enable purge flag */
	@Override
	public void setEnablePurge(boolean ep) {
		enable_purge = ep;
	}

	/** Set enable purge flag */
	public void doSetEnablePurge(boolean ep) throws TMSException {
		if (ep != enable_purge) {
			store.update(this, "enable_purge", ep);
			setEnablePurge(ep);
		}
	}

	/** Get enable purge flag */
	@Override
	public boolean getEnablePurge() {
		return enable_purge;
	}

	/** Number of days to keep events before purging */
	private int purge_days;

	/** Set the number of days to keep events before purging */
	@Override
	public void setPurgeDays(int pd) {
		purge_days = pd;
	}

	/** Set the number of days to keep events before purging */
	public void doSetPurgeDays(int pd) throws TMSException {
		if (pd != purge_days) {
			store.update(this, "purge_days", pd);
			setPurgeDays(pd);
		}
	}

	/** Get the number of days to keep events before purging */
	@Override
	public int getPurgeDays() {
		return purge_days;
	}

	/** Purge old records */
	public void purgeRecords() throws TMSException {
		if (getEnablePurge()) {
			int days = getPurgeDays();
			if ("cap_alert".equals(name))
				CapAlert.purgeRecords(days);
			else {
				store.update(
					"DELETE FROM event." + getName() +
					" WHERE event_date < now() - '" + days +
					" days'::interval;"
				);
			}
		}
	}
}
