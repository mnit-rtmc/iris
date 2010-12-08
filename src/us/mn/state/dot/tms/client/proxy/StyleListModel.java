/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
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

import javax.swing.Icon;
import javax.swing.event.ListDataListener;
import us.mn.state.dot.sonar.SonarObject;

/**
 * A list model for device styles.
 *
 * @author Douglas lau
 */
public class StyleListModel<T extends SonarObject> extends ProxyListModel<T> {

	/** Proxy manager */
	protected final ProxyManager<T> manager;

	/** Model name */
	protected final String name;

	/** Legend icon */
	protected final Icon legend;

	/** Selection model for the list model */
	protected final ProxyListSelectionModel<T> smodel;

	/** Create a new style list model */
	public StyleListModel(ProxyManager<T> m, String n, Icon l) {
		super(m.getCache());
		manager = m;
		name = n;
		legend = l;
		smodel = new ProxyListSelectionModel<T>(this,
			m.getSelectionModel());
	}

	/** Dispose of the list model */
	public void dispose() {
		super.dispose();
		for(ListDataListener l: getListDataListeners())
			removeListDataListener(l);
	}

	/** Get the proxy manager */
	public ProxyManager<T> getManager() {
		return manager;
	}

	/** Add a new proxy */
	protected int doProxyAdded(T proxy) {
		if(manager.checkStyle(name, proxy))
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Get the style name */
	public String getName() {
		return name;
	}

	/** Get the style legend */
	public Icon getLegend() {
		return legend;
	}

	/** Get the list selection model */
	public ProxyListSelectionModel<T> getSelectionModel() {
		return smodel;
	}

	/** Get the style name */
	public String toString() {
		return name;
	}
}
