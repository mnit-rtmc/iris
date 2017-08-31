/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.beacon;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.PresetComboRenderer;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.utils.I18N;

/**
 * BeaconProperties is a dialog for entering and editing beacons.
 *
 * @author Douglas Lau
 */
public class BeaconProperties extends SonarObjectForm<Beacon> {

	/** Location panel */
	private final LocationPanel loc_pnl;

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(3, 24);

	/** Controller action */
	private final IAction controller = new IAction("controller") {
		protected void doActionPerformed(ActionEvent e) {
			controllerPressed();
		}
	};

	/** Camera preset action */
	private final IAction preset_act = new IAction("camera.preset") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setPreset(preset_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			preset_mdl.setSelectedItem(proxy.getPreset());
		}
	};

	/** Camera preset combo box */
	private final JComboBox<CameraPreset> preset_cbx =
		new JComboBox<CameraPreset>();

	/** Camera preset combo box model */
	private final IComboBoxModel<CameraPreset> preset_mdl;

	/** Message text area */
	private final JTextArea message_txt = new JTextArea(3, 24);

	/** Create a new beacon form */
	public BeaconProperties(Session s, Beacon b) {
		super(I18N.get("beacon") + ": ", s, b);
		loc_pnl = new LocationPanel(s);
		preset_mdl = new IComboBoxModel<CameraPreset>(
			state.getCamCache().getPresetModel());
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<Beacon> getTypeCache() {
		return state.getBeacons();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), createLocationPanel());
		tab.add(I18N.get("device.setup"), createSetupPanel());
		add(tab);
		createUpdateJobs();
		super.initialize();
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		loc_pnl.dispose();
		super.dispose();
	}

	/** Create the location panel */
	private JPanel createLocationPanel() {
		loc_pnl.initialize();
		loc_pnl.add("device.notes");
		loc_pnl.add(notes_txt, Stretch.FULL);
		loc_pnl.add(new JButton(controller), Stretch.RIGHT);
		loc_pnl.setGeoLoc(proxy.getGeoLoc());
		return loc_pnl;
	}

	/** Create jobs for updating */
	private void createUpdateJobs() {
		notes_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				proxy.setNotes(notes_txt.getText());
			}
		});
		message_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				proxy.setMessage(message_txt.getText());
			}
		});
	}

	/** Controller lookup button pressed */
	private void controllerPressed() {
		Controller c = proxy.getController();
		if (c != null)
			showForm(new ControllerForm(session, c));
	}

	/** Create the setup panel */
	private JPanel createSetupPanel() {
		preset_cbx.setModel(preset_mdl);
		preset_cbx.setAction(preset_act);
		preset_cbx.setRenderer(new PresetComboRenderer());
		IPanel p = new IPanel();
		p.add("camera.preset");
		p.add(preset_cbx, Stretch.LAST);
		p.add("beacon.text");
		p.add(message_txt, Stretch.LAST);
		return p;
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		loc_pnl.updateEditMode();
		notes_txt.setEnabled(canWrite("notes"));
		preset_act.setEnabled(canWrite("preset"));
		message_txt.setEnabled(canWrite("message"));
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		if (a == null || a.equals("controller"))
			controller.setEnabled(proxy.getController() != null);
		if (a == null || a.equals("notes"))
			notes_txt.setText(proxy.getNotes());
		if (a == null || a.equals("preset"))
			preset_act.updateSelected();
		if (a == null || a.equals("message"))
			message_txt.setText(proxy.getMessage());
	}
}
