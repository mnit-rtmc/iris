/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
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

import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsHelper;
import us.mn.state.dot.tms.LcsLock;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.CameraPresetAction;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.roads.LaneConfigurationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * GUI for dispatching LCS arrays.
 *
 * @author Douglas Lau
 */
public class LcsDispatcher extends IPanel implements ProxyView<Lcs> {

	/** Size in pixels for each LCS in array */
	static private final int LCS_SIZE = UI.scaled(44);

	/** Current session */
	private final Session session;

	/** LCS Array manager */
	private final LcsManager manager;

	/** Selection model */
	private final ProxySelectionModel<Lcs> sel_mdl;

	/** Selection listener */
	private final ProxySelectionListener sel_listener =
		new ProxySelectionListener()
	{
		public void selectionChanged() {
			setSelected(sel_mdl.getSingleSelection());
		}
	};

	/** Name of the selected LCS array */
	private final JLabel name_lbl = createValueLabel();

	/** Verify camera preset button */
	private final JButton preset_btn = new JButton();

	/** Location of LCS array */
	private final JLabel location_lbl = createValueLabel();

	/** Status of selected LCS array */
	private final JLabel status_lbl = createValueLabel();

	/** Operation of selected LCS array */
	private final JLabel operation_lbl = createValueLabel();

	/** Reason the LCS array was locked */
	private final JComboBox<String> reason_cbx = new JComboBox<String>(
		LcsLock.REASONS);

	/** Lane configuration panel */
	private final LaneConfigurationPanel lane_config =
		new LaneConfigurationPanel(LCS_SIZE, true);

	/** Panel for drawing an LCS array */
	private final LcsPanel lcs_pnl = new LcsPanel(LCS_SIZE);

	/** LCS indicaiton selector */
	private final IndicationSelector ind_selector =
		new IndicationSelector(LCS_SIZE);

	/** Action to send new indications to the LCS array */
	private final IAction send = new IAction("lcs.send") {
		protected void doActionPerformed(ActionEvent e) {
			sendIndications();
		}
	};

	/** Button to blank the LCS array indications */
	private final JButton blank_btn = new JButton();

	/** Currently logged in user */
	private final String user;

	/** Proxy watcher */
	private final ProxyWatcher<Lcs> watcher;

	/** Currently selected LCS array */
	private Lcs lcs;

	/** Create a new LCS dispatcher */
	public LcsDispatcher(Session s, LcsManager m) {
		session = s;
		manager = m;
		user = session.getUser().getName();
		sel_mdl = manager.getSelectionModel();
		TypeCache<Lcs> cache =
			session.getSonarState().getLcsCache().getLcss();
		watcher = new ProxyWatcher<Lcs>(cache, this, true);
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		preset_btn.setBorder(BorderFactory.createEtchedBorder(
			EtchedBorder.LOWERED));
		// Make label opaque so that we can set the background color
		status_lbl.setOpaque(true);
		setTitle(I18N.get("lcs.selected"));
		add("device.name");
		add(name_lbl);
		add("camera");
		add(preset_btn, Stretch.LAST);
		add("location");
		add(location_lbl, Stretch.LAST);
		add("device.status");
		add(status_lbl, Stretch.LAST);
		add("device.operation");
		add(operation_lbl, Stretch.LAST);
		add("lcs.lock");
		add(reason_cbx, Stretch.LAST);
		add(buildSelectorBox(), Stretch.FULL);
		add(createButtonPanel(), Stretch.RIGHT);
		watcher.initialize();
		clear();
		sel_mdl.addProxySelectionListener(sel_listener);
	}

	/** Build the indication selector */
	private JPanel buildSelectorBox() {
		lane_config.add(Box.createVerticalStrut(UI.vgap));
		lane_config.add(lcs_pnl);
		lane_config.add(Box.createVerticalStrut(UI.vgap));
		lane_config.add(ind_selector);
		lane_config.add(Box.createVerticalStrut(UI.vgap));
		return lane_config;
	}

	/** Create the button panel */
	private Box createButtonPanel() {
		Box box = Box.createHorizontalBox();
		box.add(new JButton(send));
		box.add(Box.createHorizontalStrut(UI.hgap));
		box.add(blank_btn);
		return box;
	}

	/** Dispose of the LCS dispatcher */
	@Override
	public void dispose() {
		watcher.dispose();
		sel_mdl.removeProxySelectionListener(sel_listener);
		ind_selector.dispose();
		clear();
		super.dispose();
	}

	/** Set the selected LCS array */
	public void setSelected(Lcs l) {
		watcher.setProxy(l);
	}

	/** Called when all proxies have been enumerated */
	@Override
	public void enumerationComplete() { }

	/** Update the proxy view */
	@Override
	public void update(final Lcs l, String a) {
		lcs = l;
		if (a == null)
			updateConfig(l);
		if (a == null || a.equals("name"))
			name_lbl.setText(l.getName());
		if (a == null || a.equals("preset"))
			setPresetAction(l);
		if (a == null || a.equals("geoLoc")) {
			location_lbl.setText(
				GeoLocHelper.getLocation(l.getGeoLoc())
			);
		}
		if (a == null || a.equals("operation")) {
			updateStatus(l);
			String op = l.getOperation();
			operation_lbl.setText(op);
			// These operations can be very slow -- discourage
			// users from sending multiple operations at once
			// RE: None -- see server.DeviceImpl.getOperation()
			send.setEnabled(isWritePermitted(l) &&
				op.equals("None"));
		}
		if (a == null || a.equals("lock"))
			updateLock(l);
		if (a == null || a.equals("status")) {
			int[] ind = LcsHelper.getIndications(l);
			lcs_pnl.setIndications(ind, l.getShift());
			lcs_pnl.setClickHandler(new LcsPanel.ClickHandler() {
				public void handleClick(int lane) {
					// FIXME: selectDMS(l, lane);
				}
			});
			ind_selector.setIndications(ind);
		}
	}

	/** Update the LCS array config */
	private void updateConfig(Lcs l) {
		lane_config.setConfiguration(manager.laneConfiguration(l));
		ind_selector.setLcs(l);
		boolean update = isWritePermitted(l);
		ind_selector.setEnabled(update);
		send.setEnabled(update);
	}

	/** Update the LCS array config */
	private void updateLock(Lcs l) {
		// Remove action so we can update the lock reason in peace
		reason_cbx.setAction(null);
		LcsLock lk = new LcsLock(l.getLock());
		String r = lk.optReason();
		reason_cbx.setSelectedItem((r != null) ? r : "");
		LockReasonAction reason_act = new LockReasonAction(l, user,
			reason_cbx);
		BlankLcsAction blank_act = new BlankLcsAction(l, user);
		if (!isWritePermitted(l)) {
			reason_act.setEnabled(false);
			blank_act.setEnabled(false);
		}
		reason_cbx.setAction(reason_act);
		blank_btn.setAction(blank_act);
		manager.setBlankAction(blank_act);
	}

	/** Set the camera preset action */
	private void setPresetAction(Lcs l) {
		CameraPreset cp = (l != null) ? l.getPreset() : null;
		preset_btn.setAction(new CameraPresetAction(session, cp));
	}

	/** Update the status widgets */
	private void updateStatus(Lcs l) {
		String status = LcsHelper.optFaults(l);
		if (LcsHelper.isOffline(l)) {
			status_lbl.setForeground(Color.WHITE);
			status_lbl.setBackground(Color.GRAY);
			status = "OFFLINE";
		} else if (status != null) {
			status_lbl.setForeground(Color.WHITE);
			status_lbl.setBackground(Color.BLACK);
		} else {
			status_lbl.setForeground(null);
			status_lbl.setBackground(null);
		}
		status_lbl.setText((status != null) ? status : "");
	}

	/** Clear the proxy view. */
	@Override
	public void clear() {
		lcs = null;
		name_lbl.setText("");
		setPresetAction(null);
		location_lbl.setText("");
		status_lbl.setText("");
		status_lbl.setForeground(null);
		status_lbl.setBackground(null);
		operation_lbl.setText("");
		ind_selector.setEnabled(false);
		send.setEnabled(false);
		lcs_pnl.clear();
		lane_config.clear();
		updateLock(null);
	}

	/** Send new indications to the selected LCS array */
	private void sendIndications() {
		Lcs l = lcs;
		if (l != null)
			sendIndications(l);
	}

	/** Send new indications to the specified LCS array */
	private void sendIndications(Lcs l) {
		int[] ind = ind_selector.getIndications();
		if (ind != null) {
			LcsLock lk = new LcsLock(l.getLock());
			lk.setUser(user);
			lk.setIndications(ind);
			l.setLock(lk.toString());
		}
	}

	/** Check if the user is permitted to update the given LCS array */
	private boolean isWritePermitted(Lcs l) {
		return isWritePermitted(l, "lock");
	}

	/** Check if the user is permitted to update a given LCS attribute */
	private boolean isWritePermitted(Lcs l, String aname) {
		return session.isWritePermitted(l, aname);
	}
}
