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
package us.mn.state.dot.tms.client.parking;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.ParkingArea;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.PresetComboRenderer;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.utils.I18N;

/**
 * ParkingAreaProperties is a dialog for entering and editing parking areas.
 *
 * @author Douglas Lau
 */
public class ParkingAreaProperties extends SonarObjectForm<ParkingArea> {

	/** Location panel */
	private final LocationPanel loc_pnl;

	/** Camera preset 1 combo box */
	private final JComboBox<CameraPreset> preset_1_cbx =
		new JComboBox<CameraPreset>();

	/** Camera preset 1 combo box model */
	private final IComboBoxModel<CameraPreset> preset_1_mdl;

	/** Camera preset 1 action */
	private final IAction preset_1_act = new IAction("camera.preset") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setPreset1(preset_1_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			preset_1_mdl.setSelectedItem(proxy.getPreset1());
		}
	};

	/** Camera preset 2 combo box */
	private final JComboBox<CameraPreset> preset_2_cbx =
		new JComboBox<CameraPreset>();

	/** Camera preset 2 combo box model */
	private final IComboBoxModel<CameraPreset> preset_2_mdl;

	/** Camera preset 2 action */
	private final IAction preset_2_act = new IAction("camera.preset") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setPreset2(preset_2_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			preset_2_mdl.setSelectedItem(proxy.getPreset2());
		}
	};

	/** Camera preset 3 combo box */
	private final JComboBox<CameraPreset> preset_3_cbx =
		new JComboBox<CameraPreset>();

	/** Camera preset 3 combo box model */
	private final IComboBoxModel<CameraPreset> preset_3_mdl;

	/** Setup panel */
	private final PropSetup setup;

	/** Camera preset 3 action */
	private final IAction preset_3_act = new IAction("camera.preset") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setPreset3(preset_3_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			preset_3_mdl.setSelectedItem(proxy.getPreset3());
		}
	};

	/** Create a new parking area properties form */
	public ParkingAreaProperties(Session s, ParkingArea pa) {
		super(I18N.get("parking_area") + ": ", s, pa);
		loc_pnl = new LocationPanel(s);
		preset_1_mdl = new IComboBoxModel<CameraPreset>(
			state.getCamCache().getPresetModel());
		preset_2_mdl = new IComboBoxModel<CameraPreset>(
			state.getCamCache().getPresetModel());
		preset_3_mdl = new IComboBoxModel<CameraPreset>(
			state.getCamCache().getPresetModel());
		setup = new PropSetup(session, pa);
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<ParkingArea> getTypeCache() {
		return state.getParkingAreas();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), createLocationPanel());
		tab.add(I18N.get("parking_area.setup"), setup);
		add(tab);
		setup.initialize();
		super.initialize();
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		loc_pnl.dispose();
		setup.dispose();
		super.dispose();
	}

	/** Create the location panel */
	private JPanel createLocationPanel() {
		preset_1_cbx.setModel(preset_1_mdl);
		preset_1_cbx.setAction(preset_1_act);
		preset_1_cbx.setRenderer(new PresetComboRenderer());
		preset_2_cbx.setModel(preset_2_mdl);
		preset_2_cbx.setAction(preset_2_act);
		preset_2_cbx.setRenderer(new PresetComboRenderer());
		preset_3_cbx.setModel(preset_3_mdl);
		preset_3_cbx.setAction(preset_3_act);
		preset_3_cbx.setRenderer(new PresetComboRenderer());
		loc_pnl.initialize();
		loc_pnl.add("camera.preset");
		loc_pnl.add(preset_1_cbx, Stretch.LAST);
		loc_pnl.add("camera.preset");
		loc_pnl.add(preset_2_cbx, Stretch.LAST);
		loc_pnl.add("camera.preset");
		loc_pnl.add(preset_3_cbx, Stretch.LAST);
		loc_pnl.setGeoLoc(proxy.getGeoLoc());
		return loc_pnl;
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		loc_pnl.updateEditMode();
		preset_1_act.setEnabled(canWrite("preset1"));
		preset_2_act.setEnabled(canWrite("preset2"));
		preset_3_act.setEnabled(canWrite("preset3"));
		setup.updateEditMode();
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		if (a == null || a.equals("preset1"))
			preset_1_act.updateSelected();
		if (a == null || a.equals("preset2"))
			preset_2_act.updateSelected();
		if (a == null || a.equals("preset3"))
			preset_3_act.updateSelected();
		setup.updateAttribute(a);
	}
}
