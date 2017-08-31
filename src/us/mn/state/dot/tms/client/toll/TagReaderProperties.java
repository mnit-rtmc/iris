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
package us.mn.state.dot.tms.client.toll;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
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
	private final JComboBox<TollZone> toll_zone_cbx =
		new JComboBox<TollZone>();

	/** Toll zone model */
	private final IComboBoxModel<TollZone> toll_zone_mdl;

	/** DMS text field (for adding) */
	private final JTextField dms_txt = new JTextField(10);

	/** DMS list model */
	private final DefaultListModel<DMS> dms_mdl =
		new DefaultListModel<DMS>();

	/** DMS list */
	private final JList<DMS> dms_lst = new JList<DMS>(dms_mdl);

	/** Link DMS action */
	private final IAction link_btn = new IAction("tag_reader.link") {
		protected void doActionPerformed(ActionEvent e) {
			DMS d = DMSHelper.lookup(dms_txt.getText());
			if (d != null) {
				insertIntoModel(d);
				updateSignLinks();
			}
			dms_txt.setText("");
		}
	};

	/** Insert a DMS into the link model */
	private void insertIntoModel(DMS d) {
		for (int i = 0; i < dms_mdl.size(); i++) {
			DMS s = dms_mdl.get(i);
			if (d.getName().compareTo(s.getName()) < 0) {
				dms_mdl.add(i, d);
				return;
			}
		}
		dms_mdl.addElement(d);
	}

	/** Unlink DMS action */
	private final IAction unlink_btn = new IAction("tag_reader.unlink") {
		protected void doActionPerformed(ActionEvent e) {
			int s = dms_lst.getSelectedIndex();
			if (s >= 0) {
				dms_mdl.remove(s);
				updateSignLinks();
			}
		}
	};

	/** Update sign links */
	private void updateSignLinks() {
		DMS[] signs = new DMS[dms_mdl.size()];
		dms_mdl.copyInto(signs);
		proxy.setSigns(signs);
	}

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
		dms_lst.setVisibleRowCount(6);
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
		loc_pnl.add(dms_txt);
		loc_pnl.add(new JLabel());
		loc_pnl.add(dms_lst, Stretch.TALL);
		loc_pnl.add(new JLabel());
		loc_pnl.add(new JButton(link_btn));
		loc_pnl.add(new JButton(unlink_btn));
		loc_pnl.add(new JLabel(), Stretch.LEFT);
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
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		loc_pnl.updateEditMode();
		notes_txt.setEnabled(canWrite("notes"));
		toll_zone_act.setEnabled(canWrite("toll_zone"));
		boolean ud = canWrite("signs");
		dms_txt.setEnabled(ud);
		dms_lst.setEnabled(ud);
		link_btn.setEnabled(ud);
		unlink_btn.setEnabled(ud);
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
		dms_txt.setText("");
		DMS[] signs = proxy.getSigns();
		dms_mdl.clear();
		for (DMS d: signs)
			dms_mdl.addElement(d);
	}

	/** Check if the user can make device requests */
	private boolean canRequest() {
		return isUpdatePermitted("deviceRequest");
	}
}
