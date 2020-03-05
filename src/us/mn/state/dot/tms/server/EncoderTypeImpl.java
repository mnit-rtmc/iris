/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2019  Minnesota Department of Transportation
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
		store.query("SELECT name, make, model, config FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
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
		map.put("make", make);
		map.put("model", model);
		map.put("config", config);
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
		make = "";
		model = "";
		config = "";
	}

	/** Create an encoder type */
	private EncoderTypeImpl(ResultSet row) throws SQLException {
		this(row.getString(1), // name
		     row.getString(2), // make
		     row.getString(3), // model
		     row.getString(4)  // config
		);
	}

	/** Create a new encoder type */
	private EncoderTypeImpl(String n, String mk, String mdl, String c) {
		this(n);
		make = mk;
		model = mdl;
		config = c;
	}

	/** Encoder make */
	private String make;

	/** Set the encoder make */
	@Override
	public void setMake(String m) {
		make = m;
	}

	/** Set the encoder make */
	public void doSetMake(String m) throws TMSException {
		if (!objectEquals(m, make)) {
			store.update(this, "make", m);
			setMake(m);
		}
	}

	/** Get the encoder make */
	@Override
	public String getMake() {
		return make;
	}

	/** Encoder model */
	private String model;

	/** Set the encoder model */
	@Override
	public void setModel(String m) {
		model = m;
	}

	/** Set the encoder model */
	public void doSetModel(String m) throws TMSException {
		if (!objectEquals(m, model)) {
			store.update(this, "model", m);
			setModel(m);
		}
	}

	/** Get the encoder model */
	@Override
	public String getModel() {
		return model;
	}

	/** Encoder config */
	private String config;

	/** Set the encoder config */
	@Override
	public void setConfig(String c) {
		config = c;
	}

	/** Set the encoder config */
	public void doSetConfig(String c) throws TMSException {
		if (!objectEquals(c, config)) {
			store.update(this, "config", c);
			setConfig(c);
		}
	}

	/** Get the encoder config */
	@Override
	public String getConfig() {
		return config;
	}
}
