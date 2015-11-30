/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toll;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.TagReader;
import us.mn.state.dot.tms.TollZone;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.utils.I18N;

/**
 * TagReaderProperties is a dialog for entering and editing tag readers.
 *
 * @author Douglas Lau
 */
public class TagReaderProperties extends SonarObjectForm<TagReader> {

	/** Location panel */
	private final LocationPanel loc_pnl;

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(3, 24);

	/** Toll zone combobox */
	private final JComboBox toll_zone_cbx = new JComboBox();

	/** Toll zone model */
	private final IComboBoxModel<TollZone> toll_zone_mdl;

	/** First DMS */
	private final JTextField dms_1 = new JTextField(10);

	/** Second DMS */
	private final JTextField dms_2 = new JTextField(10);

	/** Third DMS */
	private final JTextField dms_3 = new JTextField(10);

	/** Toll zone action */
	private final IAction toll_zone_act = new IAction("toll_zone") {
		@Override
		protected void doActionPerformed(ActionEvent e) {
			proxy.setTollZone(toll_zone_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			toll_zone_mdl.setSelectedItem(proxy.getTollZone());
		}
	};

	/** Controller action */
	private final IAction controller = new IAction("controller") {
		protected void doActionPerformed(ActionEvent e) {
			Controller c = proxy.getController();
			if (c != null)
				showForm(new ControllerForm(session, c));
		}
	};

	/** Status panel */
	private final IPanel status_pnl;

	/** Send settings action */
	private final IAction settings = new IAction("device.send.settings") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setDeviceRequest(DeviceRequest.
				SEND_SETTINGS.ordinal());
		}
	};

	/** Create a new tag reader properties form */
	public TagReaderProperties(Session s, TagReader tr) {
		super(I18N.get("tag_reader") + ": ", s, tr);
		toll_zone_mdl = new IComboBoxModel<TollZone>(
			s.getSonarState().getTollZoneModel());
		loc_pnl = new LocationPanel(s);
		status_pnl = new IPanel();
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<TagReader> getTypeCache() {
		return state.getTagReaders();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), createLocationPanel());
		tab.add(I18N.get("device.status"), createStatusPanel());
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
		toll_zone_cbx.setModel(toll_zone_mdl);
		toll_zone_cbx.setAction(toll_zone_act);
		loc_pnl.initialize();
		loc_pnl.add("device.notes");
		loc_pnl.add(notes_txt, Stretch.FULL);
		loc_pnl.add(new JButton(controller), Stretch.RIGHT);
		loc_pnl.add("toll_zone");
		loc_pnl.add(toll_zone_cbx, Stretch.LEFT);
		loc_pnl.add("dms");
		loc_pnl.add(dms_1);
		loc_pnl.add(dms_2);
		loc_pnl.add(dms_3, Stretch.RIGHT);
		loc_pnl.setGeoLoc(proxy.getGeoLoc());
		return loc_pnl;
	}

	/** Create the status panel */
	private IPanel createStatusPanel() {
		status_pnl.initialize();
		status_pnl.add(new JButton(settings), Stretch.RIGHT);
		return status_pnl;
	}

	/** Create the widget jobs */
	private void createUpdateJobs() {
		notes_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				proxy.setNotes(notes_txt.getText());
			}
		});
		dms_1.addFocusListener(dms_update);
		dms_2.addFocusListener(dms_update);
		dms_3.addFocusListener(dms_update);
	}

	/** DMS update job */
	private final FocusAdapter dms_update = new FocusAdapter() {
		@Override
		public void focusLost(FocusEvent e) {
			DMS d1 = DMSHelper.lookup(dms_1.getText());
			DMS d2 = DMSHelper.lookup(dms_2.getText());
			DMS d3 = DMSHelper.lookup(dms_3.getText());
			LinkedList<DMS> ds = new LinkedList<DMS>();
			if (d1 != null)
				ds.add(d1);
			if (d2 != null)
				ds.add(d2);
			if (d3 != null)
				ds.add(d3);
			proxy.setSigns(ds.toArray(new DMS[0]));
		}
	};

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		loc_pnl.updateEditMode();
		notes_txt.setEnabled(canUpdate("notes"));
		toll_zone_act.setEnabled(canUpdate("toll_zone"));
		boolean ud = canUpdate("signs");
		dms_1.setEnabled(ud);
		dms_2.setEnabled(ud);
		dms_3.setEnabled(ud);
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		if (a == null || a.equals("controller"))
			controller.setEnabled(proxy.getController() != null);
		if (a == null || a.equals("notes"))
			notes_txt.setText(proxy.getNotes());
		if (a == null || a.equals("toll_zone"))
			toll_zone_act.updateSelected();
		if (a == null || a.equals("signs"))
			updateSigns();
		if (a == null) {
			boolean r = canRequest();
			settings.setEnabled(r);
		}
	}

	/** Update the signs */
	private void updateSigns() {
		DMS[] ds = proxy.getSigns();
		dms_1.setText((ds.length > 0) ? ds[0].getName() : "");
		dms_2.setText((ds.length > 1) ? ds[1].getName() : "");
		dms_3.setText((ds.length > 2) ? ds[2].getName() : "");
	}

	/** Check if the user can make device requests */
	private boolean canRequest() {
		return isUpdatePermitted("deviceRequest");
	}
}
