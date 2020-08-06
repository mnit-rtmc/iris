/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  SRF Consulting Group
 * Copyright (C) 2020  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.Map;

import us.mn.state.dot.tms.CameraTemplate;
import us.mn.state.dot.tms.TMSException;

/**
 * @author John L. Stanley - SRF Consulting
 */
public class CameraTemplateImpl extends BaseObjectImpl implements CameraTemplate {

	/** Load all the camera templates */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, CameraTemplateImpl.class);
		store.query("SELECT name, notes, label FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new CameraTemplateImpl(
					row.getString(1), // name
					row.getString(2), // notes
					row.getString(3)  // label
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("notes", notes);
		map.put("label", label);
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

	/** Create a camera template */
	public CameraTemplateImpl(String n) {
		super(n);
	}

	/** Create a camera template */
	public CameraTemplateImpl(String n, String no, String l) {
		super(n);
		notes = no;
		label = l;
	}

	/** Template label */
	private String label;

	/** Get the template label */
	@Override
	public String getLabel() {
		return label;
	}

	/** Set the template label */
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

	/** Template notes */
	private String notes;

	/** Get the template notes */
	@Override
	public String getNotes() {
		return notes;
	}

	/** Set the template notes */
	@Override
	public void setNotes(String n) {
		notes = n;
	}

	/** Set the template notes */
	public void doSetNotes(String n) throws TMSException {
		if (!objectEquals(n, notes)) {
			store.update(this, "notes", n);
			setNotes(n);
		}
	}
}
