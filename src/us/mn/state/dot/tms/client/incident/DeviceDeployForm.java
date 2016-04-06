/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.utils.I18N;

/**
 * DeviceDeployForm is a dialog for deploying devices for an incident.
 *
 * @author Douglas Lau
 */
public class DeviceDeployForm extends SonarObjectForm<Incident> {

	/** Incident manager */
	private final IncidentManager manager;

	/** Model for deployment list */
	private final DeviceDeployModel model;

	/** List of deployments for the incident */
	private final JList<LCSArray> list;

	/** Action to send device messages */
	private final IAction send = new IAction("incident.send") {
		protected void doActionPerformed(ActionEvent e) {
			sendIndications();
			close(session.getDesktop());
		}
	};

	/** Create a new incident device deploy form */
	public DeviceDeployForm(Session s, Incident inc, IncidentManager man){
		super(I18N.get("incident") + ": ", s, inc);
		manager = man;
		model = new DeviceDeployModel(man, inc);
		list = new JList<LCSArray>(model);
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<Incident> getTypeCache() {
		return state.getIncCache().getIncidents();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		list.setCellRenderer(new ProposedLcsCellRenderer(session,
			model));
		add(createPanel());
		super.initialize();
	}

	/** Create the panel for the form */
	private JPanel createPanel() {
		JLabel lbl = new JLabel();
		lbl.setHorizontalTextPosition(SwingConstants.TRAILING);
		lbl.setText(manager.getDescription(proxy));
		lbl.setIcon(manager.getIcon(proxy));
		JButton btn = new JButton(send);
		send.setEnabled(model.getSize() > 0);
		btn.setEnabled(model.getSize() > 0);
		IPanel p = new IPanel();
		p.add(lbl, Stretch.CENTER);
		p.add("incident.deploy.proposed");
		p.add(list, Stretch.FULL);
		p.add(btn, Stretch.RIGHT);
		return p;
	}

	/** Send new indications to LCS arrays for the incident */
	private void sendIndications() {
		for (int i = 0; i < model.getSize(); i++)
			sendIndications(model.getElementAt(i));
	}

	/** Send new indications to the specified LCS array */
	private void sendIndications(LCSArray lcs_array) {
		Integer[] ind = model.getIndications(lcs_array.getName());
		if (ind != null) {
			lcs_array.setOwnerNext(session.getUser());
			lcs_array.setIndicationsNext(ind);
		}
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		if ("cleared".equals(a) && proxy.getCleared())
			close(session.getDesktop());
	}
}
