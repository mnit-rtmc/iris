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
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel for editing users.
 *
 * @author Douglas Lau
 */
public class UserTabPanel extends JPanel {

	/** Role table panel */
	private final ProxyTablePanel<User> utab_pnl;

	/** User panel */
	private final UserPanel user_pnl;

	/** Create a new user tab panel */
	public UserTabPanel(Session s) {
		setBorder(UI.border);
		utab_pnl = new ProxyTablePanel<User>(new UserModel(s)) {
			protected void selectProxy() {
				selectUser();
				super.selectProxy();
			}
		};
		user_pnl = new UserPanel(s);
	}

	/** Initializze the widgets in the form */
	public void initialize() {
		utab_pnl.initialize();
		user_pnl.initialize();
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
		hg.addComponent(utab_pnl);
		hg.addGap(UI.hgap);
		hg.addComponent(user_pnl);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup vg = gl.createParallelGroup();
		vg.addComponent(utab_pnl);
		vg.addComponent(user_pnl);
		return vg;
	}

	/** Dispose of the panel */
	public void dispose() {
		utab_pnl.dispose();
		user_pnl.dispose();
		removeAll();
	}

	/** Change the selected user */
	private void selectUser() {
		User u = utab_pnl.getSelectedProxy();
		user_pnl.setUser(u);
	}
}
