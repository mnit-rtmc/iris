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
package us.mn.state.dot.tms.client.proxy;

import java.util.Map;
import us.mn.state.dot.map.Theme;
import us.mn.state.dot.tms.client.TmsSelectionModel;
import us.mn.state.dot.tms.client.security.IrisUser;

/**
 * The DeviceHandler class provides proxies for TrafficDevice objects.  And
 * manages ListModels of proxies.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public interface ProxyHandler {

	/** Get the TMS object class description */
	public String getProxyType();

	/** Get the theme */
	public Theme getTheme();

	/** Refresh listeners are notified whenever the data has been updated */
	public void addRefreshListener(RefreshListener l);

	/** Remove a refresh listener */
	public void removeRefreshListener(RefreshListener l);

	/** Get a Map that contains all proxies. Note: must be synchronized */
	public Map<Object, TmsMapProxy> getProxies();

	/** Get the name of the logged in user */
	public IrisUser getUser();

	/** Get the selection model */
	public TmsSelectionModel getSelectionModel();

	/** Dispose of the proxy handler */
	public void dispose();
}
