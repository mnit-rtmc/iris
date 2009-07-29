/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
import java.awt.FlowLayout;
import javax.swing.JToolBar;
import javax.swing.JPanel;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;

/**
 * This status bar contains JPanel components such as the real-time map 
 * coordinates, AWS status, etc.
 * @see CoordinatePanel, AwsStatusPanel
 *
 * @author Michael Darter
 * @company AHMCT
 */
public class IrisToolBar extends JToolBar {

	/** Current session */
	protected final Session session;

	/** Constructor */
	public IrisToolBar(MapBean m, Session s) {
		session = s;
		add(buildComponents(m));
	}

	/** Build toolbar components */
	protected JPanel buildComponents(MapBean m) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ActionPlanPanel plan_panel = new ActionPlanPanel(session);
		p.add(plan_panel);
		if(AwsStatusPanel.getIEnabled()) {
			AwsStatusPanel aws_panel = new AwsStatusPanel(
				session.getSonarState(), session.getDesktop());
			p.add(aws_panel);
		}
		if(MapZoomPanel.getIEnabled()) {
			MapZoomPanel z_panel = new MapZoomPanel(m);
			p.add(z_panel);
		}
		if(CoordinatePanel.getIEnabled()) {
			CoordinatePanel c_panel = new CoordinatePanel(m);
			p.add(c_panel);
		}
		return p;
	}

	/** Dispose of the toolbar */
	public void dispose() {
		for(Component c: getComponents()) {
			if(c instanceof ToolPanel) {
				ToolPanel tp = (ToolPanel)c;
				tp.dispose();
			}
		}
		removeAll();
	}
}
