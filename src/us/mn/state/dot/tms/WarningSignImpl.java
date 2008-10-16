/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.util.HashMap;
import java.util.Map;
import java.sql.ResultSet;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.server.Namespace;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.WarningSignPoller;

/**
 * WarningSignImpl is a traffic device can display one fixed message. It can
 * only be turned on or off.
 *
 * @author Douglas Lau
 */
public class WarningSignImpl extends Device2Impl implements WarningSign {

	/** Load all the warning signs */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading warning signs...");
		namespace.registerType(SONAR_TYPE, WarningSignImpl.class);
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"camera, message FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.add(new WarningSignImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// geo_loc
					row.getString(3),	// controller
					row.getInt(4),		// pin
					row.getString(5),	// notes
					row.getString(6),	// camera
					row.getString(7)	// message
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc.getName());
		if(controller != null)
			map.put("controller", controller.getName());
		map.put("pin", pin);
		map.put("notes", notes);
		if(camera != null)
			map.put("camera", camera.getName());
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

	/** Create a new warning sign with a string name */
	public WarningSignImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		MainServer.server.createObject(g);
		geo_loc = g;
	}

	/** Create a warning sign */
	protected WarningSignImpl(String n, GeoLocImpl l, ControllerImpl c,
		int p, String nt, CameraImpl cam, String m)
	{
		super(n, c, p, nt);
		geo_loc = l;
		camera = cam;
		message = m;
		initTransients();
	}

	/** Create a warning sign */
	protected WarningSignImpl(Namespace ns, String n, String l, String c,
		int p, String nt, String cam, String m) throws NamespaceError
	{
		this(n, (GeoLocImpl)ns.getObject(GeoLoc.SONAR_TYPE, l),
			(ControllerImpl)ns.getObject(Controller.SONAR_TYPE, c),
			p, nt, (CameraImpl)ns.getObject(Camera.SONAR_TYPE, cam),
			m);
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		super.doDestroy();
		store.destroy(geo_loc);
	}

	/** Device location */
	protected GeoLocImpl geo_loc;

	/** Get the device location */
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Camera from which this sign can be seen */
	protected Camera camera;

	/** Set the verification camera */
	public void setCamera(Camera c) {
		camera = c;
	}

	/** Set the verification camera */
	public void doSetCamera(Camera c) throws TMSException {
		if(c == camera)
			return;
		if(c == null)
			store.update(this, "camera", null);
		else
			store.update(this, "camera", c.getName());
		setCamera(c);
	}

	/** Get verification camera */
	public Camera getCamera() {
		return camera;
	}

	/** Message text of the sign */
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

	/** Flag for deployed status */
	protected transient boolean deployed;

	/** Set the deployed status of the sign */
	public void setDeployed(boolean d) {
		WarningSignPoller p = getWarningSignPoller();
		if(p != null)
			p.setDeployed(this, d);
	}

	/** Check if the warning sign is deployed */
	public boolean getDeployed() {
		return deployed;
	}

	/** Set the actual deployed status from the controller */
	public void setDeployedStatus(boolean d) {
		if(d != deployed) {
			deployed = d;
			notifyStatus();
		}
	}

	/** Get a warning sign poller */
	protected WarningSignPoller getWarningSignPoller() {
		if(isActive()) {
			MessagePoller p = getPoller();
			if(p instanceof WarningSignPoller)
				return (WarningSignPoller)p;
		}
		return null;
	}
}
