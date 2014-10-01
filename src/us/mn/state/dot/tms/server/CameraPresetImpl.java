/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.TMSException;

/**
 * CameraPresetImpl represents a single CCTV camera preset.
 *
 * @author Douglas Lau
 */
public class CameraPresetImpl extends BaseObjectImpl implements CameraPreset {

	/** Load all the camera presets */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, CameraPresetImpl.class);
		store.query("SELECT name, camera, preset_num, direction " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new CameraPresetImpl(
					namespace,
					row.getString(1),	// name
					row.getString(2),	// camera
					row.getInt(3),		// preset_num
					row.getShort(4)		// direction
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("camera", camera);
		map.put("preset_num", preset_num);
		map.put("direction", direction);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new camera preset with a string name */
	public CameraPresetImpl(String n) throws TMSException {
		super(n);
	}

	/** Create a camera preset */
	protected CameraPresetImpl(String n, CameraImpl c, int p, short d) {
		super(n);
		camera = c;
		preset_num = p;
		direction = d;
	}

	/** Create a camera preset */
	protected CameraPresetImpl(Namespace ns, String n, String c, int p,
		short d)
	{
		this(n, (CameraImpl)ns.lookupObject(Camera.SONAR_TYPE, c), p,d);
	}

	/** CCTV Camera */
	private CameraImpl camera;

	/** Get the CCTV camera */
	@Override
	public Camera getCamera() {
		return camera;
	}

	/** Preset number */
	private int preset_num;

	/** Get the preset number */
	@Override
	public int getPresetNum() {
		return preset_num;
	}

	/** Preset direction */
	private short direction;

	/** Set the direction */
	@Override
	public void setDirection(short d) {
		direction = d;
	}

	/** Set the direction */
	public void doSetDirection(short d) throws TMSException {
		if (d != direction) {
			store.update(this, "direction", d);
			setDirection(d);
		}
	}

	/** Get the direction */
	@Override
	public short getDirection() {
		return direction;
	}

	/** Flag to indicate device assignment */
	private transient boolean assigned = false;

	/** Set assigned flag */
	public void setAssignedNotify(boolean a) {
		assigned = a;
		notifyAttribute("assigned");
	}

	/** Get assigned flag */
	@Override
	public boolean getAssigned() {
		return assigned;
	}
}
