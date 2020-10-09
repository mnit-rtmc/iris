/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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

import us.mn.state.dot.tms.CapResponseType;
import us.mn.state.dot.tms.TMSException;

/**
 * Common Alerting Protocol (CAP) response type field substitution value
 * server-side implementation. Used for IPAWS alert processing for generating
 * messages for posting to DMS.
 *
 * @author Gordon Parikh
 */
public class CapResponseTypeImpl extends BaseObjectImpl
	implements CapResponseType {

	/** Database table name */
	static private final String TABLE = "iris.cap_response_type";
	
	public CapResponseTypeImpl(String n) {
		super(n);
	}

	public CapResponseTypeImpl(String n, String ev, String rt, String m) {
		super(n);
		event = ev;
		response_type = rt;
		multi = m;
	}

	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}
	
	/** Load all the CAP response type substitution values */
	static public void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, CapResponseTypeImpl.class);
		store.query("SELECT name, event, response_type, multi FROM iris." +
				SONAR_TYPE + ";", new ResultFactory()
		{
			@Override
			public void create(ResultSet row) throws Exception {
				try {
					namespace.addObject(new CapResponseTypeImpl(row));
				} catch (Exception e) {
					System.out.println("Error adding: " + row.getString(1));
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("event", event);
		map.put("response_type", response_type);
		map.put("multi", multi);
		return map;
	}

	private CapResponseTypeImpl(ResultSet row) throws SQLException {
		this(row.getString(1),			// name
			row.getString(2),			// event
			row.getString(3),			// response type
			row.getString(4));			// MULTI
	}
	
	/** Applicable alert event type */
	private String event;
	
	/** Set the applicable alert event type */
	@Override
	public void setEvent(String ev) {
		event = ev;
	}

	/** Set the applicable alert event type */
	public void doSetEvent(String ev) throws TMSException {
		if (ev != event) {
			store.update(this, "event", ev);
			setEvent(ev);
		}
	}
	
	/** Get the applicable alert event type */
	@Override
	public String getEvent() {
		return event;
	}

	/** Applicable response type */
	private String response_type;
	
	/** Set the applicable response type */
	@Override
	public void setResponseType(String rt) {
		response_type = rt;
	}

	/** Set the applicable response type */
	public void doSetResponseType(String rt) throws TMSException {
		if (rt != response_type) {
			store.update(this, "response_type", rt);
			setResponseType(rt);
		}
	}
	
	/** Get the applicable response type */
	@Override
	public String getResponseType() {
		return response_type;
	}

	/** MULTI string that will be substituted into the message */
	private String multi;
	
	/** Set the MULTI string that will be substituted into the message */
	@Override
	public void setMulti(String m) {
		multi = m;
	}

	/** Set the MULTI string that will be substituted into the message */
	public void doSetMulti(String m) throws TMSException {
		if (m != multi) {
			store.update(this, "multi", m);
			setMulti(m);
		}
	}
	
	/** Get the MULTI string that will be substituted into the message */
	@Override
	public String getMulti() {
		return multi;
	}
}
