/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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

import java.awt.Color;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.Properties;
import javax.swing.UIManager;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.util.HTTPProxySelector;
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
		SimpleHandler handler) throws IOException
	{
		String loc = getPropertyFile(args);
		Properties props = PropertyLoader.load(loc);
		updateSystemProperties(props);
		return new IrisClient(props, handler);
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
		tweakLookAndFeel();
		try {
			IrisClient c = createClient(args, handler);
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

	/** Tweak the look and feel */
	static protected void tweakLookAndFeel() {
		UIManager.put("ComboBox.disabledForeground",
			new javax.swing.plaf.ColorUIResource(Color.GRAY));
		UIManager.put("TextField.inactiveForeground",
			 new javax.swing.plaf.ColorUIResource(Color.GRAY));
		UIManager.put("TextArea.inactiveForeground",
			 new javax.swing.plaf.ColorUIResource(Color.GRAY));
	}
}
