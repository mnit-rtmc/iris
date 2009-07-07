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

import java.awt.FlowLayout;
import javax.swing.JToolBar;
import javax.swing.JPanel;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * This status bar contains JPanel components such as the real-time map 
 * coordinates, AWS status, etc.
 * @see CoordinatePanel, AwsStatusPanel
 *
 * @author Michael Darter
 * @company AHMCT
 */
public class IrisToolBar extends JToolBar {

	/** coordinate panel */
	protected CoordinatePanel c_panel;

	/** AWS panel */
	protected AwsStatusPanel aws_panel;

	/** Map zoom panel */
	protected MapZoomPanel z_panel;

	/** Action plan panel */
	protected ActionPlanPanel plan_panel;

	/** sonar state */
	protected final SonarState state;

	/** Constructor */
	public IrisToolBar(MapBean m, SonarState st, SmartDesktop desktop) {
		super();
		state = st;
		add(buildComponents(m, desktop));
	}

	/** Build toolbar components */
	protected JPanel buildComponents(MapBean m, SmartDesktop desktop) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		plan_panel = new ActionPlanPanel(state);
		p.add(plan_panel);
		if(AwsStatusPanel.getIEnabled()) {
			aws_panel = new AwsStatusPanel(state, desktop);
			p.add(aws_panel);
		}
		if(MapZoomPanel.getIEnabled()) {
			z_panel = new MapZoomPanel(m);
			p.add(z_panel);
		}
		if(CoordinatePanel.getIEnabled()) {
			c_panel = new CoordinatePanel(m);
			p.add(c_panel);
		}
		return p;
	}

	/** cleanup */
	public void dispose() {
		plan_panel.dispose();
		if(c_panel != null)
			c_panel.dispose();
		if(aws_panel != null)
			aws_panel.dispose();
		if(z_panel != null)
			z_panel.dispose();
	}
}
