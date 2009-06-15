/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.security.UserManager;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * The TmsConnection class represents a connection to the TMS server.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class TmsConnection {

	/** The desktop used by this connection */
	private final SmartDesktop desktop;

	/** Currently login user */
	protected final UserManager userManager;

	/** Create a new TmsConnection that is closed */
	public TmsConnection(SmartDesktop desktop, UserManager userManager) {
		this.desktop = desktop;
		this.userManager = userManager;
	}

	/** Get the desktop used by the client */
	public SmartDesktop getDesktop() {
		return desktop;
	}

	/** Get the SONAR state */
	public SonarState getSonarState() {
		return userManager.getSonarState();
	}

	/** Get the user information for the user who owns the connection */
	public User getUser() {
		return userManager.getUser();
	}
}
