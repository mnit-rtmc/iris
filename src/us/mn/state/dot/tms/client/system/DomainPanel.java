/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Domain;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel for editing domains.
 *
 * @author Douglas Lau
 */
public class DomainPanel extends JPanel {

	/** User session */
	private final Session session;

	/** Domain table panel */
	private final ProxyTablePanel<Domain> domain_pnl;

	/** Create a new domain panel */
	public DomainPanel(Session s) {
		setBorder(UI.border);
		session = s;
		DomainModel d_mdl = new DomainModel(s);
		domain_pnl = new ProxyTablePanel<Domain>(d_mdl);
	}

	/** Initializze the panel */
	public void initialize() {
		domain_pnl.initialize();
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
		hg.addComponent(domain_pnl);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup vg = gl.createParallelGroup();
		vg.addComponent(domain_pnl);
		return vg;
	}

	/** Dispose of the panel */
	public void dispose() {
		domain_pnl.dispose();
		removeAll();
	}
}
