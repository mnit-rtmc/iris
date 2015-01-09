/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2015  Minnesota Department of Transportation
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
import java.util.LinkedList;
import java.util.Set;
import us.mn.state.dot.sonar.SonarObject;

/**
 * A model for tracking selected proxy objects.
 *
 * @author Douglas Lau
 */
public class ProxySelectionModel<T extends SonarObject> {

	/** Currently selected proxy objects */
	private final Set<T> selected = new HashSet<T>();

	/** The listeners of this model */
	private final LinkedList<ProxySelectionListener> lsnrs =
		new LinkedList<ProxySelectionListener>();

	/** Flag to allow multiple selection */
	private boolean allow_multiple = false;

	/** Set the allow multiple selection flag */
	public void setAllowMultiple(boolean m) {
		allow_multiple = m;
		if (selected.size() > 1)
			clearSelection();
	}

	/** Get the allow multiple selection flag */
	public boolean getAllowMultiple() {
		return allow_multiple;
	}

	/** Add a proxy to the selection */
	public void addSelected(T proxy) {
		if (selected.size() > 0 && !allow_multiple)
			setSelected(proxy);
		else if (selected.add(proxy))
			fireSelectionChanged();
	}

	/** Remove a proxy from the selection */
	public void removeSelected(T proxy) {
		if (selected.remove(proxy))
			fireSelectionChanged();
	}

	/** Set a proxy to be a single selection */
	public void setSelected(T proxy) {
		selected.clear();
		selected.add(proxy);
		fireSelectionChanged();
	}

	/** Set a list of proxies to be a selection */
	public void setSelected(Set<T> proxies) {
		selected.clear();
		selected.addAll(proxies);
		fireSelectionChanged();
	}

	/** Clear the proxy selection */
	public void clearSelection() {
		selected.clear();
		fireSelectionChanged();
	}

	/** Get a list of the selected proxies */
	public Set<T> getSelected() {
		return new HashSet<T>(selected);
	}

	/** Test if a proxy is selected */
	public boolean isSelected(T proxy) {
		return selected.contains(proxy);
	}

	/** Get the count of selected objects */
	public int getSelectedCount() {
		return selected.size();
	}

	/** Get a single selected proxy (if only one is selected) */
	public T getSingleSelection() {
		if (selected.size() == 1) {
			for (T proxy: selected)
				return proxy;
		}
		return null;
	}

	/** Add a proxy selection listener to the model */
	public void addProxySelectionListener(ProxySelectionListener l) {
		lsnrs.add(l);
	}

	/** Remove a proxy selection listener from the model */
	public void removeProxySelectionListener(ProxySelectionListener l) {
		lsnrs.remove(l);
	}

	/** Fire a selection added event to all listeners */
	private void fireSelectionChanged() {
		for (ProxySelectionListener l: lsnrs)
			l.selectionChanged();
	}

	/** Dispose of the proxy selection model */
	public void dispose() {
		lsnrs.clear();
		selected.clear();
	}
}
