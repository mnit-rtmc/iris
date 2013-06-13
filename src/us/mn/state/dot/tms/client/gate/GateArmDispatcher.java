/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.gate;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;

/**
 * GateArmDispatcher is a GUI component for deploying gate arms.
 *
 * @author Douglas Lau
 */
public class GateArmDispatcher extends JPanel {

	/** SONAR namespace */
	private final Namespace namespace;

	/** Currently logged in user */
	private final User user;

	/** Selection model */
	private final ProxySelectionModel<GateArm> sel_model;

	/** Selection listener */
	private final ProxySelectionListener<GateArm> sel_listener =
		new ProxySelectionListener<GateArm>()
	{
		public void selectionAdded(GateArm ga) { }
		public void selectionRemoved(GateArm ga) { }
	};

	/** Create a new gate arm dispatcher */
	public GateArmDispatcher(Session session, GateArmManager manager) {
		super(new BorderLayout());
		SonarState st = session.getSonarState();
		namespace = st.getNamespace();
		user = session.getUser();
		sel_model = manager.getSelectionModel();
		sel_model.addProxySelectionListener(sel_listener);
	}

	/** Dispose of the dispatcher */
	public void dispose() {
		sel_model.removeProxySelectionListener(sel_listener);
		removeAll();
	}
}
