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
import javax.naming.AuthenticationException;
import us.mn.state.dot.sonar.ConfigurationError;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.Client;
import us.mn.state.dot.sonar.client.ShowHandler;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Cabinet;
import us.mn.state.dot.tms.CabinetStyle;
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
	public SonarState(Properties props, ShowHandler handler)
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
		cabinets = new TypeCache<Cabinet>(Cabinet.class);
		comm_links = new TypeCache<CommLink>(CommLink.class);
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
		sign_groups = new TypeCache<SignGroup>(SignGroup.class);
		dms_sign_groups = new TypeCache<DmsSignGroup>(
			DmsSignGroup.class);
		sign_text = new TypeCache<SignText>(SignText.class);
		singleton = this;
	}

	/** Login to the SONAR server */
	public void login(String user, String password)
		throws AuthenticationException
	{
		super.login(user, password);
		populate(roles);
		populate(users);
		populate(connections);
		populate(system_policy);
		populate(roads);
		populate(geo_locs);
		populate(comm_links);
		populate(cabinet_styles);
		populate(cabinets);
		populate(controllers);
		populate(holidays);
		populate(graphics);
		populate(fonts);
		populate(glyphs);
		populate(monitors);
		populate(sign_groups);
		populate(dms_sign_groups);
		populate(sign_text);
	}

	/** Look up the specified user */
	public User lookupUser(String name) {
		Map<String, User> user_map = users.getAll();
		while(true) {
			synchronized(user_map) {
				User u = user_map.get(name);
				if(u != null)
					return u;
			}
			try {
				Thread.sleep(100);
			}
			catch(InterruptedException e) {
				// Do nothing
			}
		}
	}

	/** Lookup the specified road */
	public Road lookupRoad(String name) {
		return roads.getObject(name);
	}

	/** Lookup a geo location */
	public GeoLoc lookupGeoLoc(String name) {
		return geo_locs.getObject(name);
	}

	/** Look up the specified connection */
	public Connection lookupConnection(String name) {
		Map<String, Connection> conn_map = connections.getAll();
		while(true) {
			synchronized(conn_map) {
				Connection c = conn_map.get(name);
				if(c != null)
					return c;
			}
			try {
				Thread.sleep(100);
			}
			catch(InterruptedException e) {
				// Do nothing
			}
		}
	}
}
