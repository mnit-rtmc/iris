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
package us.mn.state.dot.tms.client.system;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import us.mn.state.dot.sonar.Capability;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel for editing capabilities and privileges.
 *
 * @author Douglas Lau
 */
public class CapabilityPanel extends JPanel {

	/** User session */
	private final Session session;

	/** Capability table panel */
	private final ProxyTablePanel<Capability> cap_pnl;

	/** Privilege table panel */
	private final PrivilegePanel priv_pnl;

	/** Create a new capability panel */
	public CapabilityPanel(Session s) {
		setBorder(UI.border);
		session = s;
		cap_pnl = new ProxyTablePanel<Capability>(
			new CapabilityModel(s))
		{
			protected void selectProxy() {
				selectCapability();
				super.selectProxy();
			}
		};
		priv_pnl = new PrivilegePanel(new PrivilegeModel(s, null));
	}

	/** Initializze the panel */
	public void initialize() {
		cap_pnl.initialize();
		priv_pnl.initialize();
		layoutPanel();
	}

	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		gl.setHorizontalGroup(createHorizontalGroup(gl));
		gl.setVerticalGroup(createVerticalGroup(gl));
		setLayout(gl);
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup hg = gl.createSequentialGroup();
		hg.addComponent(cap_pnl);
		hg.addGap(UI.hgap);
		hg.addComponent(priv_pnl);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup vg = gl.createParallelGroup();
		vg.addComponent(cap_pnl);
		vg.addComponent(priv_pnl);
		return vg;
	}

	/** Dispose of the panel */
	public void dispose() {
		cap_pnl.dispose();
		priv_pnl.dispose();
		removeAll();
	}

	/** Change the selected capability */
	private void selectCapability() {
		Capability c = cap_pnl.getSelectedProxy();
		priv_pnl.setModel(new PrivilegeModel(session, c));
	}
}
