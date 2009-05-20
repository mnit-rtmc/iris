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

import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URL;
import java.util.Properties;
import java.util.TimeZone;
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
		sanityChecks();
		IrisClient c = createClientSplash(args);
		ExceptionDialog.setOwner(c);
		Scheduler.setHandler(new SimpleHandler());
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

	/** perform sanity and debug checks */
	static public void sanityChecks() {

		// does the default time zone support DST?
		if (!TimeZone.getDefault().useDaylightTime()) {
			System.err.println("Warning: the default time zone ("+
			TimeZone.getDefault().getDisplayName()+
			") doesn't support DST. Specify the time zone via the command line.");
		}

		// flag if assertions on or off
		if(true) {
			//ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
			boolean assertsEnabled = false;
			assert assertsEnabled = true;    // Intentional side-effect
			String msg = "Assertions are turned " + (assertsEnabled ? "on" : "off") + ".";
			System.err.println(msg);
		}

	}

}
