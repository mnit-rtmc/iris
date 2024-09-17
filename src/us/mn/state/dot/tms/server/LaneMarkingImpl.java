/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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
import java.sql.SQLException;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.LaneMarkingPoller;

/**
 * A lane marking is a dynamically-controlled lane striping, such as
 * in-pavement LED lighting.
 *
 * @author Douglas Lau
 */
public class LaneMarkingImpl extends DeviceImpl implements LaneMarking {

	/** Load all the lane markings */
	static protected void loadAll() throws TMSException {
		namespace.registerType(LaneMarkingImpl.class);
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"deployed FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LaneMarkingImpl(row));
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
		map.put("deployed", deployed);
		return map;
	}

	/** Create a new lane marking with a string name */
	public LaneMarkingImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name, SONAR_TYPE);
		g.notifyCreate();
		geo_loc = g;
	}

	/** Create a lane marking */
	private LaneMarkingImpl(ResultSet row) throws SQLException {
		this(row.getString(1), // name
		     row.getString(2), // geo_loc
		     row.getString(3), // controller
		     row.getInt(4),    // pin
		     row.getString(5), // notes
		     row.getBoolean(6) // deployed
		);
	}

	/** Create a lane marking */
	private LaneMarkingImpl(String n, String gl, String c, int p, String nt,
		boolean d)
	{
		super(n, lookupController(c), p, nt);
		geo_loc = lookupGeoLoc(gl);
		deployed = d;
		initTransients();
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		geo_loc.notifyRemove();
	}

	/** Device location */
	private final GeoLocImpl geo_loc;

	/** Get the device location */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Flag for deployed status */
	private boolean deployed;

	/** Set the deployed status of the sign */
	@Override
	public void setDeployed(boolean d) {
		LaneMarkingPoller p = getLaneMarkingPoller();
		if (p != null)
			p.setDeployed(this, d);
	}

	/** Check if the lane marking is deployed */
	@Override
	public boolean getDeployed() {
		return deployed;
	}

	/** Set the actual deployed status from the controller */
	public void setDeployedNotify(boolean d) {
		if (d != deployed) {
			try {
				store.update(this, "deployed", d);
				deployed = d;
				notifyAttribute("deployed");
			}
			catch (TMSException e) {
				e.printStackTrace();
			}
		}
	}

	/** Get a lane marking poller */
	private LaneMarkingPoller getLaneMarkingPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof LaneMarkingPoller)
		      ?	(LaneMarkingPoller) dp
		      : null;
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		// no device requests are currently supported
	}
}
