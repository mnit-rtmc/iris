/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2014  Minnesota Department of Transportation
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

import java.util.HashMap;
import java.util.Map;
import java.sql.ResultSet;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.comm.BeaconPoller;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.event.BeaconEvent;

/**
 * A Beacon is a light which flashes toward oncoming traffic.
 *
 * @author Douglas Lau
 */
public class BeaconImpl extends DeviceImpl implements Beacon {

	/** Load all the beacons */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, BeaconImpl.class);
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"preset, message FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new BeaconImpl(
					row.getString(1),	// name
					row.getString(2),	// geo_loc
					row.getString(3),	// controller
					row.getInt(4),		// pin
					row.getString(5),	// notes
					row.getString(6),	// preset
					row.getString(7)	// message
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("notes", notes);
		map.put("preset", preset);
		map.put("message", message);
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

	/** Create a new beacon with a string name */
	public BeaconImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		g.notifyCreate();
		geo_loc = g;
	}

	/** Create a beacon */
	protected BeaconImpl(String n, GeoLocImpl l, ControllerImpl c,
		int p, String nt, CameraPresetImpl cp, String m)
	{
		super(n, c, p, nt);
		geo_loc = l;
		setPreset(cp);
		message = m;
		initTransients();
	}

	/** Create a beacon */
	protected BeaconImpl(String n, String l, String c, int p, String nt,
		String cp, String m)
	{
		this(n, lookupGeoLoc(l), lookupController(c), p, nt,
		     lookupPreset(cp), m);
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		setPreset(null);
		geo_loc.notifyRemove();
	}

	/** Device location */
	protected GeoLocImpl geo_loc;

	/** Get the device location */
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Camera preset from which the beacon can be seen */
	private CameraPreset preset;

	/** Set the verification camera preset */
	@Override
	public void setPreset(CameraPreset cp) {
		final CameraPreset ocp = preset;
		if (cp instanceof CameraPresetImpl) {
			CameraPresetImpl cpi = (CameraPresetImpl)cp;
			cpi.setAssignedNotify(true);
		}
		preset = cp;
		if (ocp instanceof CameraPresetImpl) {
			CameraPresetImpl ocpi = (CameraPresetImpl)ocp;
			ocpi.setAssignedNotify(false);
		}
	}

	/** Set the verification camera preset */
	public void doSetPreset(CameraPreset cp) throws TMSException {
		if (cp == preset)
			return;
		store.update(this, "preset", cp);
		setPreset(cp);
	}

	/** Get verification camera preset */
	@Override
	public CameraPreset getPreset() {
		return preset;
	}

	/** Message text */
	protected String message = "";

	/** Set the message text */
	public void setMessage(String m) {
		message = m;
	}

	/** Set the message text */
	public void doSetMessage(String m) throws TMSException {
		if(m.equals(message))
			return;
		store.update(this, "message", m);
		setMessage(m);
	}

	/** Get the message text */
	public String getMessage() {
		return message;
	}

	/** Flashing state */
	private transient boolean flashing;

	/** Set the flashing state */
	public void setFlashing(boolean f) {
		BeaconPoller p = getBeaconPoller();
		if(p != null)
			p.setFlashing(this, f);
	}

	/** Check if the beacon is flashing */
	public boolean getFlashing() {
		return flashing;
	}

	/** Set the flashing state and notify clients */
	public void setFlashingNotify(boolean f) {
		if(f != flashing) {
			flashing = f;
			notifyAttribute("flashing");
			logBeaconEvent(f);
		}
	}

	/** Log a beacon event */
	private void logBeaconEvent(boolean f) {
		EventType et = (f)
		             ? EventType.BEACON_ON_EVENT
		             : EventType.BEACON_OFF_EVENT;
		BeaconEvent ev = new BeaconEvent(et, name);
		try {
			ev.doStore();
		}
		catch (TMSException e) {
			e.printStackTrace();
		}
	}

	/** Get a beacon poller */
	private BeaconPoller getBeaconPoller() {
		if (isActive()) {
			DevicePoller dp = getPoller();
			if (dp instanceof BeaconPoller)
				return (BeaconPoller)dp;
		}
		return null;
	}

	/** Request a device operation */
	@Override
	public void setDeviceRequest(int r) {
		sendDeviceRequest(DeviceRequest.fromOrdinal(r));
	}

	/** Request a device operation */
	public void sendDeviceRequest(DeviceRequest req) {
		BeaconPoller p = getBeaconPoller();
		if (p != null)
			p.sendRequest(this, req);
	}
}
