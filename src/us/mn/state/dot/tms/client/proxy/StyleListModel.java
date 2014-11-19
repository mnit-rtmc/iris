/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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

import javax.swing.event.ListDataListener;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.ItemStyle;

/**
 * A list model for proxy item styles.
 *
 * @author Douglas lau
 */
public class StyleListModel<T extends SonarObject> extends ProxyListModel<T> {

	/** Proxy manager */
	private final ProxyManager<T> manager;

	/** Model name */
	private final String name;

	/** Selection model for the list model */
	private final ProxyListSelectionModel<T> smodel;

	/** Create a new style list model */
	public StyleListModel(ProxyManager<T> m, String n) {
		super(m.getCache());
		manager = m;
		name = n;
		smodel = new ProxyListSelectionModel<T>(this,
			m.getSelectionModel());
	}

	/** Dispose of the list model */
	@Override
	public void dispose() {
		super.dispose();
		for (ListDataListener l: getListDataListeners())
			removeListDataListener(l);
	}

	/** Get the proxy manager */
	public ProxyManager<T> getManager() {
		return manager;
	}

	/** Add a new proxy */
	@Override
	protected int doProxyAdded(T proxy) {
		ItemStyle is = ItemStyle.lookupStyle(name);
		if (manager.checkStyle(is, proxy))
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Get the style name */
	public String getName() {
		return name;
	}

	/** Get the list selection model */
	public ProxyListSelectionModel<T> getSelectionModel() {
		return smodel;
	}

	/** Get the style name */
	@Override
	public String toString() {
		return name;
	}
}
