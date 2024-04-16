/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2024  Minnesota Department of Transportation
 * Copyright (C) 2017       SRF Consulting
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

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.PresetComboRenderer;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.gps.GpsPanel;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;

/**
 * PropLocation is a GUI panel for displaying and editing locations on a DMS
 * properties form.
 *
 * @author Douglas Lau
 */
public class PropLocation extends LocationPanel {

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(11, 24);

	/** Camera preset combo box model */
	private final IComboBoxModel<CameraPreset> preset_mdl;

	/** Camera preset action */
	private final IAction preset_act = new IAction("camera.preset") {
		protected void doActionPerformed(ActionEvent e) {
			dms.setPreset(preset_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			preset_mdl.setSelectedItem(dms.getPreset());
		}
	};

	/** Camera preset combo box */
	private final JComboBox<CameraPreset> preset_cbx =
		new JComboBox<CameraPreset>();

	/** Controller action */
	private final IAction controller = new IAction("controller") {
		protected void doActionPerformed(ActionEvent e) {
			controllerPressed();
		}
	};

	/** Controller lookup button pressed */
	private void controllerPressed() {
		Controller c = dms.getController();
		if (c != null) {
			session.getDesktop().show(
				new ControllerForm(session, c));
		}
	}

	/** GPS panel */
	private final GpsPanel gps_pnl;

	/** DMS to display */
	private final DMS dms;

	/** Create a new DMS properties location panel */
	public PropLocation(Session s, DMS sign) {
		super(s);
		dms = sign;
		preset_mdl = new IComboBoxModel<CameraPreset>(
			state.getCamCache().getPresetModel());
		gps_pnl = new GpsPanel(s, sign);
	}

	/** Initialize the widgets on the form */
	@Override
	public void initialize() {
		super.initialize();
		preset_cbx.setModel(preset_mdl);
		preset_cbx.setAction(preset_act);
		preset_cbx.setRenderer(new PresetComboRenderer());

		add("device.notes");
		add(notes_txt, Stretch.DOUBLE);
		add(gps_pnl, Stretch.CENTER);
		add("camera.preset");
		add(preset_cbx, Stretch.LAST);
		add(new JButton(controller), Stretch.RIGHT);
		setGeoLoc(dms.getGeoLoc());
	}

	/** Create the widget jobs */
	@Override
	protected void createJobs() {
		super.createJobs();
		notes_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String n = notes_txt.getText().trim();
				dms.setNotes((n.length() > 0) ? n : null);
			}
		});
	}

	/** Update the edit mode */
	@Override
	public void updateEditMode() {
		super.updateEditMode();
		gps_pnl.updateEditMode();
		notes_txt.setEnabled(canWrite("notes"));
		preset_act.setEnabled(canWrite("preset"));
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		if (a == null || a.equals("controller"))
			controller.setEnabled(dms.getController() != null);
		if (a == null || a.equals("notes")) {
			String n = dms.getNotes();
			notes_txt.setText((n != null) ? n : "");
		}
		if (a == null || a.equals("preset"))
			preset_act.updateSelected();
	}

	/** Check if the user can write an attribute */
	private boolean canWrite(String aname) {
		return session.canWrite(dms, aname);
	}
}
