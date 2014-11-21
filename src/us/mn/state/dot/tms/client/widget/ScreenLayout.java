/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import javax.swing.JLayeredPane;

/**
 * ScreenLayout is a layout manager which lays out components on physical
 * screens. It is usefull for dual-monitor setups when components should not
 * span multiple screens.
 *
 * @author Douglas Lau
 */
public class ScreenLayout implements LayoutManager {

	/** Array of all physical screens */
	protected final Screen[] screens = Screen.getAllScreens();

	/** Create a new screen layout manager */
	public ScreenLayout(final Container parent) {
		parent.addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
			public void ancestorMoved(HierarchyEvent e) {
				layoutContainer(parent);
			}
		});
		parent.setLayout(this);
	}

	/** Add a layout component */
	public void addLayoutComponent(String name, Component comp) {
		// do nothing
	}

	/** Remove a layout component */
	public void removeLayoutComponent(Component comp) {
		// do nothing
	}

	/** Get the preferred layout size */
	public Dimension preferredLayoutSize(Container parent) {
		return parent.getSize();
	}

	/** Get the minimum layout size */
	public Dimension minimumLayoutSize(Container parent) {
		return Widgets.UI.dimension(100, 100);
	}

	/** Layout the container */
	protected void layoutContainer(Container parent, Component[] comps) {
		Rectangle bounds = parent.getBounds();
		Point p = Screen.getLocation(parent);
		Rectangle rect = new Rectangle(bounds);
		rect.translate(p.x, p.y);
		for(int i = 0; i < comps.length; i++) {
			Component c = comps[i];
			if(i < screens.length) {
				Rectangle b = rect.intersection(
					screens[i].getBounds());
				b.translate(-p.x, -p.y);
				c.setBounds(b);
				c.setVisible(!b.isEmpty());
			} else
				c.setVisible(false);
		}
	}

	/** Layout the container */
	public void layoutContainer(Container parent) {
		// Only lay out the default layer in a layered pane
		if(parent instanceof JLayeredPane) {
			JLayeredPane pane = (JLayeredPane)parent;
			Component[] comps = pane.getComponentsInLayer(0);
			layoutContainer(parent, comps);
		} else
			layoutContainer(parent, parent.getComponents());
	}
}
