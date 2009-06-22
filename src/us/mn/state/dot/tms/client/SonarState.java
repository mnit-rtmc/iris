/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.sonar.ConfigurationError;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.Privilege;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.Client;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Alarm;
import us.mn.state.dot.tms.BaseHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.Holiday;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.TimingPlan;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.camera.CamCache;
import us.mn.state.dot.tms.client.detector.DetCache;
import us.mn.state.dot.tms.client.dms.DmsCache;
import us.mn.state.dot.tms.client.lcs.LcsCache;
import us.mn.state.dot.tms.client.toast.ConCache;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * Holds the state of the SONAR client
 *
 * @author Douglas Lau
 */
public class SonarState extends Client {

	/** Cache of role proxies */
	protected final TypeCache<Role> roles;

	/** Get the role type cache */
	public TypeCache<Role> getRoles() {
		return roles;
	}

	/** Cache of privilege proxies */
	protected final TypeCache<Privilege> privileges;

	/** Get the privilege type cache */
	public TypeCache<Privilege> getPrivileges() {
		return privileges;
	}

	/** Cache of user proxies */
	protected final TypeCache<User> users;

	/** Get the user type cache */
	public TypeCache<User> getUsers() {
		return users;
	}

	/** Cache of connection proxies */
	protected final TypeCache<Connection> connections;

	/** Get the connection type cache */
	public TypeCache<Connection> getConnections() {
		return connections;
	}

	/** Cache of system attributes */
	protected final TypeCache<SystemAttribute> system_attributes;

	/** Get the system attribute type cache */
	public TypeCache<SystemAttribute> getSystemAttributes() {
		return system_attributes;
	}

	/** Cache of holiday proxies */
	protected final TypeCache<Holiday> holidays;

	/** Get the holiday type cache */
	public TypeCache<Holiday> getHolidays() {
		return holidays;
	}

	/** Cache of graphic proxies */
	protected final TypeCache<Graphic> graphics;

	/** Get the graphic type cache */
	public TypeCache<Graphic> getGraphics() {
		return graphics;
	}

	/** Cache of road proxies */
	protected final TypeCache<Road> roads;

	/** Get the road type cache */
	public TypeCache<Road> getRoads() {
		return roads;
	}

	/** Road proxy list model */
	protected final ProxyListModel<Road> road_model;

	/** Get the road list model */
	public ProxyListModel<Road> getRoadModel() {
		return road_model;
	}

	/** Cache of geo locations */
	protected final TypeCache<GeoLoc> geo_locs;

	/** Get the geo location cache */
	public TypeCache<GeoLoc> getGeoLocs() {
		return geo_locs;
	}

	/** Cache of alarms */
	protected final TypeCache<Alarm> alarms;

	/** Get the alarm cache */
	public TypeCache<Alarm> getAlarms() {
		return alarms;
	}

	/** Available alarm proxy list model */
	protected final ProxyListModel<Alarm> avail_alarm_model;

	/** Get the available alarm list model */
	public ProxyListModel<Alarm> getAvailableAlarms() {
		return avail_alarm_model;
	}

	/** Cache of warning signs */
	protected final TypeCache<WarningSign> warn_signs;

	/** Get the warning sign cache */
	public TypeCache<WarningSign> getWarningSigns() {
		return warn_signs;
	}

	/** Cache of ramp meters */
	protected final TypeCache<RampMeter> ramp_meters;

	/** Get the ramp meter cache */
	public TypeCache<RampMeter> getRampMeters() {
		return ramp_meters;
	}

	/** Cache of controller objects */
	protected final ConCache con_cache;

	/** Get the controller object cache */
	public ConCache getConCache() {
		return con_cache;
	}

	/** Cache of camera objects */
	protected final CamCache cam_cache;

	/** Get the camera object cache */
	public CamCache getCamCache() {
		return cam_cache;
	}

	/** Cache of detector objects */
	protected final DetCache det_cache;

	/** Get the detector object cache */
	public DetCache getDetCache() {
		return det_cache;
	}

	/** Cache of DMS objects */
	protected final DmsCache dms_cache;

	/** Get the DMS object cache */
	public DmsCache getDmsCache() {
		return dms_cache;
	}

	/** Cache of LCS objects */
	protected final LcsCache lcs_cache;

	/** Get the LCS object cache */
	public LcsCache getLcsCache() {
		return lcs_cache;
	}

	/** Cache of timing plans */
	protected final TypeCache<TimingPlan> timing_plans;

	/** Get the timing plan cache */
	public TypeCache<TimingPlan> getTimingPlans() {
		return timing_plans;
	}

	/** Create a new Sonar state */
	public SonarState(Properties props, ExceptionHandler handler)
		throws IOException, ConfigurationError, NoSuchFieldException,
		IllegalAccessException
	{
		super(props, handler);
		roles = new TypeCache<Role>(Role.class, this);
		privileges = new TypeCache<Privilege>(Privilege.class, this);
		users = new TypeCache<User>(User.class, this);
		connections = new TypeCache<Connection>(Connection.class, this);
		system_attributes = new TypeCache<SystemAttribute>(
			SystemAttribute.class, this);
		holidays = new TypeCache<Holiday>(Holiday.class, this);
		graphics = new TypeCache<Graphic>(Graphic.class, this);
		roads = new TypeCache<Road>(Road.class, this);
		road_model = new ProxyListModel<Road>(roads);
		road_model.initialize();
		geo_locs = new TypeCache<GeoLoc>(GeoLoc.class, this);
		alarms = new TypeCache<Alarm>(Alarm.class, this);
		avail_alarm_model = new ProxyListModel<Alarm>(alarms) {
			protected int doProxyAdded(Alarm proxy) {
				if(proxy.getController() == null)
					return super.doProxyAdded(proxy);
				else
					return -1;
			}
		};
		avail_alarm_model.initialize();
		warn_signs = new TypeCache<WarningSign>(WarningSign.class,
			this);
		ramp_meters = new TypeCache<RampMeter>(RampMeter.class, this);
		cam_cache = new CamCache(this);
		con_cache = new ConCache(this);
		det_cache = new DetCache(this);
		dms_cache = new DmsCache(this);
		lcs_cache = new LcsCache(this);
		timing_plans = new TypeCache<TimingPlan>(TimingPlan.class,this);
		// FIXME: this is an ugly hack
		BaseHelper.namespace = getNamespace();
	}

	/** Populate the type caches */
	public void populateCaches() {
		populate(roles);
		populate(privileges);
		populate(users);
		populate(connections);
		populate(system_attributes);
		populate(roads, true);
		populate(geo_locs, true);
		populate(graphics, true);
		cam_cache.populate(this);
		con_cache.populate(this);
		det_cache.populate(this);
		populate(alarms);
		populate(warn_signs);
		populate(ramp_meters);
		ramp_meters.ignoreAttribute("operation");
		dms_cache.populate(this);
		lcs_cache.populate(this);
		populate(timing_plans);
		populate(holidays);
	}

	/** Look up the specified user */
	public User lookupUser(String name) {
		return users.lookupObject(name);
	}

	/** Look up the specified connection */
	public Connection lookupConnection(String name) {
		return connections.lookupObject(name);
	}
}
