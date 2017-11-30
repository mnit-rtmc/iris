/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
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

	/** Item style */
	private final ItemStyle style;

	/** Selection model for the list model */
	private final ProxyListSelectionModel<T> smodel;

	/** Create a new style list model */
	public StyleListModel(ProxyManager<T> m, String n) {
		super(m.getCache());
		manager = m;
		name = n;
		style = ItemStyle.lookupStyle(name);
		smodel = new ProxyListSelectionModel<T>(this,
			m.getSelectionModel());
	}

	/** Dispose of the list model */
	@Override
	public void dispose() {
		super.dispose();
		for (ListDataListener l: getListDataListeners())
			removeListDataListener(l);
		smodel.dispose();
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(T proxy) {
		return manager.checkStyle(style, proxy);
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
