/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Properties;

import us.mn.state.dot.tms.utils.ExceptionDialog;
import us.mn.state.dot.util.HTTPProxySelector;

/**
 * Main entry point for IrisClient.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class Main {

	/** Name of default properties file to load */
	static protected final String DEFAULT_PROPERTIES =
		"iris-client.properties";

	/** Application name */
	static protected final String NAME = "IRIS Client";

	/** Socket to prevent more than one instance of IRIS per host */
	static protected ServerSocket socket;

	/** Splash screen to show while client starts up */
	static protected SplashScreen splashScreen;

	/** Bind to a socket to prevent multiple instances from running.
	 * Only one instance of the IRIS client is allowed on any machine.
	 * This is enforced by binding a server socket to port 1099.  If that
	 * port is in use an error message will be displayed to the user. */
	static protected void bindSocket() throws IOException {
		socket = new ServerSocket(1099, 0);
	}

	/** Show the startup splash screen */
	static protected void showSplashScreen() {
		splashScreen = new SplashScreen();
		splashScreen.setVisible(true);
	}

	/** Hide the startup splash screen */
	static protected void hideSplashScreen() {
		splashScreen.setVisible(false);
		splashScreen.dispose();
		splashScreen = null;
	}

	/** Read the IRIS property file */
	static protected Properties readPropertyFile(String prop_file)
		throws IOException
	{
		String workingDir = System.getProperty("user.dir");
		File file = new File(workingDir, prop_file);
		URL url = null;
		if(file.exists())
			url = file.toURL();
		else
			url = new URL(prop_file);
		Properties props = new Properties();
		props.load(url.openStream());
		return props;
	}

	/** Set a system property if it's defined in a property set */
	static protected void setSystemProperty(String name, Properties props) {
		String value = props.getProperty(name);
		if(value != null)
			System.setProperty(name, value);
	}

	/** Update the system properties with the given property set */
	static protected void updateSystemProperties(Properties props) {
		setSystemProperty("mail.smtp.host", props);
		setSystemProperty("email_sender", props);
		setSystemProperty("email_recipient", props);
		ProxySelector.setDefault(new HTTPProxySelector(props));
	}

	/** Get the name of the property file to use */
	static protected String getPropertyFile(String[] args) {
		if(args.length > 0)
			return args[0];
		else
			return DEFAULT_PROPERTIES;
	}

	/** Create the IRIS client */
	static protected IrisClient createClient(String[] args)
		throws Exception
	{
		Properties props = readPropertyFile(getPropertyFile(args));
		updateSystemProperties(props);
		return new IrisClient(props);
	}

	/**
	 * Main entry point.
	 *
	 * @param args Arguments passed to the application.
	 */
	static protected void execute(final String[] args) throws Exception {
		bindSocket();
		IrisClient c;
		showSplashScreen();
		try {
			c = createClient(args);
		}
		finally {
			hideSplashScreen();
		}
		ExceptionDialog.setOwner(c);
		c.setVisible(true);
	}

	/**
	 * Main IRIS client entry point.
	 *
	 * @param args Arguments passed to the application.
	 */
	static public void main(String[] args) {
		try {
			execute(args);
		}
		catch(Exception e) {
			new ExceptionDialog(e).setVisible(true);
		}
	}
}
