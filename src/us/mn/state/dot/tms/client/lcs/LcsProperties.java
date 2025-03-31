/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsState;
import us.mn.state.dot.tms.LcsType;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.PresetComboRenderer;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * LcsProperties is a dialog for editing the properties of an LCS array.
 *
 * @author Douglas Lau
 */
public class LcsProperties extends SonarObjectForm<Lcs> {

	/** Size in pixels for each indication icon */
	static private final int LCS_SIZE = UI.scaled(18);

	/** Location panel */
	private final LocationPanel loc_pnl;

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(3, 32);

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

	/** Controller action */
	private final IAction controller = new IAction("controller") {
		protected void doActionPerformed(ActionEvent e) {
			Controller c = proxy.getController();
			if (c != null) {
				SmartDesktop sd = session.getDesktop();
				sd.show(new ControllerForm(session, c));
			}
		}
	};

	/** LCS type action */
	private final IAction lcs_type_act = new IAction("lcs.type") {
		protected void doActionPerformed(ActionEvent e) {
			int t = lcs_type_cbx.getSelectedIndex();
			if (t >= 0)
				proxy.setLcsType(t);
		}
		@Override
		protected void doUpdateSelected() {
			lcs_type_cbx.setSelectedIndex(proxy.getLcsType());
		}
	};

	/** LCS type combo box component */
	private final JComboBox<LcsType> lcs_type_cbx = new JComboBox
		<LcsType>(LcsType.values());

	/** LCS state table panel */
	private final ProxyTablePanel<LcsState> state_pnl;

	/** Spinner for lane shift */
	private final JSpinner shift_spn = new JSpinner(
		new SpinnerNumberModel(0, 0, 12, 1));

	/** Action to send settings */
	private final IAction settings = new IAction("device.send.settings") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setDeviceRequest(DeviceRequest.
				SEND_SETTINGS.ordinal());
		}
	};

	/** Create a new lane control signal properties form */
	public LcsProperties(Session s, Lcs lcs) {
		super(I18N.get("lcs") + ": ", s, lcs);
		loc_pnl = new LocationPanel(s);
		preset_mdl = new IComboBoxModel<CameraPreset>(
			state.getCamCache().getPresetModel());
		state_pnl = new ProxyTablePanel<LcsState>(
			new LcsStateModel(s, lcs));
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<Lcs> getTypeCache() {
		return state.getLcsCache().getLcss();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		state_pnl.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), createLocationPanel());
		tab.add(I18N.get("device.setup"), createSetupPanel());
		add(tab);
		createUpdateJobs();
		settings.setEnabled(isWritePermitted("deviceRequest"));
		super.initialize();
	}

	/** Create the location panel */
	private JPanel createLocationPanel() {
		preset_cbx.setModel(preset_mdl);
		preset_cbx.setAction(preset_act);
		preset_cbx.setRenderer(new PresetComboRenderer());
		loc_pnl.initialize();
		loc_pnl.add("camera.preset");
		loc_pnl.add(preset_cbx, Stretch.LAST);
		loc_pnl.add(new JButton(controller), Stretch.RIGHT);
		loc_pnl.setGeoLoc(proxy.getGeoLoc());
		return loc_pnl;
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		loc_pnl.dispose();
		state_pnl.dispose();
		super.dispose();
	}

	/** Create setup panel */
	private JPanel createSetupPanel() {
		lcs_type_cbx.setAction(lcs_type_act);
		IPanel p = new IPanel();
		p.add("device.notes");
		p.add(notes_txt, Stretch.LAST);
		p.add("lcs.type");
		p.add(lcs_type_cbx, Stretch.LAST);
		p.add("lcs.lane.shift");
		p.add(shift_spn, Stretch.LAST);
		p.add(state_pnl, Stretch.FULL);
		p.add(new JButton(settings), Stretch.CENTER);
		return p;
	}

	/** Create jobs for updating widgets */
	private void createUpdateJobs() {
		notes_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String n = notes_txt.getText().trim();
				proxy.setNotes((n.length() > 0) ? n : null);
			}
		});
		shift_spn.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Number n = (Number) shift_spn.getValue();
				proxy.setShift(n.intValue());
			}
		});
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		loc_pnl.updateEditMode();
		preset_act.setEnabled(canWrite("preset"));
		notes_txt.setEnabled(canWrite("notes"));
		lcs_type_act.setEnabled(canWrite("lcsType"));
		shift_spn.setEnabled(canWrite("shift"));
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		if (a == null || a.equals("preset"))
			preset_act.updateSelected();
		if (a == null || a.equals("controller"))
			controller.setEnabled(proxy.getController() != null);
		if (a == null || a.equals("notes")) {
			String n = proxy.getNotes();
			notes_txt.setText((n != null) ? n : "");
		}
		if (a == null || a.equals("lcsType"))
			lcs_type_act.updateSelected();
		if (a == null || a.equals("shift"))
			shift_spn.setValue(proxy.getShift());
	}
}
