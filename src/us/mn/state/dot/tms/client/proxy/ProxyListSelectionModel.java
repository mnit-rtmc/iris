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

import java.util.LinkedList;
import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
	protected final ProxyListModel<T> model;

	/** Proxy selection model */
	protected final ProxySelectionModel<T> sel;

	/** The "valueIsAdjusting" crap doesn't work right */
	protected int adjusting = 0;

	/** Create a new proxy list selection model */
	public ProxyListSelectionModel(ProxyListModel<T> m,
		ProxyManager<T> manager)
	{
		model = m;
		sel = manager.getSelectionModel();
		sel.addProxySelectionListener(new ProxySelectionListener<T>() {
			public void selectionAdded(T proxy) {
				if(adjusting > 0)
					return;
				int i = model.getRow(proxy);
				if(i >= 0) {
					adjusting++;
					addSelectionInterval(i, i);
					adjusting--;
				}
			}
			public void selectionRemoved(T proxy) {
				if(adjusting > 0)
					return;
				int i = model.getRow(proxy);
				if(i >= 0) {
					adjusting++;
					removeSelectionInterval(i, i);
					adjusting--;
				}
			}
		});
		addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(adjusting > 0 || e.getValueIsAdjusting())
					return;
				updateProxySelectionModel(e);
			}
		});
	}

	/** Update the proxy selection model from a selection event */
	protected void updateProxySelectionModel(ListSelectionEvent e) {
		updateProxySelectionModel(e.getFirstIndex(), e.getLastIndex());
	}

	/** Update the proxy selection model from a selection event */
	protected void updateProxySelectionModel(int index0, int index1) {
		for(int i = index0; i <= index1; i++) {
			T proxy = model.getProxy(i);
			if(proxy != null) {
				if(isSelectedIndex(i))
					sel.addSelected(proxy);
				else
					sel.removeSelected(proxy);
			}
		}
	}

	/** Insert an interval into the model */
	public void insertIndexInterval(int index, int length, boolean before) {
		adjusting++;
		super.insertIndexInterval(index, length, before);
		// NOTE: if the proxies being added are already selected,
		//       we need to add them to this selection model
		for(int i = index; i < index + length; i++) {
			T proxy = model.getProxy(i);
			if(proxy != null && sel.isSelected(proxy))
				addSelectionInterval(index, index);
		}
		adjusting--;
	}

	/** Remove an interval from the model */
	public void removeIndexInterval(int index0, int index1) {
		// NOTE: other models should not be affected by removing
		//       a proxy from this model
		adjusting++;
		super.removeIndexInterval(index0, index1);
		adjusting--;
	}

	/** Set the selection interval */
	public void setSelectionInterval(int index0, int index1) {
		adjusting++;
		super.setSelectionInterval(index0, index1);
		// NOTE: we need to deselect any selected items not in the
		//       list model.
		for(T proxy: sel.getSelected()) {
			int i = model.getRow(proxy);
			if(i < index0 || i > index1)
				sel.removeSelected(proxy);
		}
		for(int i = index0; i <= index1; i++) {
			T proxy = model.getProxy(i);
			if(proxy != null)
				sel.addSelected(proxy);
		}
		adjusting--;
	}
}
