/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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

import java.awt.Color;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * IncidentDeployForm is a dialog for deploying devices for an incident.
 *
 * @author Douglas Lau
 */
public class IncidentDeployForm extends SonarObjectForm<Incident> {

	/** Frame title */
	static private final String TITLE = "Incident: ";

	/** Currently logged in user */
	protected final User user;

	/** Mapping of LCS array names to proposed indications */
	protected final HashMap<String, Integer []> indications =
		new HashMap<String, Integer []>();

	/** List of deployments for the incident */
	protected final JList list = new JList();

	/** Button to send device messages */
	protected final JButton send_btn = new JButton("Send");

	/** Create a new incident deploy form */
	public IncidentDeployForm(Session s, Incident inc) {
		super(TITLE, s, inc);
		user = session.getUser();
	}

	/** Get the SONAR type cache */
	protected TypeCache<Incident> getTypeCache() {
		return state.getIncidents();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		super.initialize();
		populateList();
		add(createPanel());
		createJobs();
	}

	/** Populate the list model with LCS array indications to display */
	protected void populateList() {
		// FIXME: find upstream LCS arrays
	}

	/** Create the panel for the form */
	protected JPanel createPanel() {
		FormPanel panel = new FormPanel(false);
		panel.addRow(list);
		panel.addRow(send_btn);
		send_btn.setEnabled(true);
		return panel;
	}

	/** Create jobs */
	protected void createJobs() {
		new ActionJob(send_btn) {
			public void perform() {
				sendIndications();
			}
		};
	}

	/** Send new indications to LCS arrays for the incident */
	protected void sendIndications() {
		ListModel model = list.getModel();
		for(int i = 0; i < model.getSize(); i++) {
			Object e = model.getElementAt(i);
			if(e instanceof LCSArray)
				sendIndications((LCSArray)e);
		}
	}

	/** Send new indications to the specified LCS array */
	protected void sendIndications(LCSArray lcs_array) {
		Integer[] ind = indications.get(lcs_array.getName());
		if(ind != null) {
			lcs_array.setOwnerNext(user);
			lcs_array.setIndicationsNext(ind);
		}
	}

	/** Update one attribute on the form */
	protected void doUpdateAttribute(String a) {
		if("cleared".equals(a) && proxy.getCleared())
			closeForm();
	}
}
