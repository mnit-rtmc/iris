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
package us.mn.state.dot.tms.client.toolbar;

import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JToolBar;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.map.MapBean;

/**
 * This status bar contains JPanel components such as the real-time map 
 * coordinates, AWS status, etc.
 * @see CoordinatePanel, AwsStatusPanel
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class IrisToolBar extends JToolBar {

	/** Map widget */
	private final MapBean map;

	/** Create a new IRIS toolbar */
	public IrisToolBar(MapBean m) {
		map = m;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

	/** Build toolbar components */
	public void createToolPanels(Session s) {
		if (ModemPanel.getIEnabled()) {
			add(new ModemPanel(s));
			add(Box.createGlue());
		}
		if (AwsStatusPanel.getIEnabled()) {
			add(new AwsStatusPanel(s.getSonarState(),
				s.getDesktop()));
			add(Box.createGlue());
		}
		if (CoordinatePanel.getIEnabled()) {
			add(new CoordinatePanel(map));
			add(Box.createGlue());
		}
		if (EditModePanel.getIEnabled())
			add(new EditModePanel(s));
	}

	/** Clear the toolbar */
	public void clear() {
		for (Component c: getComponents()) {
			if (c instanceof ToolPanel) {
				ToolPanel tp = (ToolPanel)c;
				tp.dispose();
			}
		}
		removeAll();
	}

	/** Dispose of the toolbar */
	public void dispose() {
		clear();
	}
}
