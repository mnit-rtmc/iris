/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import us.mn.state.dot.sched.SwingRunner;
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
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.IAction;
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
	static private final int LCS_SIZE = 48;

	/** Current session */
	protected final Session session;

	/** LCS Array manager */
	private final LCSArrayManager manager;

	/** Cache of LCS array proxy objects */
	protected final TypeCache<LCSArray> cache;

	/** Selection model */
	protected final ProxySelectionModel<LCSArray> selectionModel;

	/** Name of the selected LCS array */
	protected final JTextField nameTxt = FormPanel.createTextField();

	/** Verify camera button */
	private final JButton camera_btn = new JButton();

	/** Location of LCS array */
	protected final JTextField locationTxt = FormPanel.createTextField();

	/** Status of selected LCS array */
	protected final JTextField statusTxt = FormPanel.createTextField();

	/** Operation of selected LCS array */
	protected final JTextField operationTxt = FormPanel.createTextField();

	/** LCS lock combo box component */
	protected final JComboBox lcs_lock = new JComboBox(
		LCSArrayLock.getDescriptions());

	/** Lane configuration panel */
	private final LaneConfigurationPanel lane_config =
		new LaneConfigurationPanel(LCS_SIZE);

	/** Panel for drawing an LCS array */
	protected final LCSArrayPanel lcsPnl = new LCSArrayPanel(LCS_SIZE);

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
	protected LCSArray watching;

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
		if(watching != null) {
			cache.ignoreObject(watching);
			watching = null;
		}
		removeAll();
	}

	/** Create the dispatcher panel */
	protected JPanel createMainPanel() {
		FormPanel panel = new FormPanel(true);
		panel.setBorder(BorderFactory.createTitledBorder(
			I18N.get("lcs.selected")));
		panel.add(I18N.get("device.name"), nameTxt);
		panel.addRow(I18N.get("camera"), camera_btn);
		camera_btn.setBorder(BorderFactory.createEtchedBorder(
			EtchedBorder.LOWERED));
		panel.addRow(I18N.get("location"), locationTxt);
		panel.addRow(I18N.get("device.status"), statusTxt);
		panel.addRow(I18N.get("device.operation"), operationTxt);
//		panel.add(I18N.get("lcs.lock"), lcs_lock);
		panel.finishRow();
		panel.addRow(buildSelectorBox());
		panel.addRow(buildButtonPanel());
		return panel;
	}

	/** Build the indication selector box */
	protected Box buildSelectorBox() {
		Box box = Box.createHorizontalBox();
		box.add(createLabel(I18N.get("location.left")));
		box.add(Box.createHorizontalStrut(4));
		lane_config.add(lcsPnl);
		lane_config.add(indicationSelector);
		box.add(lane_config);
		box.add(Box.createHorizontalStrut(4));
		box.add(createLabel(I18N.get("location.right")));
		return box;
	}

	/** Create an "L" or "R" label */
	protected JLabel createLabel(String t) {
		JLabel label = new JLabel(t);
		Font f = label.getFont();
		label.setFont(f.deriveFont(2f * f.getSize2D()));
		return label;
	}

	/** Build the panel that holds the send and clear buttons */
	protected Box buildButtonPanel() {
		Box box = Box.createHorizontalBox();
		box.add(new JButton(send));
		box.add(Box.createHorizontalStrut(4));
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
		if(proxy == getSingleSelection()) {
			SwingRunner.invoke(new Runnable() {
				public void run() {
					updateAttribute(proxy, a);
				}
			});
		}
	}

	/** Get the selected LCS array (if a single array is selected) */
	protected LCSArray getSingleSelection() {
		if(selectionModel.getSelectedCount() == 1) {
			for(LCSArray lcs_array: selectionModel.getSelected())
				return lcs_array;
		}
		return null;
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
		disableWidgets();
	}

	/** Set the selected LCS array */
	public void setSelected(LCSArray lcs_array) {
		if(watching != null)
			cache.ignoreObject(watching);
		watching = lcs_array;
		cache.watchObject(watching);
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
		nameTxt.setText("");
		setCameraAction(null);
		locationTxt.setText("");
		statusTxt.setText("");
		statusTxt.setForeground(null);
		statusTxt.setBackground(null);
		operationTxt.setText("");
		lcs_lock.setEnabled(false);
		lcs_lock.setSelectedItem(null);
		indicationSelector.setEnabled(false);
		send.setEnabled(false);
		blankBtn.setEnabled(false);
		lcsPnl.clear();
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
			nameTxt.setText(lcs_array.getName());
		if(a == null || a.equals("camera"))
			setCameraAction(lcs_array);
		// FIXME: this won't update when geoLoc attributes change
		//        plus, geoLoc is not an LCSArray attribute
		if(a == null || a.equals("geoLoc")) {
			locationTxt.setText(LCSArrayHelper.lookupLocation(
				lcs_array));
		}
		if(a == null || a.equals("operation")) {
			updateStatus(lcs_array);
			String op = lcs_array.getOperation();
			operationTxt.setText(op);
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
			lcsPnl.setIndications(ind, lcs_array.getShift());
			lcsPnl.setClickHandler(new LCSArrayPanel.ClickHandler(){
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
			statusTxt.setForeground(Color.WHITE);
			statusTxt.setBackground(Color.GRAY);
			statusTxt.setText(LCSArrayHelper.getStatus(lcs_array));
		} else
			updateCritical(lcs_array);
	}

	/** Update the critical error status */
	protected void updateCritical(LCSArray lcs_array) {
		String critical = LCSArrayHelper.getCriticalError(lcs_array);
		if(critical.isEmpty())
			updateMaintenance(lcs_array);
		else {
			statusTxt.setForeground(Color.WHITE);
			statusTxt.setBackground(Color.BLACK);
			statusTxt.setText(critical);
		}
	}

	/** Update the maintenance error status */
	protected void updateMaintenance(LCSArray lcs_array) {
		String maintenance = LCSArrayHelper.getMaintenance(lcs_array);
		if(maintenance.isEmpty()) {
			statusTxt.setForeground(null);
			statusTxt.setBackground(null);
		} else {
			statusTxt.setForeground(Color.BLACK);
			statusTxt.setBackground(Color.YELLOW);
		}
		statusTxt.setText(maintenance);
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
