/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.marking;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.ControllerForm;
import us.mn.state.dot.tms.client.toast.LocationPanel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * LaneMarkingProperties is a dialog for entering and editing lane markings
 *
 * @author Douglas Lau
 */
public class LaneMarkingProperties extends SonarObjectForm<LaneMarking> {

	/** Frame title */
	static private final String TITLE = "Lane Marking: ";

	/** Location panel */
	protected LocationPanel location;

	/** Notes text area */
	protected final JTextArea notes = new JTextArea(3, 24);

	/** Controller button */
	protected final JButton controllerBtn = new JButton("Controller");

	/** Create a new lane marking properties form */
	public LaneMarkingProperties(Session s, LaneMarking ws) {
		super(TITLE, s, ws);
	}

	/** Get the SONAR type cache */
	protected TypeCache<LaneMarking> getTypeCache() {
		return state.getLaneMarkings();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		super.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add("Location", createLocationPanel());
		add(tab);
		updateAttribute(null);
		if(canUpdate())
			createUpdateJobs();
		createControllerJob();
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	protected void dispose() {
		location.dispose();
		super.dispose();
	}

	/** Create the location panel */
	protected JPanel createLocationPanel() {
		location = new LocationPanel(session, proxy.getGeoLoc());
		location.initialize();
		location.addRow("Notes", notes);
		location.setCenter();
		location.addRow(controllerBtn);
		return location;
	}

	/** Create the widget jobs */
	protected void createUpdateJobs() {
		new FocusJob(notes) {
			public void perform() {
				proxy.setNotes(notes.getText());
			}
		};
	}

	/** Create the controller job */
	protected void createControllerJob() {
		new ActionJob(this, controllerBtn) {
			public void perform() throws Exception {
				controllerPressed();
			}
		};
	}

	/** Controller lookup button pressed */
	protected void controllerPressed() {
		Controller c = proxy.getController();
		if(c != null)
			showForm(new ControllerForm(session, c));
	}

	/** Update one attribute on the form */
	protected void doUpdateAttribute(String a) {
		if(a == null || a.equals("controller"))
			controllerBtn.setEnabled(proxy.getController() != null);
		if(a == null || a.equals("notes"))
			notes.setText(proxy.getNotes());
	}
}
