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
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.Alarm;
import us.mn.state.dot.tms.BaseHelper;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.Holiday;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.MapExtent;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.TimeAction;
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

	/** Cache of map extents */
	protected final TypeCache<MapExtent> map_extents;

	/** Get the map extent cache */
	public TypeCache<MapExtent> getMapExtents() {
		return map_extents;
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

	/** Cache of lane markings */
	protected final TypeCache<LaneMarking> lane_markings;

	/** Get the lane marking cache */
	public TypeCache<LaneMarking> getLaneMarkings() {
		return lane_markings;
	}

	/** Cache of incidents */
	protected final TypeCache<Incident> incidents;

	/** Get the incident object cache */
	public TypeCache<Incident> getIncidents() {
		return incidents;
	}

	/** Cache of action plans */
	protected final TypeCache<ActionPlan> action_plans;

	/** Get the action plan cache */
	public TypeCache<ActionPlan> getActionPlans() {
		return action_plans;
	}

	/** Cache of time actions */
	protected final TypeCache<TimeAction> time_actions;

	/** Get the time action cache */
	public TypeCache<TimeAction> getTimeActions() {
		return time_actions;
	}

	/** Cache of DMS actions */
	protected final TypeCache<DmsAction> dms_actions;

	/** Get the DMS action cache */
	public TypeCache<DmsAction> getDmsActions() {
		return dms_actions;
	}

	/** Cache of lane actions */
	protected final TypeCache<LaneAction> lane_actions;

	/** Get the lane action cache */
	public TypeCache<LaneAction> getLaneActions() {
		return lane_actions;
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
		map_extents = new TypeCache<MapExtent>(MapExtent.class, this);
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
		lane_markings = new TypeCache<LaneMarking>(LaneMarking.class,
			this);
		incidents = new TypeCache<Incident>(Incident.class, this);
		action_plans = new TypeCache<ActionPlan>(ActionPlan.class,this);
		time_actions = new TypeCache<TimeAction>(TimeAction.class,this);
		dms_actions = new TypeCache<DmsAction>(DmsAction.class, this);
		lane_actions = new TypeCache<LaneAction>(LaneAction.class,this);
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
		populate(map_extents);
		populate(graphics, true);
		con_cache.populate(this);
		det_cache.populate(this);
		cam_cache.populate(this);
		populate(alarms);
		populate(warn_signs);
		populate(ramp_meters);
		ramp_meters.ignoreAttribute("operation");
		dms_cache.populate(this);
		lcs_cache.populate(this);
		populate(lane_markings);
		populate(incidents);
		populate(action_plans);
		populate(time_actions);
		populate(dms_actions);
		populate(lane_actions);
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
