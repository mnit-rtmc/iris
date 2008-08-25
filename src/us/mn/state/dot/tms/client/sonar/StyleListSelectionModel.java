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
package us.mn.state.dot.tms.client.sonar;

import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import us.mn.state.dot.sonar.SonarObject;

/**
 * The style list selection model synchronized the selection with a proxy
 * selection model.
 *
 * @author Douglas lau
 */
public class StyleListSelectionModel<T extends SonarObject>
	extends DefaultListSelectionModel
{
	/** Style list model */
	protected final StyleListModel<T> model;

	/** Proxy selection model */
	protected final ProxySelectionModel<T> sel;

	/** Flag to determine if an event was initiated by this model */
	protected boolean initiated = false;

	/** Create a new proxy list selection model */
	public StyleListSelectionModel(StyleListModel<T> m,
		ProxyManager<T> manager)
	{
		model = m;
		sel = manager.getSelectionModel();
		sel.addProxySelectionListener(new ProxySelectionListener<T>() {
			public void selectionAdded(T proxy) {
				// FIXME
				// find the index of the proxy
				// call addSelectionInterval
			}
			public void selectionRemoved(T proxy) {
				// FIXME
				// find the index of the proxy
				// call removeSelectionInterval
			}
		});
		addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting())
					return;
				updateProxySelectionModel(e);
			}
		});
	}

	/** Update the proxy selection model from a selection event */
	protected void updateProxySelectionModel(ListSelectionEvent e) {
		for(int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
			T proxy = model.getProxy(i);
			if(isSelectedIndex(i))
				sel.addSelected(proxy);
			else
				sel.removeSelected(proxy);
		}
	}
}
