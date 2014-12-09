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
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel for editing roles.
 *
 * @author Douglas Lau
 */
public class RolePanel extends JPanel {

	/** User session */
	private final Session session;

	/** Role table panel */
	private final ProxyTablePanel<Role> role_pnl;

	/** Capability table panel */
	private final ProxyTablePanel<Capability> cap_pnl;

	/** Create a new role panel */
	public RolePanel(Session s) {
		setBorder(UI.border);
		session = s;
		RoleModel r_mdl = new RoleModel(s) {
			protected void proxyChangedSwing(Role r) {
				super.proxyChangedSwing(r);
				/* Repaint the capability panel when the
				 * role capabilities are changed. */
				cap_pnl.repaint();
			}
		};
		role_pnl = new ProxyTablePanel<Role>(r_mdl) {
			protected void selectProxy() {
				selectRole();
				super.selectProxy();
			}
		};
		cap_pnl = new ProxyTablePanel<Capability>(
			new RoleCapabilityModel(s, null));
	}

	/** Initializze the panel */
	public void initialize() {
		role_pnl.initialize();
		cap_pnl.initialize();
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
		hg.addComponent(role_pnl);
		hg.addGap(UI.hgap);
		hg.addComponent(cap_pnl);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup vg = gl.createParallelGroup();
		vg.addComponent(role_pnl);
		vg.addComponent(cap_pnl);
		return vg;
	}

	/** Dispose of the panel */
	public void dispose() {
		role_pnl.dispose();
		cap_pnl.dispose();
		removeAll();
	}

	/** Change the selected role */
	private void selectRole() {
		Role r = role_pnl.getSelectedProxy();
		cap_pnl.setModel(new RoleCapabilityModel(session, r));
	}
}
