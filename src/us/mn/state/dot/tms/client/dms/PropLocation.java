/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * PropLocation is a GUI panel for displaying and editing locations on a DMS
 * properties form.
 *
 * @author Douglas Lau
 */
public class PropLocation extends LocationPanel {

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(3, 24);

	/** Camera combo box */
	private final JComboBox camera_cbx = new JComboBox();

	/** Controller action */
	private final IAction controller = new IAction("controller") {
		@Override protected void do_perform() {
			controllerPressed();
		}
	};

	/** Controller lookup button pressed */
	private void controllerPressed() {
		Controller c = dms.getController();
		if(c != null) {
			session.getDesktop().show(
				new ControllerForm(session, c));
		}
	}

	/** DMS to display */
	private final DMS dms;

	/** Create a new DMS properties location panel */
	public PropLocation(Session s, DMS sign) {
		super(s);
		dms = sign;
	}

	/** Initialize the widgets on the form */
	@Override public void initialize() {
		super.initialize();
		camera_cbx.setModel(new WrapperComboBoxModel(
			state.getCamCache().getCameraModel()));
		add("device.notes");
		add(notes_txt, Stretch.FULL);
		add("camera");
		add(camera_cbx, Stretch.LAST);
		add(new JButton(controller), Stretch.RIGHT);
		setGeoLoc(dms.getGeoLoc());
	}

	/** Create the widget jobs */
	@Override protected void createJobs() {
		super.createJobs();
		notes_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				dms.setNotes(notes_txt.getText());
			}
		});
		camera_cbx.setAction(new IAction("camera") {
			@Override protected void do_perform() {
				dms.setCamera(
					(Camera)camera_cbx.getSelectedItem());
			}
		});
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		if(a == null || a.equals("controller"))
			controller.setEnabled(dms.getController() != null);
		if(a == null || a.equals("notes")) {
			notes_txt.setEnabled(canUpdate("notes"));
			notes_txt.setText(dms.getNotes());
		}
		if(a == null || a.equals("camera")) {
			camera_cbx.setEnabled(canUpdate("camera"));
			camera_cbx.setSelectedItem(dms.getCamera());
		}
	}

	/** Check if the user can update an attribute */
	private boolean canUpdate(String aname) {
		return session.canUpdate(dms, aname);
	}
}
