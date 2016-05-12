/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
 * Copyright (C) 2015  Iteris Inc.
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
import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.sonar.Capability;
import us.mn.state.dot.sonar.ConfigurationError;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Privilege;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.Client;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.Alarm;
import us.mn.state.dot.tms.BaseHelper;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.BeaconAction;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmArray;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.Holiday;
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.MapExtent;
import us.mn.state.dot.tms.MeterAction;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.TagReader;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TollZone;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.Word;
import us.mn.state.dot.tms.client.camera.CamCache;
import us.mn.state.dot.tms.client.comm.ConCache;
import us.mn.state.dot.tms.client.detector.DetCache;
import us.mn.state.dot.tms.client.dms.DmsCache;
import us.mn.state.dot.tms.client.incident.IncCache;
import us.mn.state.dot.tms.client.lcs.LcsCache;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * Holds the state of the SONAR client
 *
 * @author Douglas Lau
 */
public class SonarState extends Client {

	/** Exception handler */
	private final ExceptionHandler handler;

	/** Cache of capability proxies */
	private final TypeCache<Capability> capabilities =
		new TypeCache<Capability>(Capability.class, this);

	/** Get the capability type cache */
	public TypeCache<Capability> getCapabilities() {
		return capabilities;
	}

	/** Cache of privilege proxies */
	private final TypeCache<Privilege> privileges =
		new TypeCache<Privilege>(Privilege.class, this);

	/** Get the privilege type cache */
	public TypeCache<Privilege> getPrivileges() {
		return privileges;
	}

	/** Cache of role proxies */
	private final TypeCache<Role> roles =
		new TypeCache<Role>(Role.class, this);

	/** Get the role type cache */
	public TypeCache<Role> getRoles() {
		return roles;
	}

	/** Cache of user proxies */
	private final TypeCache<User> users =
		new TypeCache<User>(User.class, this);

	/** Get the user type cache */
	public TypeCache<User> getUsers() {
		return users;
	}

	/** Cache of connection proxies */
	private final TypeCache<Connection> connections =
		new TypeCache<Connection>(Connection.class, this);

	/** Get the connection type cache */
	public TypeCache<Connection> getConnections() {
		return connections;
	}

	/** Cache of system attributes */
	private final TypeCache<SystemAttribute> system_attributes =
		new TypeCache<SystemAttribute>(SystemAttribute.class, this);

	/** Get the system attribute type cache */
	public TypeCache<SystemAttribute> getSystemAttributes() {
		return system_attributes;
	}

	/** Cache of graphic proxies */
	private final TypeCache<Graphic> graphics =
		new TypeCache<Graphic>(Graphic.class, this);

	/** Get the graphic type cache */
	public TypeCache<Graphic> getGraphics() {
		return graphics;
	}

	/** Cache of road proxies */
	private final TypeCache<Road> roads =
		new TypeCache<Road>(Road.class, this);

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
	private final TypeCache<GeoLoc> geo_locs =
		new TypeCache<GeoLoc>(GeoLoc.class, this);

	/** Get the geo location cache */
	public TypeCache<GeoLoc> getGeoLocs() {
		return geo_locs;
	}

	/** Cache of map extents */
	private final TypeCache<MapExtent> map_extents =
		new TypeCache<MapExtent>(MapExtent.class, this);

	/** Get the map extent cache */
	public TypeCache<MapExtent> getMapExtents() {
		return map_extents;
	}

	/** Cache of alarms */
	private final TypeCache<Alarm> alarms =
		new TypeCache<Alarm>(Alarm.class, this);

	/** Get the alarm cache */
	public TypeCache<Alarm> getAlarms() {
		return alarms;
	}

	/** Cache of beacons */
	private final TypeCache<Beacon> beacons =
		new TypeCache<Beacon>(Beacon.class, this);

	/** Get the beacon cache */
	public TypeCache<Beacon> getBeacons() {
		return beacons;
	}

	/** Beacon proxy list model */
	private final ProxyListModel<Beacon> beacon_model;

	/** Get the beacon list model */
	public ProxyListModel<Beacon> getBeaconModel() {
		return beacon_model;
	}

	/** Cache of ramp meters */
	private final TypeCache<RampMeter> ramp_meters =
		new TypeCache<RampMeter>(RampMeter.class, this);

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

	/** Cache of incident objects */
	private final IncCache inc_cache;

	/** Get the Incident cache */
	public IncCache getIncCache() {
		return inc_cache;
	}

	/** Cache of LCS objects */
	private final LcsCache lcs_cache;

	/** Get the LCS object cache */
	public LcsCache getLcsCache() {
		return lcs_cache;
	}

	/** Cache of lane markings */
	private final TypeCache<LaneMarking> lane_markings =
		new TypeCache<LaneMarking>(LaneMarking.class, this);

	/** Get the lane marking cache */
	public TypeCache<LaneMarking> getLaneMarkings() {
		return lane_markings;
	}

	/** Cache of weather sensors */
	private final TypeCache<WeatherSensor> weather_sensors =
		new TypeCache<WeatherSensor>(WeatherSensor.class, this);

	/** Get the weather sensor cache */
	public TypeCache<WeatherSensor> getWeatherSensors() {
		return weather_sensors;
	}

	/** Cache of tag readers */
	private final TypeCache<TagReader> tag_readers =
		new TypeCache<TagReader>(TagReader.class, this);

	/** Get the tag reader cache */
	public TypeCache<TagReader> getTagReaders() {
		return tag_readers;
	}

	/** Cache of toll zones */
	private final TypeCache<TollZone> toll_zones =
		new TypeCache<TollZone>(TollZone.class, this);

	/** Get the toll zone cache */
	public TypeCache<TollZone> getTollZones() {
		return toll_zones;
	}

	/** Toll zone proxy list model */
	private final ProxyListModel<TollZone> toll_zone_model;

	/** Get the toll zone list model */
	public ProxyListModel<TollZone> getTollZoneModel() {
		return toll_zone_model;
	}

	/** Cache of gate arm arrays */
	private final TypeCache<GateArmArray> gate_arm_arrays =
		new TypeCache<GateArmArray>(GateArmArray.class, this);

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
	private final TypeCache<GateArm> gate_arms =
		new TypeCache<GateArm>(GateArm.class, this);

	/** Get the gate arm cache */
	public TypeCache<GateArm> getGateArms() {
		return gate_arms;
	}

	/** Cache of holiday proxies */
	private final TypeCache<Holiday> holidays =
		new TypeCache<Holiday>(Holiday.class, this);

	/** Get the holiday type cache */
	public TypeCache<Holiday> getHolidays() {
		return holidays;
	}

	/** Cache of day plans */
	private final TypeCache<DayPlan> day_plans =
		new TypeCache<DayPlan>(DayPlan.class, this);

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
	private final TypeCache<PlanPhase> plan_phases =
		new TypeCache<PlanPhase>(PlanPhase.class, this);

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
	private final TypeCache<ActionPlan> action_plans =
		new TypeCache<ActionPlan>(ActionPlan.class, this);

	/** Get the action plan cache */
	public TypeCache<ActionPlan> getActionPlans() {
		return action_plans;
	}

	/** Cache of time actions */
	private final TypeCache<TimeAction> time_actions =
		new TypeCache<TimeAction>(TimeAction.class, this);

	/** Get the time action cache */
	public TypeCache<TimeAction> getTimeActions() {
		return time_actions;
	}

	/** Cache of DMS actions */
	private final TypeCache<DmsAction> dms_actions =
		new TypeCache<DmsAction>(DmsAction.class, this);

	/** Get the DMS action cache */
	public TypeCache<DmsAction> getDmsActions() {
		return dms_actions;
	}

	/** Cache of beacon actions */
	private final TypeCache<BeaconAction> beacon_actions =
		new TypeCache<BeaconAction>(BeaconAction.class, this);

	/** Get the beacon action cache */
	public TypeCache<BeaconAction> getBeaconActions() {
		return beacon_actions;
	}

	/** Cache of lane actions */
	private final TypeCache<LaneAction> lane_actions =
		new TypeCache<LaneAction>(LaneAction.class, this);

	/** Get the lane action cache */
	public TypeCache<LaneAction> getLaneActions() {
		return lane_actions;
	}

	/** Cache of meter actions */
	private final TypeCache<MeterAction> meter_actions =
		new TypeCache<MeterAction>(MeterAction.class, this);

	/** Get the meter action cache */
	public TypeCache<MeterAction> getMeterActions() {
		return meter_actions;
	}

	/** Cache of words */
	private final TypeCache<Word> words = 
		new TypeCache<Word>(Word.class, this);

	/** Get the weather sensor cache */
	public TypeCache<Word> getWords() {
		return words;
	}

	/** Create a new Sonar state */
	public SonarState(Properties props, ExceptionHandler h)
		throws IOException, ConfigurationError, NoSuchFieldException,
		IllegalAccessException
	{
		super(props, h);
		handler = h;
		road_model = new ProxyListModel<Road>(roads);
		road_model.initialize();
		toll_zone_model = new ProxyListModel<TollZone>(toll_zones);
		toll_zone_model.initialize();
		cam_cache = new CamCache(this);
		con_cache = new ConCache(this);
		det_cache = new DetCache(this);
		dms_cache = new DmsCache(this);
		inc_cache = new IncCache(this);
		lcs_cache = new LcsCache(this);
		gate_arm_array_model = new ProxyListModel<GateArmArray>(
			gate_arm_arrays);
		gate_arm_array_model.initialize();
		day_model = new ProxyListModel<DayPlan>(day_plans);
		day_model.initialize();
		phase_model = new ProxyListModel<PlanPhase>(plan_phases);
		phase_model.initialize();
		beacon_model = new ProxyListModel<Beacon>(beacons);
		beacon_model.initialize();
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
	@Override
	public boolean login(String u, String p) throws SonarException {
		boolean l = super.login(u, p);
		user_name = l ? u : null;
		return l;
	}

	/** Populate the specified type cache */
	public void populateReadable(TypeCache<? extends SonarObject> tc,
		boolean do_wait)
	{
		if (canRead(tc.tname))
			populate(tc, do_wait);
		else
			tc.enumerationComplete();
	}

	/** Populate the specified type cache */
	public void populateReadable(TypeCache<? extends SonarObject> tc) {
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
		populate(map_extents, true);
		populate(roads);
		populate(geo_locs);
		populateReadable(words);
		con_cache.populate(this);
		det_cache.populate(this);
		cam_cache.populate(this);
		populateReadable(toll_zones);
		populateReadable(alarms);
		populateReadable(beacons);
		if (canRead(Beacon.SONAR_TYPE))
			beacons.ignoreAttribute("operation");
		populateReadable(ramp_meters);
		if (canRead(RampMeter.SONAR_TYPE)) {
			ramp_meters.ignoreAttribute("operation");
			ramp_meters.ignoreAttribute("rate");
			ramp_meters.ignoreAttribute("queue");
		}
		populateReadable(graphics);
		dms_cache.populate(this);
		inc_cache.populate(this);
		lcs_cache.populate(this);
		populateReadable(lane_markings);
		if (canRead(LaneMarking.SONAR_TYPE))
			lane_markings.ignoreAttribute("operation");
		populateReadable(weather_sensors);
		if (canRead(WeatherSensor.SONAR_TYPE)) {
			weather_sensors.ignoreAttribute("operation");
			weather_sensors.ignoreAttribute("stamp");
		}
		populateReadable(tag_readers);
		if (canRead(TagReader.SONAR_TYPE))
			tag_readers.ignoreAttribute("operation");
		populateReadable(gate_arm_arrays);
		if (canRead(GateArmArray.SONAR_TYPE))
			gate_arm_arrays.ignoreAttribute("armState");
		populateReadable(gate_arms);
		if (canRead(GateArm.SONAR_TYPE))
			gate_arms.ignoreAttribute("operation");
		populateReadable(holidays);
		populateReadable(day_plans);
		populateReadable(plan_phases);
		// Populate a second time to resolve nextPhase self-references
		populateReadable(plan_phases);
		populateReadable(action_plans);
		populateReadable(time_actions);
		populateReadable(dms_actions);
		populateReadable(beacon_actions);
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
