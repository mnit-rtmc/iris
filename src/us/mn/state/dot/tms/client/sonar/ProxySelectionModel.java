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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import us.mn.state.dot.sonar.SonarObject;

/**
 * A model for tracking selected proxy objects.
 *
 * @author Douglas Lau
 */
public class ProxySelectionModel<T extends SonarObject> {

	/** Currently selected proxy objects */
	protected Set<T> selected = new HashSet<T>();

	/** The listeners of this model */
	protected final List<ProxySelectionListener<T>> listeners =
		new LinkedList<ProxySelectionListener<T>>();

	/** Add a proxy to the selection */
	public void addSelected(T proxy) {
		if(selected.add(proxy))
			fireSelectionAdded(proxy);
	}

	/** Remove a proxy from the selection */
	public void removeSelected(T proxy) {
		if(selected.remove(proxy))
			fireSelectionRemoved(proxy);
	}

	/** Set a proxy to be a single selection */
	public void setSelected(T proxy) {
		clearSelection();
		addSelected(proxy);
	}

	/** Clear the proxy selection */
	public void clearSelection() {
		for(T proxy: selected)
			fireSelectionRemoved(proxy);
		selected.clear();
	}

	/** Get a list of the selected proxies */
	public List<T> getSelected() {
		return new LinkedList<T>(selected);
	}

	/** Get the count of selected objects */
	public int getSelectedCount() {
		return selected.size();
	}

	/** Add a proxy selection listener to the model */
	public void addProxySelectionListener(ProxySelectionListener<T> l) {
		listeners.add(l);
	}

	/** Remove a proxy selection listener from the model */
	public void removeProxySelectionListener(ProxySelectionListener<T> l) {
		listeners.remove(l);
	}

	/** Fire a selection added event to all listeners */
	protected void fireSelectionAdded(T proxy) {
		for(ProxySelectionListener<T> l: listeners)
			l.selectionAdded(proxy);
	}

	/** Fire a selection removed event to all listeners */
	protected void fireSelectionRemoved(T proxy) {
		for(ProxySelectionListener l: listeners)
			l.selectionRemoved(proxy);
	}

	/** Dispose of the proxy selection model */
	public void dispose() {
		listeners.clear();
		selected.clear();
	}
}
