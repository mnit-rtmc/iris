/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.Properties;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.utils.HTTPProxySelector;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.PropertyLoader;

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
		HTTPProxySelector ps = new HTTPProxySelector(props);
		if(ps.hasProxies())
			ProxySelector.setDefault(ps);
	}

	/** Create the IRIS client */
	static protected IrisClient createClient(String[] args,
		SimpleHandler handler, UserProperties up) throws IOException
	{
		String loc = getPropertyFile(args);
		Properties props = PropertyLoader.load(loc);
		updateSystemProperties(props);
		district = props.getProperty("district", "tms");
		I18N.initialize(props);
		return new IrisClient(props, handler, up);
	}

	/** Agency district property */
	static protected String district = "tms";

	/** Get the district ID */
	static public String districtId() {
		return district;
	}

	/**
	 * Main IRIS client entry point.
	 *
	 * @param args Arguments passed to the application.
	 */
	static public void main(String[] args) {
		DialogHandler handler = new DialogHandler();
		Scheduler.setHandler(handler);
		checkAssert();
		try {
			final UserProperties user_props = new UserProperties();
			Widgets.init(1f);
			final IrisClient c = createClient(args, handler,
				user_props);
			c.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					quit(c, user_props);
				}
			});
			handler.setOwner(c);
			c.setVisible(true);
		}
		catch(IOException e) {
			handler.handle(e);
		}
	}

	/** Check assertion status */
	static protected void checkAssert() {
		boolean assertsEnabled = false;
		// Intentional assignment side-effect
		assert assertsEnabled = true;
		System.err.println("Assertions are turned " +
			(assertsEnabled ? "on" : "off") + ".");
	}

	/** Quit the client application */
	static private void quit(IrisClient c, UserProperties user_props) {
		user_props.setWindowProperties(c);
		try {
			user_props.write();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
