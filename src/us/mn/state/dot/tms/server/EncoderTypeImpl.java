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
		store.query("SELECT name, http_path, rtsp_path, latency " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
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
		map.put("http_path", http_path);
		map.put("rtsp_path", rtsp_path);
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
		http_path = "";
		rtsp_path = "";
		latency = DEFAULT_LATENCY_MS;
	}

	/** Create an encoder type */
	private EncoderTypeImpl(ResultSet row) throws SQLException {
		this(row.getString(1),		// name
		     row.getString(2),		// http_path
		     row.getString(3),		// rtsp_path
		     row.getInt(4)		// latency
		);
	}

	/** Create a new encoder type */
	private EncoderTypeImpl(String n, String hp, String rp, int l) {
		this(n);
		http_path = hp;
		rtsp_path = rp;
		latency = l;
	}

	/** HTTP path */
	private String http_path;

	/** Set the HTTP path */
	@Override
	public void setHttpPath(String p) {
		http_path = p;
	}

	/** Set the HTTP path */
	public void doSetHttpPath(String p) throws TMSException {
		if (!p.equals(http_path)) {
			store.update(this, "http_path", p);
			setHttpPath(p);
		}
	}

	/** Get the HTTP path */
	@Override
	public String getHttpPath() {
		return http_path;
	}

	/** RTSP path */
	private String rtsp_path;

	/** Set the RTSP path */
	@Override
	public void setRtspPath(String p) {
		rtsp_path = p;
	}

	/** Set the RTSP path */
	public void doSetRtspPath(String p) throws TMSException {
		if (!p.equals(rtsp_path)) {
			store.update(this, "rtsp_path", p);
			setRtspPath(p);
		}
	}

	/** Get the RTSP path */
	@Override
	public String getRtspPath() {
		return rtsp_path;
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
