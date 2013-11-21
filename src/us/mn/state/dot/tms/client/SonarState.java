/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2013  Minnesota Department of Transportation
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
import java.util.Properties;
import us.mn.state.dot.sonar.Capability;
import us.mn.state.dot.sonar.ConfigurationError;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Privilege;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.Client;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.Alarm;
import us.mn.state.dot.tms.BaseHelper;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmArray;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.Holiday;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentDetail;
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.MapExtent;
import us.mn.state.dot.tms.MeterAction;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.client.camera.CamCache;
import us.mn.state.dot.tms.client.comm.ConCache;
import us.mn.state.dot.tms.client.detector.DetCache;
import us.mn.state.dot.tms.client.dms.DmsCache;
import us.mn.state.dot.tms.client.lcs.LcsCache;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * Holds the state of the SONAR client
 *
 * @author Douglas Lau
 */
public class SonarState extends Client {

	/** Exception handler */
	private final SimpleHandler handler;

	/** Cache of capability proxies */
	private final TypeCache<Capability> capabilities;

	/** Get the capability type cache */
	public TypeCache<Capability> getCapabilities() {
		return capabilities;
	}

	/** Cache of privilege proxies */
	private final TypeCache<Privilege> privileges;

	/** Get the privilege type cache */
	public TypeCache<Privilege> getPrivileges() {
		return privileges;
	}

	/** Cache of role proxies */
	private final TypeCache<Role> roles;

	/** Get the role type cache */
	public TypeCache<Role> getRoles() {
		return roles;
	}

	/** Cache of user proxies */
	private final TypeCache<User> users;

	/** Get the user type cache */
	public TypeCache<User> getUsers() {
		return users;
	}

	/** Cache of connection proxies */
	private final TypeCache<Connection> connections;

	/** Get the connection type cache */
	public TypeCache<Connection> getConnections() {
		return connections;
	}

	/** Cache of system attributes */
	private final TypeCache<SystemAttribute> system_attributes;

	/** Get the system attribute type cache */
	public TypeCache<SystemAttribute> getSystemAttributes() {
		return system_attributes;
	}

	/** Cache of graphic proxies */
	private final TypeCache<Graphic> graphics;

	/** Get the graphic type cache */
	public TypeCache<Graphic> getGraphics() {
		return graphics;
	}

	/** Cache of road proxies */
	private final TypeCache<Road> roads;

	/** Get the road type cache */
	public TypeCache<Road> getRoads() {
		return roads;
	}

	/** Road proxy list model */
	private final ProxyListModel<Road> road_model;

	/** Get the road list model */
	public ProxyListModel<Road> getRoadModel() {
		return road_model;
	}

	/** Cache of geo locations */
	private final TypeCache<GeoLoc> geo_locs;

	/** Get the geo location cache */
	public TypeCache<GeoLoc> getGeoLocs() {
		return geo_locs;
	}

	/** Cache of map extents */
	private final TypeCache<MapExtent> map_extents;

	/** Get the map extent cache */
	public TypeCache<MapExtent> getMapExtents() {
		return map_extents;
	}

	/** Cache of alarms */
	private final TypeCache<Alarm> alarms;

	/** Get the alarm cache */
	public TypeCache<Alarm> getAlarms() {
		return alarms;
	}

	/** Cache of warning signs */
	private final TypeCache<WarningSign> warn_signs;

	/** Get the warning sign cache */
	public TypeCache<WarningSign> getWarningSigns() {
		return warn_signs;
	}

	/** Cache of ramp meters */
	private final TypeCache<RampMeter> ramp_meters;

	/** Get the ramp meter cache */
	public TypeCache<RampMeter> getRampMeters() {
		return ramp_meters;
	}

	/** Cache of controller objects */
	private final ConCache con_cache;

	/** Get the controller object cache */
	public ConCache getConCache() {
		return con_cache;
	}

	/** Cache of camera objects */
	private final CamCache cam_cache;

	/** Get the camera object cache */
	public CamCache getCamCache() {
		return cam_cache;
	}

	/** Cache of detector objects */
	private final DetCache det_cache;

	/** Get the detector object cache */
	public DetCache getDetCache() {
		return det_cache;
	}

	/** Cache of DMS objects */
	private final DmsCache dms_cache;

	/** Get the DMS object cache */
	public DmsCache getDmsCache() {
		return dms_cache;
	}

	/** Cache of LCS objects */
	private final LcsCache lcs_cache;

	/** Get the LCS object cache */
	public LcsCache getLcsCache() {
		return lcs_cache;
	}

	/** Cache of lane markings */
	private final TypeCache<LaneMarking> lane_markings;

	/** Get the lane marking cache */
	public TypeCache<LaneMarking> getLaneMarkings() {
		return lane_markings;
	}

	/** Cache of weather sensors */
	private final TypeCache<WeatherSensor> weather_sensors;

	/** Get the weather sensor cache */
	public TypeCache<WeatherSensor> getWeatherSensors() {
		return weather_sensors;
	}

	/** Cache of gate arm arrays */
	private final TypeCache<GateArmArray> gate_arm_arrays;

	/** Get the gate arm array cache */
	public TypeCache<GateArmArray> getGateArmArrays() {
		return gate_arm_arrays;
	}

	/** Gate arm array proxy list model */
	private final ProxyListModel<GateArmArray> gate_arm_array_model;

	/** Get the gate arm array list model */
	public ProxyListModel<GateArmArray> getGateArmArrayModel() {
		return gate_arm_array_model;
	}

	/** Cache of gate arms */
	private final TypeCache<GateArm> gate_arms;

	/** Get the gate arm cache */
	public TypeCache<GateArm> getGateArms() {
		return gate_arms;
	}

	/** Cache of incident details */
	private final TypeCache<IncidentDetail> inc_details;

	/** Get the incident details object cache */
	public TypeCache<IncidentDetail> getIncidentDetails() {
		return inc_details;
	}

	/** Cache of incidents */
	private final TypeCache<Incident> incidents;

	/** Get the incident object cache */
	public TypeCache<Incident> getIncidents() {
		return incidents;
	}

	/** Cache of holiday proxies */
	private final TypeCache<Holiday> holidays;

	/** Get the holiday type cache */
	public TypeCache<Holiday> getHolidays() {
		return holidays;
	}

	/** Cache of day plans */
	private final TypeCache<DayPlan> day_plans;

	/** Get the day plan cache */
	public TypeCache<DayPlan> getDayPlans() {
		return day_plans;
	}

	/** Day plan proxy list model */
	private final ProxyListModel<DayPlan> day_model;

	/** Get the day list model */
	public ProxyListModel<DayPlan> getDayModel() {
		return day_model;
	}

	/** Cache of plan phases */
	private final TypeCache<PlanPhase> plan_phases;

	/** Get the plan phase cache */
	public TypeCache<PlanPhase> getPlanPhases() {
		return plan_phases;
	}

	/** Plan phase proxy list model */
	private final ProxyListModel<PlanPhase> phase_model;

	/** Get the phase list model */
	public ProxyListModel<PlanPhase> getPhaseModel() {
		return phase_model;
	}

	/** Cache of action plans */
	private final TypeCache<ActionPlan> action_plans;

	/** Get the action plan cache */
	public TypeCache<ActionPlan> getActionPlans() {
		return action_plans;
	}

	/** Cache of time actions */
	private final TypeCache<TimeAction> time_actions;

	/** Get the time action cache */
	public TypeCache<TimeAction> getTimeActions() {
		return time_actions;
	}

	/** Cache of DMS actions */
	private final TypeCache<DmsAction> dms_actions;

	/** Get the DMS action cache */
	public TypeCache<DmsAction> getDmsActions() {
		return dms_actions;
	}

	/** Cache of lane actions */
	private final TypeCache<LaneAction> lane_actions;

	/** Get the lane action cache */
	public TypeCache<LaneAction> getLaneActions() {
		return lane_actions;
	}

	/** Cache of meter actions */
	private final TypeCache<MeterAction> meter_actions;

	/** Get the meter action cache */
	public TypeCache<MeterAction> getMeterActions() {
		return meter_actions;
	}

	/** Create a new Sonar state */
	public SonarState(Properties props, SimpleHandler h)
		throws IOException, ConfigurationError, NoSuchFieldException,
		IllegalAccessException
	{
		super(props, h);
		handler = h;
		capabilities = new TypeCache<Capability>(Capability.class,this);
		privileges = new TypeCache<Privilege>(Privilege.class, this);
		roles = new TypeCache<Role>(Role.class, this);
		users = new TypeCache<User>(User.class, this);
		connections = new TypeCache<Connection>(Connection.class, this);
		system_attributes = new TypeCache<SystemAttribute>(
			SystemAttribute.class, this);
		graphics = new TypeCache<Graphic>(Graphic.class, this);
		roads = new TypeCache<Road>(Road.class, this);
		road_model = new ProxyListModel<Road>(roads);
		road_model.initialize();
		geo_locs = new TypeCache<GeoLoc>(GeoLoc.class, this);
		map_extents = new TypeCache<MapExtent>(MapExtent.class, this);
		inc_details = new TypeCache<IncidentDetail>(
			IncidentDetail.class, this);
		alarms = new TypeCache<Alarm>(Alarm.class, this);
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
		weather_sensors = new TypeCache<WeatherSensor>(
			WeatherSensor.class, this);
		gate_arm_arrays = new TypeCache<GateArmArray>(
			GateArmArray.class, this);
		gate_arm_array_model = new ProxyListModel<GateArmArray>(
			gate_arm_arrays);
		gate_arm_array_model.initialize();
		gate_arms = new TypeCache<GateArm>(GateArm.class, this);
		incidents = new TypeCache<Incident>(Incident.class, this);
		holidays = new TypeCache<Holiday>(Holiday.class, this);
		day_plans = new TypeCache<DayPlan>(DayPlan.class, this);
		day_model = new ProxyListModel<DayPlan>(day_plans);
		day_model.initialize();
		plan_phases = new TypeCache<PlanPhase>(PlanPhase.class, this);
		phase_model = new ProxyListModel<PlanPhase>(plan_phases);
		phase_model.initialize();
		action_plans = new TypeCache<ActionPlan>(ActionPlan.class,this);
		time_actions = new TypeCache<TimeAction>(TimeAction.class,this);
		dms_actions = new TypeCache<DmsAction>(DmsAction.class, this);
		lane_actions = new TypeCache<LaneAction>(LaneAction.class,this);
		meter_actions = new TypeCache<MeterAction>(MeterAction.class,
			this);
		// FIXME: this is an ugly hack
		BaseHelper.namespace = getNamespace();
	}

	/** Logged-in user name */
	private String user_name;

	/** Logged-in user */
	private User user;

	/** Get the logged-in user */
	public User getUser() {
		return user;
	}

	/** Check if the user can read a type */
	public boolean canRead(String tname) {
		return getNamespace().canRead(new Name(tname), user);
	}

	/** Login to the SONAR server */
	public void login(String u, String p) throws SonarException {
		final int n_login = handler.getFailedLoginCount();
		super.login(u, p);
		user_name = u;
		// Wait for up to 20 seconds
		for(int i = 0; i < 200; i++) {
			if(isLoggedIn() ||
			   n_login != handler.getFailedLoginCount())
				return;
			try {
				Thread.sleep(100);
			}
			catch(InterruptedException e) {
				handler.handle(e);
			}
		}
		throw new SonarException("Login timed out");
	}

	/** Populate the specified type cache */
	public void populateReadable(TypeCache tc, boolean do_wait) {
		if(canRead(tc.tname))
			populate(tc, do_wait);
		else
			tc.enumerationComplete();
	}

	/** Populate the specified type cache */
	public void populateReadable(TypeCache tc) {
		populateReadable(tc, false);
	}

	/** Populate the type caches */
	public void populateCaches() {
		populate(capabilities);
		populate(privileges);
		populate(roles);
		populate(users);
		populate(connections, true);
		user = users.lookupObject(user_name);
		populate(system_attributes, true);
		populate(map_extents);
		populate(inc_details);
		populate(roads);
		populate(geo_locs);
		con_cache.populate(this);
		det_cache.populate(this);
		cam_cache.populate(this);
		populateReadable(alarms);
		populateReadable(warn_signs);
		if(canRead(WarningSign.SONAR_TYPE))
			warn_signs.ignoreAttribute("operation");
		populateReadable(ramp_meters);
		if(canRead(RampMeter.SONAR_TYPE)) {
			ramp_meters.ignoreAttribute("operation");
			ramp_meters.ignoreAttribute("rate");
			ramp_meters.ignoreAttribute("queue");
		}
		populateReadable(graphics);
		dms_cache.populate(this);
		lcs_cache.populate(this);
		populateReadable(lane_markings);
		if(canRead(LaneMarking.SONAR_TYPE))
			lane_markings.ignoreAttribute("operation");
		populateReadable(weather_sensors);
		if(canRead(WeatherSensor.SONAR_TYPE)) {
			weather_sensors.ignoreAttribute("operation");
			weather_sensors.ignoreAttribute("stamp");
		}
		populateReadable(gate_arm_arrays);
		if(canRead(GateArmArray.SONAR_TYPE))
			gate_arm_arrays.ignoreAttribute("armState");
		populateReadable(gate_arms);
		if(canRead(GateArm.SONAR_TYPE))
			gate_arms.ignoreAttribute("operation");
		populateReadable(incidents);
		populateReadable(holidays);
		populateReadable(day_plans);
		populateReadable(plan_phases);
		// Populate a second time to resolve nextPhase self-references
		populateReadable(plan_phases);
		populateReadable(action_plans);
		populateReadable(time_actions);
		populateReadable(dms_actions);
		populateReadable(lane_actions);
		populateReadable(meter_actions);
	}

	/** Look up the specified connection */
	private Connection lookupConnection(String name) {
		return connections.lookupObject(name);
	}

	/** Lookup the current connection */
	public Connection lookupConnection() {
		return lookupConnection(getConnection());
	}
}
