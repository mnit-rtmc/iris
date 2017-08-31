/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JButton;
import javax.swing.JTextArea;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.IAction;

/**
 * PropLocation is a GUI panel for displaying and editing locations on a camera
 * properties form.
 *
 * @author Douglas Lau
 */
public class PropLocation extends LocationPanel {

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(3, 24);

	/** Controller action */
	private final IAction controller = new IAction("controller") {
		protected void doActionPerformed(ActionEvent e) {
			controllerPressed();
		}
	};

	/** Controller lookup button pressed */
	private void controllerPressed() {
		Controller c = camera.getController();
		if (c != null) {
			session.getDesktop().show(
				new ControllerForm(session, c));
		}
	}

	/** Camera to display */
	private final Camera camera;

	/** Create a new camera properties location panel */
	public PropLocation(Session s, Camera c) {
		super(s);
		camera = c;
	}

	/** Initialize the widgets on the form */
	@Override
	public void initialize() {
		super.initialize();
		add("device.notes");
		add(notes_txt, Stretch.FULL);
		add(new JButton(controller), Stretch.RIGHT);
		setGeoLoc(camera.getGeoLoc());
	}

	/** Create the widget jobs */
	@Override
	protected void createJobs() {
		super.createJobs();
		notes_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				camera.setNotes(notes_txt.getText());
			}
		});
	}

	/** Update the edit mode */
	@Override
	public void updateEditMode() {
		super.updateEditMode();
		notes_txt.setEnabled(canWrite("notes"));
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		if (a == null || a.equals("controller"))
			controller.setEnabled(camera.getController() != null);
		if (a == null || a.equals("notes"))
			notes_txt.setText(camera.getNotes());
	}

	/** Check if the user can write an attribute */
	private boolean canWrite(String aname) {
		return session.canWrite(camera, aname);
	}
}
