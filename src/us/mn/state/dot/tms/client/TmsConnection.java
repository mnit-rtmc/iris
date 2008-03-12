/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
import java.net.URL;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.util.Properties;
import us.mn.state.dot.data.DataFactory;
import us.mn.state.dot.data.HttpDataFactory;
import us.mn.state.dot.data.SystemConfig;
import us.mn.state.dot.data.TmsConfig;
import us.mn.state.dot.tms.client.security.IrisPermission;
import us.mn.state.dot.tms.client.security.IrisUser;
import us.mn.state.dot.tms.client.security.UserManager;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.utils.TMSProxy;

/**
 * The TmsConnection class represents a connection to the TMS server.
 * Classes which use <code>the RemoteObserver</code> interface should listen
 * to the connection so that they can deregister with the remote object when
 * the connection closes.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class TmsConnection {

	/** The desktop used by this connection */
	private final SmartDesktop desktop;

	/** Is the connection open? */
	private boolean connectionOpen = false;

	/** TMSProxy used by this connection */
	protected TMSProxy tms;

	/** Currently login user */
	protected final UserManager userManager;

	/** Create a new traffic data factory */
	static protected DataFactory createDataFactory(Properties props)
		throws MalformedURLException, InstantiationException
	{
		String cfg_url = props.getProperty("datatools.config.url");
		String trafdat_url = props.getProperty("datatools.trafdat.url");
		SystemConfig[] cfgs = new SystemConfig[1];
		cfgs[0] = new TmsConfig("IRIS", new URL(cfg_url));
		return new HttpDataFactory(trafdat_url, cfgs);
	}

	/** Traffic data factory */
	protected final DataFactory factory;

	/** Get the traffic data factory */
	public DataFactory getDataFactory() {
		return factory;
	}

	/** Create a new TmsConnection that is closed */
	public TmsConnection(SmartDesktop desktop, UserManager userManager,
		Properties props) throws MalformedURLException,
		InstantiationException
	{
		this.desktop = desktop;
		this.userManager = userManager;
		factory = createDataFactory(props);
	}

	/** Get the desktop used by the client */
	public SmartDesktop getDesktop() {
		return desktop;
	}

	/** Get the TMS proxy for the connection */
	public TMSProxy getProxy() {
		return tms;
	}

	/** Get the user information for the user who owns the connection */
	public IrisUser getUser() {
		return userManager.getUser();
	}

	/** Is the current user allowed admin privileges */
	public boolean isAdmin() {
		return getUser().hasPermission(IrisPermission.ADMINISTRATOR);
	}

	/** Is the current user allowed to send alerts? */
	public boolean isAlert() {
		return getUser().hasPermission(IrisPermission.ALERT);
	}

	/** Is the current user allowed activate privileges */
	public boolean isActivate() {
		return getUser().hasPermission(IrisPermission.ACTIVATE);
	}

	/** Is the current user allowed tiger privileges */
	public boolean isTiger() {
		return getUser().hasPermission(IrisPermission.TIGER);
	}

	/** Is the connection open? */
	public boolean isOpen() {
		return connectionOpen;
	}

	/** Open the connection */
	public void open(String hostName, String userName) throws IOException,
		NotBoundException
	{
		tms = new TMSProxy(hostName, userName);
		connectionOpen = true;
	}

	/** Close the connection */
	public void close() {
		desktop.closeFrames();
		TMSProxy t = tms;
		if(t != null)
			t.dispose();
		tms = null;
		connectionOpen = false;
	}
}
