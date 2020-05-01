/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2020  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.EncoderStream;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.Encoding;
import us.mn.state.dot.tms.EncodingQuality;
import us.mn.state.dot.tms.TMSException;

/**
 * Video encoder stream.
 *
 * @author Douglas Lau
 */
public class EncoderStreamImpl extends BaseObjectImpl implements EncoderStream {

	/** Load all the encoder streams */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, EncoderStreamImpl.class);
		store.query("SELECT name, encoder_type, view_num, " +
			"flow_stream, encoding, quality, uri_scheme, " +
			"uri_path, mcast_port, latency FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new EncoderStreamImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("encoder_type", encoder_type);
		map.put("view_num", view_num);
		map.put("flow_stream", flow_stream);
		map.put("encoding", encoding);
		map.put("quality", quality);
		map.put("uri_scheme", uri_scheme);
		map.put("uri_path", uri_path);
		map.put("mcast_port", mcast_port);
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

	/** Create a new encoder stream */
	public EncoderStreamImpl(String n) {
		super(n);
		encoding = Encoding.UNKNOWN.ordinal();
		quality = EncodingQuality.MEDIUM.ordinal();
		latency = DEFAULT_LATENCY_MS;
	}

	/** Create an encoder stream */
	private EncoderStreamImpl(ResultSet row) throws SQLException {
		this(row.getString(1),           // name
		     row.getString(2),           // encoder_type
		     (Integer) row.getObject(3), // view_num
		     row.getBoolean(4),          // flow_stream
		     row.getInt(5),              // encoding
		     row.getInt(6),              // quality
		     row.getString(7),           // uri_scheme
		     row.getString(8),           // uri_path
		     (Integer) row.getObject(9), // mcast_port
		     row.getInt(10)              // latency
		);
	}

	/** Create a new encoder stream */
	private EncoderStreamImpl(String n, String et, Integer vn, boolean fs,
		int e, int q, String s, String p, Integer mp, int l)
	{
		this(n);
		encoder_type = lookupEncoderType(et);
		view_num = vn;
		flow_stream = fs;
		encoding = e;
		quality = q;
		uri_scheme = s;
		uri_path = p;
		mcast_port = mp;
		latency = l;
	}

	/** Encoder type */
	private EncoderType encoder_type;

	/** Get the encoder type */
	@Override
	public EncoderType getEncoderType() {
		return encoder_type;
	}

	/** View number */
	private Integer view_num;

	/** Set the view number */
	@Override
	public void setViewNum(Integer vn) {
		view_num = vn;
	}

	/** Set the view number */
	public void doSetViewNum(Integer vn) throws TMSException {
		if (vn != view_num) {
			store.update(this, "view_num", vn);
			setViewNum(vn);
		}
	}

	/** Get the view number */
	@Override
	public Integer getViewNum() {
		return view_num;
	}

	/** Flow stream flag */
	private boolean flow_stream;

	/** Set the flow stream flag */
	@Override
	public void setFlowStream(boolean fs) {
		flow_stream = fs;
	}

	/** Set the flow stream flag */
	public void doSetFlowStream(boolean fs) throws TMSException {
		if (fs != flow_stream) {
			store.update(this, "flow_stream", fs);
			setFlowStream(fs);
		}
	}

	/** Get the flow stream flag */
	@Override
	public boolean getFlowStream() {
		return flow_stream;
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

	/** Encoding quality ordinal */
	private int quality;

	/** Set the encoding quality ordinal */
	@Override
	public void setQuality(int q) {
		quality = q;
	}

	/** Set the encoding quality ordinal */
	public void doSetQuality(int q) throws TMSException {
		if (q != quality) {
			store.update(this, "quality", q);
			setQuality(q);
		}
	}

	/** Get the encoding quality ordinal */
	@Override
	public int getQuality() {
		return quality;
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
		if (!objectEquals(s, uri_scheme)) {
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
		if (!objectEquals(p, uri_path)) {
			store.update(this, "uri_path", p);
			setUriPath(p);
		}
	}

	/** Get the URI path */
	@Override
	public String getUriPath() {
		return uri_path;
	}

	/** Multicast port */
	private Integer mcast_port;

	/** Set the multicast port */
	@Override
	public void setMcastPort(Integer mp) {
		mcast_port = mp;
	}

	/** Set the multicast port */
	public void doSetMcastPort(Integer mp) throws TMSException {
		if (mp != mcast_port) {
			store.update(this, "mcast_port", mp);
			setMcastPort(mp);
		}
	}

	/** Get the multicast port */
	@Override
	public Integer getMcastPort() {
		return mcast_port;
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
