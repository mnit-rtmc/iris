/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2015  Minnesota Department of Transportation
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
	private final R_NodeListModel model;

	/** Proxy selection model */
	private final ProxySelectionModel<R_Node> sel;

	/** Listener for proxy selection events */
	private final ProxySelectionListener sel_lsnr =
		new ProxySelectionListener()
	{
		public void selectionChanged() {
			doSelectionChanged();
		}
	};

	/** Update the selection model */
	private void doSelectionChanged() {
		R_Node proxy = sel.getSingleSelection();
		if (proxy != null) {
			int i = model.getRow(proxy);
			if (i >= 0) {
				super.setSelectionInterval(i, i);
				return;
			}
		}
		clearSelection();
	}

	/** Create a new r_node list selection model */
	public R_NodeListSelectionModel(R_NodeListModel m,
		ProxySelectionModel<R_Node> s)
	{
		model = m;
		sel = s;
		setSelectionMode(SINGLE_SELECTION);
		doSelectionChanged();
		sel.addProxySelectionListener(sel_lsnr);
	}

	/** Set the selection mode */
	@Override
	public void setSelectionMode(int m) {
		if (m == SINGLE_SELECTION)
			super.setSelectionMode(m);
		else
			throw new IllegalArgumentException();
	}

	/** Dispose of the model */
	public void dispose() {
		sel.removeProxySelectionListener(sel_lsnr);
	}

	/** Insert an interval into the model */
	@Override
	public void insertIndexInterval(int index, int length, boolean before) {
		super.insertIndexInterval(index, length, before);
		// NOTE: if a proxy being added is already selected,
		//       we need to add it to this selection model
		for (int i = index; i < index + length; i++) {
			R_Node proxy = model.getProxy(i);
			if (proxy != null && sel.isSelected(proxy))
				super.setSelectionInterval(i, i);
		}
	}

	/** Set the selection interval */
	@Override
	public void setSelectionInterval(int index0, int index1) {
		super.setSelectionInterval(index0, index1);
		R_Node proxy = model.getProxy(index1);
		if (proxy != null)
			sel.setSelected(proxy);
		else
			sel.clearSelection();
	}
}
