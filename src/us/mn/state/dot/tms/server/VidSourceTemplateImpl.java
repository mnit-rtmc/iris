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

import us.mn.state.dot.tms.VidSourceTemplate;
import us.mn.state.dot.tms.TMSException;

/** Server-side implementation of video-source-template.
 * 
 * (See VidSourceTemplate.java and comments later
 *  in this file for more details.)
 * 
 * @author John L. Stanley - SRF Consulting
 */
public class VidSourceTemplateImpl extends BaseObjectImpl implements VidSourceTemplate {

	/** Load all the stream templates */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, VidSourceTemplateImpl.class);
		store.query("SELECT name, label, config, "+
			"default_port, subnets, latency, "+
			"encoder, scheme, codec, "+
			"rez_width, rez_height, multicast, "+
			"notes FROM iris." +
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

	/** Create a VidSourceTemplateImpl object from database fields. */
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

	/** Create an VidSourceTemplate */
	public VidSourceTemplateImpl(String n) {
		super(n);
		// TODO Auto-generated constructor stub
	}

	/** Create an VidSourceTemplate */
	private VidSourceTemplateImpl(String sName,
			String label,  String config,
			Object default_port, String subnets,
			Object latency, String encoder,
			String scheme, String codec,
			Object rez_width, Object rez_height,
			Object multicast, String notes) {
		super(sName);
		this.label = label;
		this.config = config;
		this.default_port = (Integer)default_port;
		this.subnets = subnets;
		this.latency = (Integer)latency;
		this.encoder = encoder;
		this.scheme = scheme;
		this.codec = codec;
		this.rez_width = (Integer)rez_width;
		this.rez_height = (Integer)rez_height;
		this.multicast = (Boolean)multicast;
		this.notes = notes;
	}

	//---------------------------------
	
	private String label;   // source-type identifier shown in video window
	private String config;
		// If codec is empty:
		//   config is a backwards-compatible uri_path string
		// If codec is not empty:
		//   config is a gst-launch config string
		// substitution fields
		//	{addr} address from camera encoder field
		//	{port} port from camera encoder field
		//	{addrport} address[:port] from camera encoder field
		//	{chan} encoder_channel from camera record
		//	{name} camera.name modified to not use reserved URI chars.
		//		(For live555 video proxy.)
		//	{<other IRIS system property>}
		//      (Substitute IRIS system-property field into config string.)
		//	examples
		//      rtsp://83.244.45.234/{camName}
		//      http://{addrport}/mpeg4/media.amp
	private Integer default_port;
		// If no port specified in camera record, use this value for {port} substitution
		// If no port specified here or in camera record, and {port} or {mport} is in the config string, don't use this template
	private String subnets;
		// Comma separated list of subnet identifiers where source is available.
		// If empty, the source is available in all subnets.
	private Integer latency;
	private String encoder; // Name of manufacturer & model
	private String scheme;  // (rtsp/http/udp/ftp)
	private String codec;   // (MJPEG, MPEG2, MPEG4, H264, H265, JPEG, etc)
	                // If empty, codec is probably MJPEG
	private Integer rez_width;
	private Integer rez_height;
	private Boolean multicast; // (T/F)
	private String notes;
	
	//-- maybe implement later?
//	String users; // Semicolon separated list of user-groups and users permitted to use that stream.
//	// If field is blank, stream is usable by all users.
//	// Stretch: u-g/u identifiers prefixed with '-' are not-permitted.
	
	//---------------------------------
	
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/** Set the template label */
	public void doSetLabel(String label) throws TMSException {
		if (label != this.label) {
			store.update(this, "label", label);
			setLabel(label);
		}
	}
	
	/**
	 * @return the config
	 */
	public String getConfig() {
		return config;
	}

	/**
	 * @param config to set
	 */
	public void setConfig(String config) {
		this.config = config;
	}

	/** Set the template config */
	public void doSetConfig(String config) throws TMSException {
		if (config != this.config) {
			store.update(this, "config", config);
			setConfig(config);
		}
	}
	
	/**
	 * @return the default_port
	 */
	public Integer getDefaultPort() {
		return default_port;
	}

	/**
	 * @param default_port the default_port to set
	 */
	public void setDefaultPort(Integer default_port) {
		this.default_port = default_port;
	}

	/** Set the template default_port */
	public void doSetdefault_port(Integer default_port) throws TMSException {
		if (default_port != this.default_port) {
			store.update(this, "default_port", default_port);
			setDefaultPort(default_port);
		}
	}
	
	/**
	 * @return the subnets
	 */
	public String getSubnets() {
		return subnets;
	}

	/**
	 * @param subnets the subnets to set
	 */
	public void setSubnets(String subnets) {
		this.subnets = subnets;
	}

	/** Set the template subnets */
	public void doSetSubnets(String subnets) throws TMSException {
		if (subnets != this.subnets) {
			store.update(this, "subnets", subnets);
			setSubnets(subnets);
		}
	}
	
	/**
	 * @return the latency
	 */
	public Integer getLatency() {
		return latency;
	}

	/**
	 * @param latency the latency to set
	 */
	public void setLatency(Integer latency) {
		this.latency = latency;
	}
	
	/** Set the template latency */
	public void doSetLatency(Integer latency) throws TMSException {
		if (latency != this.latency) {
			store.update(this, "latency", latency);
			setLatency(latency);
		}
	}

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
	public void setEncoder(String encoder) {
		this.encoder = encoder;
	}
	
	/** Set the template encoder */
	public void doSetEncoder(String encoder) throws TMSException {
		if (encoder != this.encoder) {
			store.update(this, "encoder", encoder);
			setEncoder(encoder);
		}
	}

	/**
	 * @return the scheme
	 */
	public String getScheme() {
		return scheme;
	}

	/**
	 * @param scheme the scheme to set
	 */
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
	
	/** Set the template scheme */
	public void doSetScheme(String scheme) throws TMSException {
		if (scheme != this.scheme) {
			store.update(this, "scheme", scheme);
			setScheme(scheme);
		}
	}

	/**
	 * @return the codec
	 */
	public String getCodec() {
		return codec;
	}

	/**
	 * @param codec the codec to set
	 */
	public void setCodec(String codec) {
		this.codec = codec;
	}

	/** Set the template codec */
	public void doSetCodec(String codec) throws TMSException {
		if (codec != this.codec) {
			store.update(this, "codec", codec);
			setCodec(codec);
		}
	}
	
	/**
	 * @return the rezWidth
	 */
	public Integer getRezWidth() {
		return rez_width;
	}

	/**
	 * @param rezWidth the rezWidth to set
	 */
	public void setRezWidth(Integer rezWidth) {
		this.rez_width = rezWidth;
	}

	/** Set the template rez_width */
	public void doSetRezWidth(Integer rez_width) throws TMSException {
		if (rez_width != this.rez_width) {
			store.update(this, "rez_width", rez_width);
			setRezWidth(rez_width);
		}
	}
	
	/**
	 * @return the rez_height
	 */
	public Integer getRezHeight() {
		return rez_height;
	}

	/**
	 * @param rez_height the rez_height to set
	 */
	public void setRezHeight(Integer rez_height) {
		this.rez_height = rez_height;
	}

	/** Set the template rez_height */
	public void doSetRezHeight(Integer rez_height) throws TMSException {
		if (rez_height != this.rez_height) {
			store.update(this, "rez_height", rez_height);
			setRezHeight(rez_height);
		}
	}
	
	/**
	 * @return the multicast
	 */
	public Boolean getMulticast() {
		return multicast;
	}

	/**
	 * @param multicast the multicast to set
	 */
	public void setMulticast(Boolean multicast) {
		this.multicast = multicast;
	}

	/** Set the template multicast */
	public void doSetMulticast(Boolean multicast) throws TMSException {
		if (multicast != this.multicast) {
			store.update(this, "multicast", multicast);
			setMulticast(multicast);
		}
	}
	
	@Override
	public String getNotes() {
		return notes;
	}

	@Override
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/** Set the template notes */
	public void doSetNotes(String notes) throws TMSException {
		if (notes != this.notes) {
			store.update(this, "notes", notes);
			setNotes(notes);
		}
	}
}
