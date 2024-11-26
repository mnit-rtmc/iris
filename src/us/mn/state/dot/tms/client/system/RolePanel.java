/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Domain;
import us.mn.state.dot.tms.Role;
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

	/** Permission panel */
	private final PermissionPanel perm_pnl;

	/** Domain table panel */
	private final ProxyTablePanel<Domain> dom_pnl;

	/** Create a new role panel */
	public RolePanel(Session s) {
		setBorder(UI.border);
		session = s;
		RoleModel r_mdl = new RoleModel(s) {
			protected void proxyChangedSwing(Role r) {
				super.proxyChangedSwing(r);
				/* Repaint other panels when the
				 * role domains are changed. */
				perm_pnl.repaint();
				dom_pnl.repaint();
			}
		};
		role_pnl = new ProxyTablePanel<Role>(r_mdl) {
			protected void selectProxy() {
				selectRole();
				super.selectProxy();
			}
		};
		perm_pnl = new PermissionPanel(
			new PermissionModel(s, null)
		);
		dom_pnl = new ProxyTablePanel<Domain>(
			new RoleDomainModel(s, null)
		);
	}

	/** Initializze the panel */
	public void initialize() {
		role_pnl.initialize();
		perm_pnl.initialize();
		dom_pnl.initialize();
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
		hg.addComponent(perm_pnl);
		hg.addGap(UI.hgap);
		hg.addComponent(dom_pnl);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup vg = gl.createParallelGroup();
		vg.addComponent(role_pnl);
		vg.addComponent(perm_pnl);
		vg.addComponent(dom_pnl);
		return vg;
	}

	/** Dispose of the panel */
	public void dispose() {
		role_pnl.dispose();
		perm_pnl.dispose();
		dom_pnl.dispose();
		removeAll();
	}

	/** Change the selected role */
	private void selectRole() {
		Role r = role_pnl.getSelectedProxy();
		perm_pnl.setModel(new PermissionModel(session, r));
		dom_pnl.setModel(new RoleDomainModel(session, r));
	}
}
