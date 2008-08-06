/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * A FilteredMonitorModel is a ProxyListModel filtered to just the
 * VideoMonitors for which the user can set the camera attribute.
 *
 * @author Tim Johnson
 */
public class FilteredMonitorModel extends ProxyListModel<VideoMonitor> {

	/** SONAR User for permission checks */
	protected final User user;

	/** Create a new filtered monitor model */
	public FilteredMonitorModel(User u, SonarState st) {
		super(st.getVideoMonitors());
		user = u;
		initialize();
	}

	/** Create a SONAR name to check for allowed updates */
	protected String createUpdateString(VideoMonitor proxy) {
		return VideoMonitor.SONAR_TYPE + "/" + proxy.getName() +
			"/camera";
	}

	/** Add a new proxy to the list model */
	public void proxyAdded(VideoMonitor proxy) {
		if(user.canUpdate(createUpdateString(proxy)))
			super.proxyAdded(proxy);
	}

	/** Change a proxy in the model */
	public void proxyChanged(VideoMonitor proxy, String attrib) {
		boolean exists = proxies.contains(proxy);
		boolean canUpdate = user.canUpdate(createUpdateString(proxy));
		if(canUpdate && !exists)
			super.proxyAdded(proxy);
		else if(!canUpdate && exists)
			super.proxyRemoved(proxy);
	}
}
