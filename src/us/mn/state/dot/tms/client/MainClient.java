/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import java.net.ProxySelector;
import java.util.Properties;
import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.sched.Scheduler;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;
import us.mn.state.dot.tms.utils.HttpProxySelector;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.PropertyLoader;

/**
 * Main entry point for IrisClient.
 *
 * @author Douglas Lau
 */
public class MainClient {

	/** Name of default properties file to load */
	static private final String DEFAULT_PROPERTIES =
		"iris-client.properties";

	/** Get the name of the property file to use */
	static private String getPropertyFile(String[] args) {
		return (args.length > 0) ? args[0] : DEFAULT_PROPERTIES;
	}

	/** Main IRIS client entry point.
	 * @param args Arguments passed to the application. */
	static public void main(final String[] args) {
		checkAssert();
		final String loc = getPropertyFile(args);
		runSwing(new Runnable() {
			public void run() {
				mainSwing(loc);
			}
		});
	}

	/** Check assertion status */
	static private void checkAssert() {
		boolean assertsEnabled = false;
		// Intentional assignment side-effect
		assert assertsEnabled = true;
		System.err.println("Assertions are turned " +
			(assertsEnabled ? "on" : "off") + ".");
	}

	/** Exception handler */
	static private DialogHandler handler;

	/** Get the exception handler */
	static public ExceptionHandler getHandler() {
		return handler;
	}

	/** Main IRIS client entry point for swing */
	static private void mainSwing(String loc) {
		handler = new DialogHandler();
		Scheduler.setHandler(handler);
		try {
			IrisClient c = createClient(loc, handler);
			handler.setOwner(c);
			c.setVisible(true);
		}
		catch (IOException e) {
			handler.handle(e);
		}
	}

	/** Create the IRIS client */
	static private IrisClient createClient(String loc,
		ExceptionHandler handler) throws IOException
	{
		Properties props = UserProperty.load(PropertyLoader.load(loc));
		updateProxySelector(props);
		I18N.initialize(props);
		return new IrisClient(props, handler);
	}

	/** Update the proxy selector with the given property set */
	static private void updateProxySelector(Properties props) {
		HttpProxySelector ps = new HttpProxySelector(props);
		if (ps.hasProxies())
			ProxySelector.setDefault(ps);
	}
}
