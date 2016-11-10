/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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

import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultListSelectionModel;
import us.mn.state.dot.sonar.SonarObject;

/**
 * The proxy list selection model synchronizes the selection with a proxy
 * selection model.
 *
 * @author Douglas lau
 */
public class ProxyListSelectionModel<T extends SonarObject>
	extends DefaultListSelectionModel
{
	/** Proxy list model */
	private final ProxyListModel<T> model;

	/** Proxy selection model */
	private final ProxySelectionModel<T> sel_mdl;

	/** Listener for proxy selection events */
	private final ProxySelectionListener listener =
		new ProxySelectionListener()
	{
		public void selectionChanged() {
			if (adjusting == 0)
				doSelectionChanged();
		}
	};

	/** The "valueIsAdjusting" crap doesn't work */
	private int adjusting = 0;

	/** Update the selection model when the selection is changed */
	private void doSelectionChanged() {
		adjusting++;
		clearSelection();
		for (T proxy : sel_mdl.getSelected()) {
			int i = model.getIndex(proxy);
			if (i >= 0)
				super.addSelectionInterval(i, i);
		}
		adjusting--;
	}

	/** Create a new proxy list selection model */
	public ProxyListSelectionModel(ProxyListModel<T> m,
		ProxySelectionModel<T> s)
	{
		model = m;
		sel_mdl = s;
		if (!sel_mdl.getAllowMultiple())
			setSelectionMode(SINGLE_SELECTION);
		sel_mdl.addProxySelectionListener(listener);
	}

	/** Dispose of the proxy list selection model */
	public void dispose() {
		sel_mdl.removeProxySelectionListener(listener);
	}

	/** Insert an interval into the model */
	@Override
	public void insertIndexInterval(int index, int length, boolean before) {
		super.insertIndexInterval(index, length, before);
		// NOTE: if the proxies being added are already selected,
		//       we need to add them to this selection model
		for (int i = index; i < index + length; i++) {
			T proxy = model.getProxy(i);
			if (proxy != null && sel_mdl.isSelected(proxy))
				super.addSelectionInterval(i, i);
		}
	}

	/** Add a selection interval */
	@Override
	public void addSelectionInterval(int index0, int index1) {
		super.addSelectionInterval(index0, index1);
		Set<T> proxies = sel_mdl.getSelected();
		for (int i = index0; i <= index1; i++) {
			T proxy = model.getProxy(i);
			if (proxy != null)
				proxies.add(proxy);
		}
		setSelected(proxies);
	}

	/** Remove a selection interval */
	@Override
	public void removeSelectionInterval(int index0, int index1) {
		super.removeSelectionInterval(index0, index1);
		Set<T> proxies = sel_mdl.getSelected();
		for (int i = index0; i <= index1; i++) {
			T proxy = model.getProxy(i);
			if (proxy != null)
				proxies.remove(proxy);
		}
		setSelected(proxies);
	}

	/** Set the selection interval */
	@Override
	public void setSelectionInterval(int index0, int index1) {
		super.setSelectionInterval(index0, index1);
		HashSet<T> proxies = new HashSet<T>();
		for (int i = index0; i <= index1; i++) {
			T proxy = model.getProxy(i);
			if (proxy != null)
				proxies.add(proxy);
		}
		setSelected(proxies);
	}

	/** Set the selected proxies */
	private void setSelected(Set<T> proxies) {
		adjusting++;
		sel_mdl.setSelected(proxies);
		adjusting--;
	}
}
