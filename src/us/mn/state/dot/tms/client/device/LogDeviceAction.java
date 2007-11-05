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
package us.mn.state.dot.tms.client.device;

import java.io.IOException;
import java.net.URL;
import javax.swing.Action;
import us.mn.state.dot.tms.utils.WebBrowser;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.device.TrafficDeviceProxy;

/**
 * Brings up a browser with the selected device in TESLA
 *
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @author Douglas Lau
 */
public class LogDeviceAction extends TrafficDeviceAction {

	protected final String userName;

	/** Create a new log device action */
	public LogDeviceAction(TrafficDeviceProxy p, TmsConnection c) {
		super(p);
		putValue(Action.NAME, "Log");
		putValue(Action.SHORT_DESCRIPTION, "Log a device failure.");
		putValue(Action.LONG_DESCRIPTION, "Log a failure " +
			"for this device in the TESLA system." );
		userName = c.getUser().getName().toUpperCase();
	}

	/** Actually perform the action */
	protected void do_perform() throws IOException {
		String deviceName = proxy.getId();
		String deviceType = proxy.getProxyType();
		URL url = new URL("http://tms-tomcat:8080" +
			"/tesla/user/DeviceFailureEvent.do?" +
			"command=form" +
			"&deviceType=" + deviceType +
			"&deviceName=" + deviceName);
		WebBrowser.open(url);
	}
}
