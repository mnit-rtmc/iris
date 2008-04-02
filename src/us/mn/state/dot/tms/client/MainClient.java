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

import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Properties;
import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.utils.ExceptionDialog;
import us.mn.state.dot.util.HTTPProxySelector;

/**
 * Main entry point for IrisClient.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class MainClient {

	/** Name of default properties file to load */
	static protected final String DEFAULT_PROPERTIES =
		"iris-client.properties";

	/** Application name */
	static protected final String NAME = "IRIS Client";

	/** Socket to prevent more than one instance of IRIS per host */
	static protected ServerSocket socket;

	/** Bind to a socket to prevent multiple instances from running.
	 * Only one instance of the IRIS client is allowed on any machine.
	 * This is enforced by binding a server socket to port 1099.  If that
	 * port is in use an error message will be displayed to the user. */
	static protected void bindSocket() throws IOException {
		socket = new ServerSocket(1099, 0);
	}

	/** Create a URL for the specified property file */
	static protected URL createURL(String prop_file) throws IOException {
		String workingDir = System.getProperty("user.dir");
		File file = new File(workingDir, prop_file);
		if(file.exists())
			return file.toURI().toURL();
		else
			return new URL(prop_file);
	}

	/** Read the IRIS property file */
	static protected Properties readPropertyFile(URL url)
		throws IOException
	{
		Properties props = new Properties();
		props.load(url.openStream());
		return props;
	}

	/** Get the name of the property file to use */
	static protected String getPropertyFile(String[] args) {
		if(args.length > 0)
			return args[0];
		else
			return DEFAULT_PROPERTIES;
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

	/** Create the IRIS client */
	static protected IrisClient createClient(String[] args)
		throws Exception
	{
		URL url = createURL(getPropertyFile(args));
		Properties props = readPropertyFile(url);
		updateSystemProperties(props);
		return new IrisClient(props);
	}

	/** Create the IRIS client with a splash screen */
	static protected IrisClient createClientSplash(String[] args)
		throws Exception
	{
		SplashScreen splash = new SplashScreen();
		splash.setVisible(true);
		try {
			return createClient(args);
		}
		finally {
			splash.setVisible(false);
			splash.dispose();
		}
	}

	/**
	 * Main entry point.
	 *
	 * @param args Arguments passed to the application.
	 */
	static protected void execute(final String[] args) throws Exception {
		bindSocket();
		IrisClient c = createClientSplash(args);
		ExceptionDialog.setOwner(c);
		Scheduler.setHandler(new ExceptionHandler() {
			public boolean handle(Exception e) {
				new ExceptionDialog(e).setVisible(true);
				return true;
			}
		});
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
