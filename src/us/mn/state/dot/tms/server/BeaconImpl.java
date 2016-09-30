/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
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
			"preset, message, verify_pin FROM iris." + SONAR_TYPE +
			";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new BeaconImpl(
					row.getString(1),	// name
					row.getString(2),	// geo_loc
					row.getString(3),	// controller
					row.getInt(4),		// pin
					row.getString(5),	// notes
					row.getString(6),	// preset
					row.getString(7),	// message
					row.getInt(8)		// verify_pin
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("notes", notes);
		map.put("preset", preset);
		map.put("message", message);
		map.put("verify_pin", verify_pin);
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

	/** Create a new beacon with a string name */
	public BeaconImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		g.notifyCreate();
		geo_loc = g;
	}

	/** Create a beacon */
	protected BeaconImpl(String n, GeoLocImpl l, ControllerImpl c,
		int p, String nt, CameraPresetImpl cp, String m, Integer vp)
	{
		super(n, c, p, nt);
		geo_loc = l;
		setPreset(cp);
		message = m;
		verify_pin = vp;
		initTransients();
	}

	/** Create a beacon */
	protected BeaconImpl(String n, String l, String c, int p, String nt,
		String cp, String m, Integer vp)
	{
		this(n, lookupGeoLoc(l), lookupController(c), p, nt,
		     lookupPreset(cp), m, vp);
	}

	/** Initialize transients */
	@Override
	public void initTransients() {
		ControllerImpl c = controller;
		Integer vp = verify_pin;
		if (c != null && vp != null)
			c.setIO(vp, this);
		super.initTransients();
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		setPreset(null);
		geo_loc.notifyRemove();
	}

	/** Set the controller of the beacon */
	@Override
	protected void doSetControllerImpl(ControllerImpl c)
		throws TMSException
	{
		ControllerImpl oc = controller;
		Integer vp = verify_pin;
		if (vp != null && c != null)
			checkControllerPin(vp);
		super.doSetControllerImpl(c);
		updateVerifyPin(oc, vp, c, vp);
	}

	/** Update the controller verify pin.
	 * @param oc Old controller.
	 * @param op Old verify pin.
	 * @param nc New controller.
	 * @param np New verify pin. */
	private void updateVerifyPin(ControllerImpl oc, Integer op,
		ControllerImpl nc, Integer np)
	{
		if (oc != null && op != null)
			oc.setIO(op, null);
		if (nc != null && np != null)
			nc.setIO(np, this);
	}

	/** Item style bits */
	private transient long styles = 0;

	/** Update the beacon styles */
	@Override
	public void updateStyles() {
		long s = ItemStyle.ALL.bit();
		if (getController() == null)
			s |= ItemStyle.NO_CONTROLLER.bit();
		if (!isActive())
			s |= ItemStyle.INACTIVE.bit();
		if (needsMaintenance())
			s |= ItemStyle.MAINTENANCE.bit();
		if (isActive() && isFailed())
			s |= ItemStyle.FAILED.bit();
		if (isAvailable())
			s |= ItemStyle.AVAILABLE.bit();
		if (getFlashing())
			s |= ItemStyle.DEPLOYED.bit();
		setStyles(s);
	}

	/** Set the item style bits (and notify clients) */
	private void setStyles(long s) {
		if (s != styles) {
			styles = s;
			notifyAttribute("styles");
		}
	}

	/** Get item style bits */
	@Override
	public long getStyles() {
		return styles;
	}

	/** Test if beacon is available */
	private boolean isAvailable() {
		return isOnline() && !needsMaintenance() && !getFlashing();
	}

	/** Test if beacon needs maintenance */
	private boolean needsMaintenance() {
		return isOnline() && !getMaintenance().isEmpty();
	}

	/** Get maintenance status */
	private String getMaintenance() {
		return ControllerHelper.getMaintenance(getController());
	}

	/** Device location */
	private GeoLocImpl geo_loc;

	/** Get the device location */
	@Override
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
	private String message = "";

	/** Set the message text */
	@Override
	public void setMessage(String m) {
		message = m;
	}

	/** Set the message text */
	public void doSetMessage(String m) throws TMSException {
		if (m.equals(message))
			return;
		store.update(this, "message", m);
		setMessage(m);
	}

	/** Get the message text */
	@Override
	public String getMessage() {
		return message;
	}

	/** Controller I/O verify pin number */
	private Integer verify_pin;

	/** Set the controller I/O verify pin number */
	@Override
	public void setVerifyPin(Integer p) {
		verify_pin = p;
	}

	/** Set the controller I/O verify pin number */
	public void doSetVerifyPin(Integer p) throws TMSException {
		if (integerEquals(p, verify_pin))
			return;
		if (p != null && (p < 1 || p > Controller.ALL_PINS))
			throw new ChangeVetoException("Invalid pin: " + p);
		if (p != null)
			checkControllerPin(p);
		store.update(this, "verify_pin", p);
		updateVerifyPin(controller, verify_pin, controller, p);
		setVerifyPin(p);
	}

	/** Get the controller I/O verify pin number */
	@Override
	public Integer getVerifyPin() {
		return verify_pin;
	}

	/** Flashing state */
	private transient boolean flashing;

	/** Set the flashing state */
	@Override
	public void setFlashing(boolean f) {
		BeaconPoller p = getBeaconPoller();
		if (p != null)
			p.setFlashing(this, f);
	}

	/** Check if the beacon is flashing */
	@Override
	public boolean getFlashing() {
		return flashing;
	}

	/** Set the flashing state and notify clients */
	public void setFlashingNotify(boolean f) {
		if (f != flashing) {
			flashing = f;
			notifyAttribute("flashing");
			logBeaconEvent(f);
			updateStyles();
		}
	}

	/** Log a beacon event */
	private void logBeaconEvent(boolean f) {
		EventType et = (f)
		             ? EventType.BEACON_ON_EVENT
		             : EventType.BEACON_OFF_EVENT;
		logEvent(new BeaconEvent(et, name));
	}

	/** Get a beacon poller */
	private BeaconPoller getBeaconPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof BeaconPoller) ? (BeaconPoller) dp : null;
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		BeaconPoller p = getBeaconPoller();
		if (p != null)
			p.sendRequest(this, dr);
	}
}
