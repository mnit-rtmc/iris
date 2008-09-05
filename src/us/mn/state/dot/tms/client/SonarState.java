/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.Client;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Alarm;
import us.mn.state.dot.tms.Cabinet;
import us.mn.state.dot.tms.CabinetStyle;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.Holiday;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.SystemPolicy;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * Holds the state of the SONAR client
 *
 * @author Douglas Lau
 */
public class SonarState extends Client {

	/** FIXME: this is a temporary hack */
	static public SonarState singleton;

	/** Cache of role proxies */
	protected final TypeCache<Role> roles;

	/** Get the role type cache */
	public TypeCache<Role> getRoles() {
		return roles;
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

	/** Cache of system policy proxies */
	protected final TypeCache<SystemPolicy> system_policy;

	/** Get the system policy type cache */
	public TypeCache<SystemPolicy> getSystemPolicy() {
		return system_policy;
	}

	/** Cache of cabinet style proxies */
	protected final TypeCache<CabinetStyle> cabinet_styles;

	/** Get the cabinet style type cache */
	public TypeCache<CabinetStyle> getCabinetStyles() {
		return cabinet_styles;
	}

	/** Cabinet style proxy list model */
	protected final ProxyListModel<CabinetStyle> cab_style_model;

	/** Get the Cabinet Style list model */
	public ProxyListModel<CabinetStyle> getCabinetStyleModel() {
		return cab_style_model;
	}

	/** Cache of cabinet proxies */
	protected final TypeCache<Cabinet> cabinets;

	/** Get the cabinet type cache */
	public TypeCache<Cabinet> getCabinets() {
		return cabinets;
	}

	/** Cache of comm link proxies */
	protected final TypeCache<CommLink> comm_links;

	/** Get the comm link type cache */
	public TypeCache<CommLink> getCommLinks() {
		return comm_links;
	}

	/** Comm link proxy list model */
	protected final ProxyListModel<CommLink> comm_link_model;

	/** Get the CommLink list model */
	public ProxyListModel<CommLink> getCommLinkModel() {
		return comm_link_model;
	}

	/** Cache of controller proxies */
	protected final TypeCache<Controller> controllers;

	/** Get the controller type cache */
	public TypeCache<Controller> getControllers() {
		return controllers;
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

	/** Cache of font proxies */
	protected final TypeCache<Font> fonts;

	/** Get the font type cache */
	public TypeCache<Font> getFonts() {
		return fonts;
	}

	/** Cache of glyph proxies */
	protected final TypeCache<Glyph> glyphs;

	/** Get the glyph type cache */
	public TypeCache<Glyph> getGlyphs() {
		return glyphs;
	}

	/** Cache of video monitor proxies */
	protected final TypeCache<VideoMonitor> monitors;

	/** Get the video monitor type cache */
	public TypeCache<VideoMonitor> getVideoMonitors() {
		return monitors;
	}

	/** VideoMonitor proxy list model */
	protected final ProxyListModel<VideoMonitor> monitor_model;

	/** Get the VideoMonitor list model */
	public ProxyListModel<VideoMonitor> getMonitorModel() {
		return monitor_model;
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

	/** Alarm proxy list model */
	protected final ProxyListModel<Alarm> alarm_model;

	/** Get the alarm list model */
	public ProxyListModel<Alarm> getAlarmModel() {
		return alarm_model;
	}

	/** Available alarm proxy list model */
	protected final ProxyListModel<Alarm> avail_alarm_model;

	/** Get the available alarm list model */
	public ProxyListModel<Alarm> getAvailableAlarms() {
		return avail_alarm_model;
	}

	/** Cache of cameras */
	protected final TypeCache<Camera> cameras;

	/** Get the camera cache */
	public TypeCache<Camera> getCameras() {
		return cameras;
	}

	/** Camera proxy list model */
	protected final ProxyListModel<Camera> camera_model;

	/** Get the camera list model */
	public ProxyListModel<Camera> getCameraModel() {
		return camera_model;
	}

	/** Cache of sign groups */
	protected final TypeCache<SignGroup> sign_groups;

	/** Get the sign group cache */
	public TypeCache<SignGroup> getSignGroups() {
		return sign_groups;
	}

	/** Cache of DMS sign groups */
	protected final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Get the DMS sign group cache */
	public TypeCache<DmsSignGroup> getDmsSignGroups() {
		return dms_sign_groups;
	}

	/** Cache of sign text */
	protected final TypeCache<SignText> sign_text;

	/** Get the sign text cache */
	public TypeCache<SignText> getSignText() {
		return sign_text;
	}

	/** Create a new Sonar state */
	public SonarState(Properties props, ExceptionHandler handler)
		throws IOException, ConfigurationError, NoSuchFieldException,
		IllegalAccessException
	{
		super(props, handler);
		roles = new TypeCache<Role>(Role.class);
		users = new TypeCache<User>(User.class);
		connections = new TypeCache<Connection>(Connection.class);
		system_policy = new TypeCache<SystemPolicy>(SystemPolicy.class);
		cabinet_styles = new TypeCache<CabinetStyle>(
			CabinetStyle.class);
		cab_style_model = new ProxyListModel<CabinetStyle>(
			cabinet_styles);
		cab_style_model.initialize();
		cabinets = new TypeCache<Cabinet>(Cabinet.class);
		comm_links = new TypeCache<CommLink>(CommLink.class);
		comm_link_model = new ProxyListModel<CommLink>(comm_links);
		comm_link_model.initialize();
		controllers = new TypeCache<Controller>(Controller.class);
		holidays = new TypeCache<Holiday>(Holiday.class);
		graphics = new TypeCache<Graphic>(Graphic.class);
		fonts = new TypeCache<Font>(Font.class);
		glyphs = new TypeCache<Glyph>(Glyph.class);
		monitors = new TypeCache<VideoMonitor>(VideoMonitor.class);
		monitor_model = new ProxyListModel<VideoMonitor>(monitors);
		monitor_model.initialize();
		roads = new TypeCache<Road>(Road.class);
		road_model = new ProxyListModel<Road>(roads);
		road_model.initialize();
		geo_locs = new TypeCache<GeoLoc>(GeoLoc.class);
		alarms = new TypeCache<Alarm>(Alarm.class);
		alarm_model = new ProxyListModel<Alarm>(alarms);
		alarm_model.initialize();
		avail_alarm_model = new ProxyListModel<Alarm>(alarms) {
			protected int doProxyAdded(Alarm proxy) {
				if(proxy.getController() == null)
					return super.doProxyAdded(proxy);
				else
					return -1;
			}
		};
		avail_alarm_model.initialize();
		cameras = new TypeCache<Camera>(Camera.class);
		camera_model = new ProxyListModel<Camera>(cameras);
		camera_model.initialize();
		sign_groups = new TypeCache<SignGroup>(SignGroup.class);
		dms_sign_groups = new TypeCache<DmsSignGroup>(
			DmsSignGroup.class);
		sign_text = new TypeCache<SignText>(SignText.class);
		singleton = this;
	}

	/** Login to the SONAR server */
	public void login(String user, String password)
		throws SonarException
	{
		super.login(user, password);
		populate(roles);
		populate(users);
		populate(connections);
		populate(system_policy);
		populate(roads, true);
		populate(geo_locs, true);
		populate(comm_links);
		populate(cabinet_styles);
		populate(cabinets);
		populate(controllers);
		populate(holidays);
		populate(graphics);
		populate(fonts);
		populate(glyphs);
		populate(alarms);
		populate(cameras);
		populate(monitors);
		populate(sign_groups);
		populate(dms_sign_groups);
		populate(sign_text);
	}

	/** Look up the specified user */
	public User lookupUser(String name) {
		return users.getObject(name);
	}

	/** Lookup a geo location */
	public GeoLoc lookupGeoLoc(String name) {
		return geo_locs.getObject(name);
	}

	/** Lookup a controller */
	public Controller lookupController(String name) {
		return controllers.getObject(name);
	}

	/** Lookup a camera */
	public Camera lookupCamera(String name) {
		return cameras.getObject(name);
	}

	/** Look up the specified connection */
	public Connection lookupConnection(String name) {
		return connections.getObject(name);
	}
}
