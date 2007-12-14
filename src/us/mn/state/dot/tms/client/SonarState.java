/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.Holiday;
import us.mn.state.dot.tms.SystemPolicy;
import us.mn.state.dot.tms.VideoMonitor;

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
		holidays = new TypeCache<Holiday>(Holiday.class);
		graphics = new TypeCache<Graphic>(Graphic.class);
		fonts = new TypeCache<Font>(Font.class);
		glyphs = new TypeCache<Glyph>(Glyph.class);
		monitors = new TypeCache<VideoMonitor>(VideoMonitor.class);
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
		populate(holidays);
		populate(graphics);
		populate(fonts);
		populate(glyphs);
		populate(monitors);
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
}
