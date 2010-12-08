/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;

/**
 * The r_node list selection model synchronizes the selection with a proxy
 * selection model.  This class is similar to ProxyListSelectionModel.
 *
 * @author Douglas lau
 */
public class R_NodeListSelectionModel extends DefaultListSelectionModel {

	/** R_Node list model */
	protected final R_NodeListModel model;

	/** Proxy selection model */
	protected final ProxySelectionModel<R_Node> sel;

	/** The "valueIsAdjusting" crap doesn't work right */
	protected int adjusting = 0;

	/** Listener for proxy selection events */
	protected final ProxySelectionListener<R_Node> listener =
		new ProxySelectionListener<R_Node> ()
	{
		public void selectionAdded(R_Node proxy) {
			if(adjusting == 0)
				doSelectionAdded(proxy);
		}
		public void selectionRemoved(R_Node proxy) {
			if(adjusting == 0)
				doSelectionRemoved(proxy);
		}
	};

	/** Update the selection model when a selection is added */
	protected void doSelectionAdded(R_Node proxy) {
		int i = model.getRow(proxy);
		if(i >= 0) {
			adjusting++;
			addSelectionInterval(i, i);
			adjusting--;
		}
	}

	/** Update the selection model when a selection is removed */
	protected void doSelectionRemoved(R_Node proxy) {
		int i = model.getRow(proxy);
		if(i >= 0) {
			adjusting++;
			removeSelectionInterval(i, i);
			adjusting--;
		}
	}

	/** List selection listener */
	protected final ListSelectionListener sel_listener =
		new ListSelectionListener()
	{
		public void valueChanged(ListSelectionEvent e) {
			if(adjusting > 0 || e.getValueIsAdjusting())
				return;
			updateProxySelectionModel(e);
		}
	};

	/** Create a new r_node list selection model */
	public R_NodeListSelectionModel(R_NodeListModel m,
		ProxySelectionModel<R_Node> s)
	{
		model = m;
		sel = s;
		for(R_Node n: sel.getSelected())
			doSelectionAdded(n);
		sel.addProxySelectionListener(listener);
		addListSelectionListener(sel_listener);
	}

	/** Dispose of the model */
	public void dispose() {
		removeListSelectionListener(sel_listener);
		sel.removeProxySelectionListener(listener);
	}

	/** Update the proxy selection model from a selection event */
	protected void updateProxySelectionModel(ListSelectionEvent e) {
		updateProxySelectionModel(e.getFirstIndex(), e.getLastIndex());
	}

	/** Update the proxy selection model from a selection event */
	protected void updateProxySelectionModel(int index0, int index1) {
		for(int i = index0; i <= index1; i++) {
			R_Node proxy = model.getProxy(i);
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
			R_Node proxy = model.getProxy(i);
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
		for(R_Node proxy: sel.getSelected()) {
			int i = model.getRow(proxy);
			if(i < index0 || i > index1)
				sel.removeSelected(proxy);
		}
		for(int i = index0; i <= index1; i++) {
			R_Node proxy = model.getProxy(i);
			if(proxy != null)
				sel.addSelected(proxy);
		}
		adjusting--;
	}
}
