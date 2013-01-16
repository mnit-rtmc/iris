/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.FocusLostJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.LaneMarking;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.utils.I18N;

/**
 * LaneMarkingProperties is a dialog for entering and editing lane markings
 *
 * @author Douglas Lau
 */
public class LaneMarkingProperties extends SonarObjectForm<LaneMarking> {

	/** Location panel */
	private final LocationPanel location;

	/** Notes text area */
	protected final JTextArea notes = new JTextArea(3, 24);

	/** Controller action */
	private final IAction controller = new IAction("controller") {
		@Override protected void do_perform() {
			Controller c = proxy.getController();
			if(c != null)
				showForm(new ControllerForm(session, c));
		}
	};

	/** Create a new lane marking properties form */
	public LaneMarkingProperties(Session s, LaneMarking lm) {
		super(I18N.get("lane.marking") + ": ", s, lm);
		location = new LocationPanel(s);
	}

	/** Get the SONAR type cache */
	protected TypeCache<LaneMarking> getTypeCache() {
		return state.getLaneMarkings();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		super.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), createLocationPanel());
		add(tab);
		updateAttribute(null);
		if(canUpdate())
			createUpdateJobs();
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	protected void dispose() {
		location.dispose();
		super.dispose();
	}

	/** Create the location panel */
	protected JPanel createLocationPanel() {
		location.setGeoLoc(proxy.getGeoLoc());
		location.initialize();
		location.addRow(I18N.get("device.notes"), notes);
		location.setCenter();
		location.addRow(new JButton(controller));
		return location;
	}

	/** Create the widget jobs */
	protected void createUpdateJobs() {
		notes.addFocusListener(new FocusLostJob(WORKER) {
			@Override public void perform() {
				proxy.setNotes(notes.getText());
			}
		});
	}

	/** Update one attribute on the form */
	protected void doUpdateAttribute(String a) {
		if(a == null || a.equals("controller"))
			controller.setEnabled(proxy.getController() != null);
		if(a == null || a.equals("notes"))
			notes.setText(proxy.getNotes());
	}
}
