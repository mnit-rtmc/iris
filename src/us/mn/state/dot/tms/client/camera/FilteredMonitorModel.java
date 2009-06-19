/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * A FilteredMonitorModel is a ProxyListModel filtered to just the
 * VideoMonitors for which the user can set the camera attribute.
 *
 * @author Tim Johnson
 * @author Douglas Lau
 */
public class FilteredMonitorModel extends ProxyListModel<VideoMonitor> {

	/** Create a SONAR name to check for allowed updates */
	static protected Name createAttrName(VideoMonitor proxy) {
		return new Name(proxy, "camera");
	}

	/** SONAR namespace */
	protected final Namespace namespace;

	/** SONAR User for permission checks */
	protected final User user;

	/** Create a new filtered monitor model */
	public FilteredMonitorModel(User u, SonarState st) {
		super(st.getCamCache().getVideoMonitors());
		namespace = st.getNamespace();
		user = u;
		initialize();
	}

	/** Add a new proxy to the list model */
	protected int doProxyAdded(VideoMonitor proxy) {
		if(namespace.canUpdate(user, createAttrName(proxy)))
			return super.doProxyAdded(proxy);
		else
			return -1;
	}
}
