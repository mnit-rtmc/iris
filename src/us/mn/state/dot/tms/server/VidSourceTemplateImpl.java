/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  SRF Consulting Group
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

import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.VidSourceTemplate;

/** Server-side implementation of video-source-template.
 *
 * (See VidSourceTemplate.java and comments later
 *  in this file for more details.)
 *
 * @author John L. Stanley - SRF Consulting
 */
public class VidSourceTemplateImpl extends BaseObjectImpl
	implements VidSourceTemplate
{
	/** Load all the stream templates */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, VidSourceTemplateImpl.class);
		store.query("SELECT name, label, config, default_port, " +
			"subnets, latency, encoder, scheme, codec, " +
			"rez_width, rez_height, multicast, notes FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new VidSourceTemplateImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("label", label);
		map.put("config", config);
		map.put("default_port", default_port);
		map.put("subnets", subnets);
		map.put("latency", latency);
		map.put("encoder", encoder);
		map.put("scheme", scheme);
		map.put("codec", codec);
		map.put("rez_width", rez_width);
		map.put("rez_height", rez_height);
		map.put("multicast", multicast);
		map.put("notes", notes);
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

	/** Create a video source template */
	public VidSourceTemplateImpl(String n) {
		super(n);
	}

	/** Create a video source template from a row */
	private VidSourceTemplateImpl(ResultSet row) throws SQLException {
		this(row.getString(1),    // name
		     row.getString(2),    // label
		     row.getString(3),    // config
		     row.getObject(4),    // default_port
		     row.getString(5),    // subnets
		     row.getObject(6),    // latency
		     row.getString(7),    // encoder
		     row.getString(8),    // scheme
		     row.getString(9),    // codec
		     row.getObject(10),   // rez_width
		     row.getObject(11),   // rez_height
		     row.getObject(12),   // multicast
		     row.getString(13));  // notes
	}

	/** Create a video source template */
	private VidSourceTemplateImpl(String sName, String label, String config,
		Object default_port, String subnets, Object latency,
		String encoder, String scheme, String codec, Object rez_width,
		Object rez_height, Object multicast, String notes)
	{
		super(sName);
		this.label = label;
		this.config = config;
		this.default_port = (Integer) default_port;
		this.subnets = subnets;
		this.latency = (Integer) latency;
		this.encoder = encoder;
		this.scheme = scheme;
		this.codec = codec;
		this.rez_width = (Integer) rez_width;
		this.rez_height = (Integer) rez_height;
		this.multicast = (Boolean) multicast;
		this.notes = notes;
	}

	/** Source-type identifier shown in video window */
	private String label;

	/**
	 * @return the label
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	@Override
	public void setLabel(String lbl) {
		label = lbl;
	}

	/** Set the template label */
	public void doSetLabel(String lbl) throws TMSException {
		if (!objectEquals(lbl, label)) {
			store.update(this, "label", lbl);
			setLabel(lbl);
		}
	}

	/**
	 * Configuration string.
	 * If codec is empty:
	 *   config is a backwards-compatible uri_path string
	 * If codec is not empty:
	 *   config is a gst-launch config string
	 * substitution fields
	 *	{addr} address from camera encoder field
	 *	{port} port from camera encoder field
	 *	{addrport} address[:port] from camera encoder field
	 *	{chan} encoder_channel from camera record
	 *	{name} camera.name modified to not use reserved URI chars.
	 *		(For live555 video proxy.)
	 *	{&lt;other IRIS system property&gt;}
	 *      (Substitute IRIS system-property field into config string.)
	 *	examples
	 *      rtsp://83.244.45.234/{camName}
	 *      http://{addrport}/mpeg4/media.amp
	 */
	private String config;

	/**
	 * @return the config
	 */
	@Override
	public String getConfig() {
		return config;
	}

	/**
	 * @param config to set
	 */
	@Override
	public void setConfig(String cfg) {
		config = cfg;
	}

	/** Set the template config */
	public void doSetConfig(String cfg) throws TMSException {
		if (!objectEquals(cfg, config)) {
			store.update(this, "config", cfg);
			setConfig(cfg);
		}
	}

	/** Default port.
	 * If no port specified in camera record, use this value for {port}
	 * substitution
	 * If no port specified here or in camera record, and {port} or {mport}
	 * is in the config string, don't use this template */
	private Integer default_port;

	/**
	 * @return the default_port
	 */
	@Override
	public Integer getDefaultPort() {
		return default_port;
	}

	/**
	 * @param default_port the default_port to set
	 */
	@Override
	public void setDefaultPort(Integer dport) {
		default_port = dport;
	}

	/** Set the template default_port */
	public void doSetdefault_port(Integer dport) throws TMSException {
		if (!objectEquals(dport, default_port)) {
			store.update(this, "default_port", dport);
			setDefaultPort(dport);
		}
	}

	/** Comma separated list of subnet identifiers where source is available.
	 * If empty, the source is available in all subnets. */
	private String subnets;

	/**
	 * @return the subnets
	 */
	@Override
	public String getSubnets() {
		return subnets;
	}

	/**
	 * @param subnets the subnets to set
	 */
	@Override
	public void setSubnets(String snets) {
		subnets = snets;
	}

	/** Set the template subnets */
	public void doSetSubnets(String snets) throws TMSException {
		if (!objectEquals(snets, subnets)) {
			store.update(this, "subnets", snets);
			setSubnets(snets);
		}
	}

	private Integer latency;

	/**
	 * @return the latency
	 */
	@Override
	public Integer getLatency() {
		return latency;
	}

	/**
	 * @param latency the latency to set
	 */
	@Override
	public void setLatency(Integer lat) {
		latency = lat;
	}

	/** Set the template latency */
	public void doSetLatency(Integer lat) throws TMSException {
		if (!objectEquals(lat, latency)) {
			store.update(this, "latency", lat);
			setLatency(lat);
		}
	}

 	/** Name of manufacturer &amp; model */
	private String encoder;

	/**
	 * @return the encoder
	 */
	@Override
	public String getEncoder() {
		return encoder;
	}

	/**
	 * @param encoder the encoder to set
	 */
	@Override
	public void setEncoder(String enc) {
		encoder = enc;
	}

	/** Set the template encoder */
	public void doSetEncoder(String enc) throws TMSException {
		if (!objectEquals(enc, encoder)) {
			store.update(this, "encoder", enc);
			setEncoder(enc);
		}
	}

	/** URI scheme (rtsp/http/udp/ftp) */
	private String scheme;

	/**
	 * @return the scheme
	 */
	@Override
	public String getScheme() {
		return scheme;
	}

	/**
	 * @param scheme the scheme to set
	 */
	@Override
	public void setScheme(String scm) {
		scheme = scm;
	}

	/** Set the template scheme */
	public void doSetScheme(String scm) throws TMSException {
		if (!objectEquals(scm, scheme)) {
			store.update(this, "scheme", scm);
			setScheme(scm);
		}
	}

	/** Codec (MJPEG, MPEG2, MPEG4, H264, H265, JPEG, etc).
	 * If empty, codec is probably MJPEG */
	private String codec;

	/**
	 * @return the codec
	 */
	@Override
	public String getCodec() {
		return codec;
	}

	/**
	 * @param codec the codec to set
	 */
	@Override
	public void setCodec(String cdc) {
		codec = cdc;
	}

	/** Set the template codec */
	public void doSetCodec(String cdc) throws TMSException {
		if (!objectEquals(cdc, codec)) {
			store.update(this, "codec", cdc);
			setCodec(cdc);
		}
	}

	private Integer rez_width;

	/**
	 * @return the rezWidth
	 */
	@Override
	public Integer getRezWidth() {
		return rez_width;
	}

	/**
	 * @param rezWidth the rezWidth to set
	 */
	@Override
	public void setRezWidth(Integer w) {
		rez_width = w;
	}

	/** Set the template rez_width */
	public void doSetRezWidth(Integer w) throws TMSException {
		if (!objectEquals(w, rez_width)) {
			store.update(this, "rez_width", w);
			setRezWidth(w);
		}
	}

	private Integer rez_height;

	/**
	 * @return the rez_height
	 */
	@Override
	public Integer getRezHeight() {
		return rez_height;
	}

	/**
	 * @param rez_height the rez_height to set
	 */
	@Override
	public void setRezHeight(Integer h) {
		rez_height = h;
	}

	/** Set the template rez_height */
	public void doSetRezHeight(Integer h) throws TMSException {
		if (!objectEquals(h, rez_height)) {
			store.update(this, "rez_height", h);
			setRezHeight(h);
		}
	}

	/** Multicast (T/F) */
	private Boolean multicast;

	/**
	 * @return the multicast
	 */
	@Override
	public Boolean getMulticast() {
		return multicast;
	}

	/**
	 * @param multicast the multicast to set
	 */
	@Override
	public void setMulticast(Boolean mc) {
		multicast = mc;
	}

	/** Set the template multicast */
	public void doSetMulticast(Boolean mc) throws TMSException {
		if (!objectEquals(mc, multicast)) {
			store.update(this, "multicast", mc);
			setMulticast(mc);
		}
	}

	private String notes;

	@Override
	public String getNotes() {
		return notes;
	}

	@Override
	public void setNotes(String nt) {
		notes = nt;
	}

	/** Set the template notes */
	public void doSetNotes(String nt) throws TMSException {
		if (!objectEquals(nt, notes)) {
			store.update(this, "notes", nt);
			setNotes(nt);
		}
	}
}
