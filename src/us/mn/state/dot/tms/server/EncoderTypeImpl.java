/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.Encoding;
import us.mn.state.dot.tms.TMSException;

/**
 * Video encoder type.
 *
 * @author Douglas Lau
 */
public class EncoderTypeImpl extends BaseObjectImpl implements EncoderType {

	/** Load all the encoder types */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, EncoderTypeImpl.class);
		store.query("SELECT name, encoding, uri_scheme, uri_path, " +
			"latency FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new EncoderTypeImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("encoding", encoding);
		map.put("uri_scheme", uri_scheme);
		map.put("uri_path", uri_path);
		map.put("latency", latency);
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

	/** Create a new encoder type */
	public EncoderTypeImpl(String n) {
		super(n);
		encoding = Encoding.UNKNOWN.ordinal();
		uri_scheme = "";
		uri_path = "";
		latency = DEFAULT_LATENCY_MS;
	}

	/** Create an encoder type */
	private EncoderTypeImpl(ResultSet row) throws SQLException {
		this(row.getString(1),		// name
		     row.getInt(2),		// encoding
		     row.getString(3),		// uri_scheme
		     row.getString(4),		// uri_path
		     row.getInt(5)		// latency
		);
	}

	/** Create a new encoder type */
	private EncoderTypeImpl(String n, int e, String s, String p, int l) {
		this(n);
		encoding = e;
		uri_scheme = s;
		uri_path = p;
		latency = l;
	}

	/** Encoding ordinal */
	private int encoding;

	/** Set the encoding ordinal */
	@Override
	public void setEncoding(int e) {
		encoding = e;
	}

	/** Set the encoding ordinal */
	public void doSetEncoding(int e) throws TMSException {
		if (e != encoding) {
			store.update(this, "encoding", e);
			setEncoding(e);
		}
	}

	/** Get the encoding ordinal */
	@Override
	public int getEncoding() {
		return encoding;
	}

	/** URI scheme */
	private String uri_scheme;

	/** Set the URI scheme */
	@Override
	public void setUriScheme(String s) {
		uri_scheme = s;
	}

	/** Set the URI scheme */
	public void doSetUriScheme(String s) throws TMSException {
		if (!s.equals(uri_scheme)) {
			store.update(this, "uri_scheme", s);
			setUriScheme(s);
		}
	}

	/** Get the URI scheme */
	@Override
	public String getUriScheme() {
		return uri_scheme;
	}

	/** URI path */
	private String uri_path;

	/** Set the URI path */
	@Override
	public void setUriPath(String p) {
		uri_path = p;
	}

	/** Set the URI path */
	public void doSetUriPath(String p) throws TMSException {
		if (!p.equals(uri_path)) {
			store.update(this, "uri_path", p);
			setUriPath(p);
		}
	}

	/** Get the URI path */
	@Override
	public String getUriPath() {
		return uri_path;
	}

	/** Stream latency */
	private int latency;

	/** Set the stream latency (ms) */
	@Override
	public void setLatency(int l) {
		latency = l;
	}

	/** Set the stream latency (ms) */
	public void doSetLatency(int l) throws TMSException {
		if (l != latency) {
			store.update(this, "latency", l);
			setLatency(l);
		}
	}

	/** Get the stream latency (ms) */
	@Override
	public int getLatency() {
		return latency;
	}
}
