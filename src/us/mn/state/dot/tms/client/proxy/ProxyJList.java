/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JList;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sonar.SonarObject;

/**
 * A proxy JList is a special JList which contains SONAR proxy objects.
 *
 * @author Douglas Lau
 */
public class ProxyJList<T extends SonarObject> extends JList {

	/** Style list model */
	protected final StyleListModel<T> model;

	/** Create a new proxy JList */
	public ProxyJList(StyleListModel<T> m) {
		model = m;
		setModel(m);
		setSelectionModel(m.getSelectionModel());
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() > 1)
					doDoubleClick();
			}
			public void mousePressed(MouseEvent e) {
				if(e.isPopupTrigger())
					popupMenu(e);
			}
			// NOTE: needed for cross-platform functionality
			public void mouseReleased(MouseEvent e) {
				if(e.isPopupTrigger())
					popupMenu(e);
			}
		});
	}

	/** Respond to a double-click event */
	protected void doDoubleClick() {
		new AbstractJob() {
			public void perform() {
				model.getManager().showPropertiesForm();
			}
		}.addToScheduler();
	}

	/** Set the selected proxy */
	protected void setSelected(SonarObject o) {
		setSelectedValue(o, true);
		if(getSelectedValue() != o)
			clearSelection();
	}

	/** Popup a context-sensitive menu */
	protected void popupMenu(MouseEvent e) {
		int index = locationToIndex(e.getPoint());
		if(index >= 0) {
			Rectangle bounds = getCellBounds(index, index);
			if(bounds.contains(e.getPoint()))
				model.getManager().showPopupMenu(e);
		}
	}
}
