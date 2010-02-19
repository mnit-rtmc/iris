/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
import javax.swing.SwingUtilities;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.LCSArrayLock;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.toast.FormPanel;

/**
 * GUI for controlling a LaneControlSignal object.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class LcsDispatcher extends JPanel implements ProxyListener<LCSArray>,
	ProxySelectionListener<LCSArray>
{
	/** Font for "L" and "R" labels */
	static protected final Font FONT = new Font(null, Font.BOLD, 24);

	/** Lookup the DMS for the LCS in lane 1 */
	static protected DMS lookupDMS(LCSArray lcs_array) {
		LCS lcs = LCSArrayHelper.lookupLCS(lcs_array, 1);
		if(lcs != null)
			return DMSHelper.lookup(lcs.getName());
		else
			return null;
	}

	/** Current session */
	protected final Session session;

	/** Cache of LCS array proxy objects */
	protected final TypeCache<LCSArray> cache;

	/** Selection model */
	protected final ProxySelectionModel<LCSArray> selectionModel;

	/** Name of the selected LCS array */
	protected final JTextField nameTxt = FormPanel.createTextField();

	/** Verify camera name textfield */
	protected final JTextField cameraTxt = FormPanel.createTextField();

	/** Location of LCS array */
	protected final JTextField locationTxt = FormPanel.createTextField();

	/** Operation of selected LCS array */
	protected final JTextField operationTxt = FormPanel.createTextField();

	/** LCS lock combo box component */
	protected final JComboBox lcs_lock = new JComboBox(
		LCSArrayLock.getDescriptions());

	/** Panel for drawing an LCS array */
	protected final LCSArrayPanel lcsPnl = new LCSArrayPanel(58);

	/** LCS indicaiton selector */
	protected final IndicationSelector indicationSelector =
		new IndicationSelector();

	/** Button to send new indications to the LCS array */
	protected final JButton sendBtn = new JButton("Send");

	/** Button to blank the LCS array indications */
	protected final JButton blankBtn = new JButton();

	/** Action to blank selected LCS */
	protected final BlankLcsAction blankAction;

	/** Currently logged in user */
	protected final User user;

	/** Currently watching LCS */
	protected LCSArray watching;

	/** Create a new LCS dispatcher */
	public LcsDispatcher(Session s, LCSArrayManager manager) {
		super(new BorderLayout());
		session = s;
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
			"Selected Lane-Use Control Signal"));
		panel.add("Name", nameTxt);
		panel.addRow("Camera", cameraTxt);
		panel.addRow("Location", locationTxt);
		panel.addRow("Operation", operationTxt);
		panel.add("Lock", lcs_lock);
		panel.finishRow();
		panel.addRow(buildSelectorBox());
		panel.addRow(buildButtonPanel());
		return panel;
	}

	/** Build the indication selector box */
	protected Box buildSelectorBox() {
		Box box = Box.createHorizontalBox();
		Box vbox = Box.createVerticalBox();
		box.add(createLabel("L"));
		box.add(Box.createHorizontalStrut(4));
		vbox.add(lcsPnl);
		vbox.add(Box.createVerticalStrut(2));
		vbox.add(indicationSelector);
		box.add(vbox);
		box.add(Box.createHorizontalStrut(4));
		box.add(createLabel("R"));
		return box;
	}

	/** Create an "L" or "R" label */
	protected JLabel createLabel(String t) {
		JLabel label = new JLabel(t);
		label.setFont(FONT);
		return label;
	}

	/** Build the panel that holds the send and clear buttons */
	protected Box buildButtonPanel() {
		Box box = Box.createHorizontalBox();
		new ActionJob(sendBtn) {
			public void perform() {
				sendIndications();
			}
		};
		box.add(sendBtn);
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
			SwingUtilities.invokeLater(new Runnable() {
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
		indicationSelector.setLCSArray(lcs_array);
		indicationSelector.setEnabled(update);
		if(update) {
			lcs_lock.setAction(new LockLcsAction(lcs_array,
				lcs_lock));
		} else
			lcs_lock.setAction(null);
		lcs_lock.setEnabled(update);
		sendBtn.setEnabled(update);
		blankBtn.setEnabled(update);
		updateAttribute(lcs_array, null);
	}

	/** Disable the dispatcher widgets */
	protected void disableWidgets() {
		nameTxt.setText("");
		cameraTxt.setText("");
		locationTxt.setText("");
		operationTxt.setText("");
		operationTxt.setForeground(null);
		operationTxt.setBackground(null);
		lcs_lock.setEnabled(false);
		lcs_lock.setSelectedItem(null);
		indicationSelector.setEnabled(false);
		sendBtn.setEnabled(false);
		blankBtn.setEnabled(false);
		lcsPnl.clear();
	}

	/** Update one attribute on the form */
	protected void updateAttribute(LCSArray lcs_array, String a) {
		if(a == null || a.equals("name"))
			nameTxt.setText(lcs_array.getName());
		if(a == null || a.equals("camera")) {
			cameraTxt.setText(DMSHelper.getCameraName(
				lookupDMS(lcs_array)));
		}
		// FIXME: this won't update when geoLoc attributes change
		//        plus, geoLoc is not an LCSArray attribute
		if(a == null || a.equals("geoLoc")) {
			locationTxt.setText(LCSArrayHelper.lookupLocation(
				lcs_array));
		}
		if(a == null || a.equals("operation")) {
			String status = LCSArrayHelper.lookupStatus(lcs_array);
			if("".equals(status)) {
				operationTxt.setForeground(null);
				operationTxt.setBackground(null);
			} else {
				operationTxt.setForeground(Color.WHITE);
				operationTxt.setBackground(Color.GRAY);
			}
			String op = lcs_array.getOperation();
			operationTxt.setText(op);
			// These operations can be very slow -- discourage
			// users from sending multiple operations at once
			// RE: None -- see server.DeviceImpl.getOperation()
			sendBtn.setEnabled(canUpdate(lcs_array) &&
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
			indicationSelector.setIndications(ind);
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
