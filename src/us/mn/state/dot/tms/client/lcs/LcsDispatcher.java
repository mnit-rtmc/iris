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
package us.mn.state.dot.tms.client.lcs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import static us.mn.state.dot.sched.SwingRunner.runSwing;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.LCSArrayLock;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.CameraSelectAction;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.roads.LaneConfigurationPanel;
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * GUI for controlling a LaneControlSignal object.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class LcsDispatcher extends JPanel implements ProxyListener<LCSArray>,
	ProxySelectionListener<LCSArray>
{
	/** Size in pixels for each LCS in array */
	static private final int LCS_SIZE = UI.scaled(44);

	/** Current session */
	protected final Session session;

	/** LCS Array manager */
	private final LCSArrayManager manager;

	/** Cache of LCS array proxy objects */
	protected final TypeCache<LCSArray> cache;

	/** Selection model */
	protected final ProxySelectionModel<LCSArray> selectionModel;

	/** Name of the selected LCS array */
	private final JLabel name_lbl = FormPanel.createValueLabel();

	/** Verify camera button */
	private final JButton camera_btn = new JButton();

	/** Location of LCS array */
	private final JLabel location_lbl = FormPanel.createValueLabel();

	/** Status of selected LCS array */
	private final JLabel status_lbl = FormPanel.createValueLabel();

	/** Operation of selected LCS array */
	private final JLabel operation_lbl = FormPanel.createValueLabel();

	/** LCS lock combo box component */
	protected final JComboBox lcs_lock = new JComboBox(
		LCSArrayLock.getDescriptions());

	/** Lane configuration panel */
	private final LaneConfigurationPanel lane_config =
		new LaneConfigurationPanel(LCS_SIZE, true);

	/** Panel for drawing an LCS array */
	private final LCSArrayPanel lcs_pnl = new LCSArrayPanel(LCS_SIZE);

	/** LCS indicaiton selector */
	protected final IndicationSelector indicationSelector =
		new IndicationSelector(LCS_SIZE);

	/** Action to send new indications to the LCS array */
	private final IAction send = new IAction("lcs.send") {
		protected void do_perform() {
			sendIndications();
		}
	};

	/** Button to blank the LCS array indications */
	protected final JButton blankBtn = new JButton();

	/** Action to blank selected LCS */
	protected final BlankLcsAction blankAction;

	/** Currently logged in user */
	protected final User user;

	/** Currently watching LCS */
	private LCSArray watching;

	/** Watch an LCS array */
	private void watch(final LCSArray nw) {
		final LCSArray ow = watching;
		if(ow != null)
			cache.ignoreObject(ow);
		watching = nw;
		if(nw != null)
			cache.watchObject(nw);
	}

	/** Create a new LCS dispatcher */
	public LcsDispatcher(Session s, LCSArrayManager m) {
		super(new BorderLayout());
		session = s;
		manager = m;
		cache = session.getSonarState().getLcsCache().getLCSArrays();
		user = session.getUser();
		selectionModel = manager.getSelectionModel();
		blankAction = new BlankLcsAction(selectionModel, user);
		blankBtn.setAction(blankAction);
		manager.setBlankAction(blankAction);
		add(createMainPanel(), BorderLayout.CENTER);
		clearSelected();
		cache.addProxyListener(this);
		selectionModel.addProxySelectionListener(this);
	}

	/** Dispose of the LCS dispatcher */
	public void dispose() {
		selectionModel.removeProxySelectionListener(this);
		cache.removeProxyListener(this);
		indicationSelector.dispose();
		clearSelected();
		removeAll();
	}

	/** Create the dispatcher panel */
	protected JPanel createMainPanel() {
		FormPanel panel = new FormPanel();
		panel.setBorder(BorderFactory.createTitledBorder(
			I18N.get("lcs.selected")));
		camera_btn.setBorder(BorderFactory.createEtchedBorder(
			EtchedBorder.LOWERED));
		panel.setHeavy(true);
		panel.add(I18N.get("device.name"), name_lbl);
		panel.setHeavy(false);
		panel.addRow(I18N.get("camera"), camera_btn);
		panel.addRow(I18N.get("location"), location_lbl);
		panel.addRow(I18N.get("device.status"), status_lbl);
		// Make label opaque so that we can set the background color
		status_lbl.setOpaque(true);
		panel.addRow(I18N.get("device.operation"), operation_lbl);
//		panel.add(I18N.get("lcs.lock"), lcs_lock);
		panel.finishRow();
		panel.addRow(buildSelectorBox());
		panel.addRow(createButtonPanel());
		return panel;
	}

	/** Build the indication selector */
	private JPanel buildSelectorBox() {
		lane_config.add(Box.createVerticalStrut(UI.vgap));
		lane_config.add(lcs_pnl);
		lane_config.add(Box.createVerticalStrut(UI.vgap));
		lane_config.add(indicationSelector);
		lane_config.add(Box.createVerticalStrut(UI.vgap));
		return lane_config;
	}

	/** Create the button panel */
	private Box createButtonPanel() {
		Box box = Box.createHorizontalBox();
		box.add(new JButton(send));
		box.add(Box.createHorizontalStrut(UI.hgap));
		box.add(blankBtn);
		return box;
	}

	/** A new proxy has been added */
	public void proxyAdded(LCSArray proxy) {
		// we're not interested
	}

	/** Enumeration of the proxy type has completed */
	public void enumerationComplete() {
		// we're not interested
	}

	/** A proxy has been removed */
	public void proxyRemoved(LCSArray proxy) {
		// Note: the LCSArrayManager will remove the proxy from the
		//       ProxySelectionModel, so we can ignore this.
	}

	/** A proxy has been changed */
	public void proxyChanged(final LCSArray proxy, final String a) {
		if(proxy == selectionModel.getSingleSelection()) {
			runSwing(new Runnable() {
				public void run() {
					updateAttribute(proxy, a);
				}
			});
		}
	}

	/** Called whenever a sign is added to the selection */
	public void selectionAdded(LCSArray s) {
		updateSelected();
	}

	/** Called whenever a sign is removed from the selection */
	public void selectionRemoved(LCSArray s) {
		updateSelected();
	}

	/** Update the selected sign(s) */
	protected void updateSelected() {
		List<LCSArray> selected = selectionModel.getSelected();
		if(selected.size() == 1) {
			for(LCSArray lcs_array: selected)
				setSelected(lcs_array);
		} else
			clearSelected();
	}

	/** Clear the selection */
	protected void clearSelected() {
		watch(null);
		disableWidgets();
	}

	/** Set the selected LCS array */
	public void setSelected(LCSArray lcs_array) {
		watch(lcs_array);
		boolean update = canUpdate(lcs_array);
		lane_config.setConfiguration(manager.laneConfiguration(
			lcs_array));
		indicationSelector.setLCSArray(lcs_array);
		indicationSelector.setEnabled(update);
		if(update) {
			lcs_lock.setAction(new LockLcsAction(lcs_array,
				lcs_lock));
		} else
			lcs_lock.setAction(null);
		lcs_lock.setEnabled(update);
		send.setEnabled(update);
		blankBtn.setEnabled(update);
		updateAttribute(lcs_array, null);
	}

	/** Disable the dispatcher widgets */
	protected void disableWidgets() {
		name_lbl.setText("");
		setCameraAction(null);
		location_lbl.setText("");
		status_lbl.setText("");
		status_lbl.setForeground(null);
		status_lbl.setBackground(null);
		operation_lbl.setText("");
		lcs_lock.setEnabled(false);
		lcs_lock.setSelectedItem(null);
		indicationSelector.setEnabled(false);
		send.setEnabled(false);
		blankBtn.setEnabled(false);
		lcs_pnl.clear();
		lane_config.clear();
	}

	/** Set the camera action */
	protected void setCameraAction(LCSArray lcs_array) {
		Camera cam = LCSArrayHelper.getCamera(lcs_array);
		camera_btn.setAction(new CameraSelectAction(cam,
			session.getCameraManager().getSelectionModel()));
	}

	/** Update one attribute on the form */
	protected void updateAttribute(final LCSArray lcs_array, String a) {
		if(a == null || a.equals("name"))
			name_lbl.setText(lcs_array.getName());
		if(a == null || a.equals("camera"))
			setCameraAction(lcs_array);
		// FIXME: this won't update when geoLoc attributes change
		//        plus, geoLoc is not an LCSArray attribute
		if(a == null || a.equals("geoLoc")) {
			location_lbl.setText(LCSArrayHelper.lookupLocation(
				lcs_array));
		}
		if(a == null || a.equals("operation")) {
			updateStatus(lcs_array);
			String op = lcs_array.getOperation();
			operation_lbl.setText(op);
			// These operations can be very slow -- discourage
			// users from sending multiple operations at once
			// RE: None -- see server.DeviceImpl.getOperation()
			send.setEnabled(canUpdate(lcs_array) &&
				op.equals("None"));
		}
		if(a == null || a.equals("lcsLock")) {
			Integer lk = lcs_array.getLcsLock();
			if(lk != null)
				lcs_lock.setSelectedIndex(lk);
			else
				lcs_lock.setSelectedIndex(0);
		}
		if(a == null || a.equals("indicationsCurrent")) {
			Integer[] ind = lcs_array.getIndicationsCurrent();
			lcs_pnl.setIndications(ind, lcs_array.getShift());
			lcs_pnl.setClickHandler(
				new LCSArrayPanel.ClickHandler()
			{
				public void handleClick(int lane) {
					selectDMS(lcs_array, lane);
				}
			});
			indicationSelector.setIndications(ind);
		}
	}

	/** Update the status widgets */
	protected void updateStatus(LCSArray lcs_array) {
		if(LCSArrayHelper.isFailed(lcs_array)) {
			status_lbl.setForeground(Color.WHITE);
			status_lbl.setBackground(Color.GRAY);
			status_lbl.setText(LCSArrayHelper.getStatus(lcs_array));
		} else
			updateCritical(lcs_array);
	}

	/** Update the critical error status */
	protected void updateCritical(LCSArray lcs_array) {
		String critical = LCSArrayHelper.getCriticalError(lcs_array);
		if(critical.isEmpty())
			updateMaintenance(lcs_array);
		else {
			status_lbl.setForeground(Color.WHITE);
			status_lbl.setBackground(Color.BLACK);
			status_lbl.setText(critical);
		}
	}

	/** Update the maintenance error status */
	protected void updateMaintenance(LCSArray lcs_array) {
		String maintenance = LCSArrayHelper.getMaintenance(lcs_array);
		if(maintenance.isEmpty()) {
			status_lbl.setForeground(null);
			status_lbl.setBackground(null);
		} else {
			status_lbl.setForeground(Color.BLACK);
			status_lbl.setBackground(Color.YELLOW);
		}
		status_lbl.setText(maintenance);
	}

	/** Select the DMS for the specified lane */
	protected void selectDMS(LCSArray lcs_array, int lane) {
		LCS lcs = LCSArrayHelper.lookupLCS(lcs_array, lane);
		if(lcs != null) {
			DMS dms = DMSHelper.lookup(lcs.getName());
			if(dms != null) {
				session.getDMSManager().getSelectionModel().
					setSelected(dms);
			}
		}
	}

	/** Send new indications to the selected LCS array */
	protected void sendIndications() {
		List<LCSArray> selected = selectionModel.getSelected();
		if(selected.size() == 1) {
			for(LCSArray lcs_array: selected)
				sendIndications(lcs_array);
		}
	}

	/** Send new indications to the specified LCS array */
	protected void sendIndications(LCSArray lcs_array) {
		Integer[] indications = indicationSelector.getIndications();
		if(indications != null) {
			lcs_array.setOwnerNext(user);
			lcs_array.setIndicationsNext(indications);
		}
	}

	/** Check if the user can update the given LCS array */
	protected boolean canUpdate(LCSArray lcs_array) {
		return session.canUpdate(lcs_array, "indicationsNext") &&
		       session.canUpdate(lcs_array, "ownerNext");
	}
}
