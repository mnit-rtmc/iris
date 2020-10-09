/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
package us.mn.state.dot.tms.client.alert;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsMsgPriority;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.IpawsAlert;
import us.mn.state.dot.tms.IpawsAlertDeployer;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.PushNotification;
import us.mn.state.dot.tms.PushNotificationHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.dms.DmsImagePanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.ITable;
import us.mn.state.dot.tms.client.widget.ITableModel;
import us.mn.state.dot.tms.client.widget.IWorker;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiConfig;

/**
 * An alert DMS dispatcher is a GUI panel for dispatching and reviewing
 * DMS involved in an automated alert deployment.
 * 
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class AlertDmsDispatcher extends IPanel {
	/** Client session */
	private final Session session;
	
	/** Alert manager */
	private final AlertManager manager;

	/** Currently selected alert (deployer) */
	private IpawsAlertDeployer selectedAlertDepl;
	
	/** Currently selected alert */
	private IpawsAlert selectedAlert;
	
	/** Cache of DMS */
	private final TypeCache<DMS> dmsCache;
	
	/** List of DMS included in this alert */
	private final ArrayList<DMS> dmsList = new ArrayList<DMS>();
	
	/** Table of DMS included in this alert */
	private final JTable dmsTable;
	
	/** A simple table model for DMS inclusion. Just supports 3 columns - the
	 *  DMS name, location, and whether or not it will be included in the
	 *  alert.
	 */
	private class DmsTableModel extends DefaultTableModel {
		
		public DmsTableModel() {
			addColumn(I18N.get("dms"));
			addColumn(I18N.get("location"));
			addColumn(I18N.get("alert.dms.included"));
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int col) {
			switch (col) {
			case 2:		// included
				return Boolean.class;
			default:	// name/location
				return String.class;
			}
		}
		
		@Override
		public boolean isCellEditable(int row, int col) {
			switch (col) {
			case 2:		// included
				return manager.getEditing();
			default:	// name/location
				return false;
			}
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			// get the DMS object from the list
			if (row >= 0 && row < dmsList.size()) {
				DMS dms = dmsList.get(row);
				switch (col) {
				case 0:		// name
					return dms.getName();
				case 1:		// location
					return GeoLocHelper.getLocation(dms.getGeoLoc());
				case 2:		// inclusion
					return dmsSuggestions.get(dms);
				}
			}
			return null;
		}
		
		@Override
		public void setValueAt(Object value, int row, int col) {
			// get the DMS object from the list - note that we can only set
			// column 2 (inclusion)
			if (row >= 0 && row < dmsList.size() && col == 2) {
				DMS dms = dmsList.get(row);
				dmsSuggestions.put(dms, !dmsSuggestions.get(dms));
			}
		}
	}
	
	/** DMS table model */
	private final DmsTableModel dmsTableModel;
	
	/** Fire for DMS list selection changes */
	private boolean fireSelectionChange = true;
	
	/** DMS selection listener */
	private final ListSelectionListener dmsSelectionListener =
			new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting() || !fireSelectionChange)
				return;
			int indx = dmsTable.getSelectedRow();
			if (indx >= 0 && indx < dmsList.size())
				setSelectedDms(dmsList.get(indx));
			else
				setSelectedDms(null);
		}
	};
	
	/** Select a DMS in the table */
	public void selectDmsInTable(DMS d) {
		int i = dmsList.indexOf(d);
		if (i != -1) {
			dmsTable.setRowSelectionInterval(i, i);
			dmsTable.scrollRectToVisible(new Rectangle(
					dmsTable.getCellRect(i, 0, true)));
		} else
			dmsTable.clearSelection();
	}
	
	/** DMS double-click listener (for centering map) */
	private MouseAdapter dmsMouseAdapter = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
			JTable t = (JTable) e.getSource();
			Point p = e.getPoint();
			int row = t.rowAtPoint(p);
			if (e.getClickCount() == 2 && t.getSelectedRow() != -1) {
				// center the map on the DMS
				DMS dms = dmsList.get(row);
				manager.centerMap(dms, 15);
			}
		}
	};
	
	/** Check box for controlling DMS visibility in edit mode */
	private JCheckBox showAllDmsChk;
	
	/** Text Box for MULTI string */
	private JTextArea multiBox;
	
	/** Button to preview message */
	private JButton previewBtn;
	
	/** Button to discard any changes made to the message text. */
	private JButton revertBtn;
	
	/** Label for message priority box */
	private JLabel msgPriorityLbl;
	
	/** Drop down for message priority */
	private JComboBox<DmsMsgPriority> msgPriorityCBox;
	
	/** i in circle character appended to pre/post alert time field labels
	 *  since they have tooltips
	 */
	private static final char TOOLTIP_ICON_CHAR = '\u24D8';
	
	/** Pre-alert time label */
	private JLabel preAlertTimeLbl;
	
	/** Pre-alert time text box */
	private JTextField preAlertTimeBx;
	
	/** Post-alert time label */
	private JLabel postAlertTimeLbl;
	
	/** Post-alert time text box */
	private JTextField postAlertTimeBx;
	
	/** Tab pane containing DMS renderings */
	private JTabbedPane dmsPane;
	
	/** Sign Image panel width */
	private static final int DMS_PNL_W = 200;
	
	/** Sign Image panel height */
	private static final int DMS_PNL_H = 80;
	
	/** Image panel for current message on sign */
	private DmsImagePanel currentMsgPnl;
	
	/** Image panel for alert message */
	private DmsImagePanel alertMsgPnl;
	
	/** Currently selected DMS */
	private DMS selectedDms;
	
	/** Map of displayed DMS and whether or not they are selected for
	 *  inclusion in the alert.
	 */
	private HashMap<DMS,Boolean> dmsSuggestions = new HashMap<DMS, Boolean>();
	
	/** Comparator for sorting list of DMS by name (case insensitive). */
	private class DmsComparator implements Comparator<DMS> {
		@Override
		public int compare(DMS dms0, DMS dms1) {
			return String.CASE_INSENSITIVE_ORDER.compare(
					dms0.getName(), dms1.getName());
		}
	}
	
	/** Action triggered with the "show all DMS" check box */
	private IAction showAllDms = new IAction("alert.dms.show_all") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			// update the DMS list - it will handle everything
			updateDmsList();
			
			// also tell the manager so the map gets updated
			manager.setShowAllDms(showAllDmsChk.isSelected());
			manager.updateMap();
		}
	};
	
	/** Create a new alert DMS dispatcher */
	public AlertDmsDispatcher(Session s, AlertManager m) {
		session = s;
		manager = m;
		
		// setup DMS table
		dmsCache = session.getSonarState().getDmsCache().getDMSs();
		
		dmsTableModel = new DmsTableModel();
		dmsTable = new JTable(dmsTableModel);
		Dimension d = new Dimension(dmsTable.getPreferredSize().width,
				dmsTable.getRowHeight() * 10);
		dmsTable.setPreferredScrollableViewportSize(d);
		dmsTable.getSelectionModel().addListSelectionListener(
				dmsSelectionListener);
		
		dmsTable.addMouseListener(dmsMouseAdapter);
		
		// DMS/message control options (disabled until alert selected)
		showAllDmsChk = new JCheckBox(showAllDms);
		multiBox = new JTextArea(2,28);
		multiBox.setLineWrap(true);
		multiBox.setWrapStyleWord(true);
		previewBtn = new JButton(previewMsg);
		revertBtn = new JButton(revertMsg);
		previewBtn.setToolTipText(I18N.get("alert.dms.preview.tooltip"));
		revertBtn.setToolTipText(I18N.get("alert.dms.revert.tooltip"));
		msgPriorityLbl = new JLabel(I18N.get("alert.dms.msg_priority"));
		
		showAllDmsChk.setEnabled(false);
		multiBox.setEnabled(false);
		previewBtn.setEnabled(false);
		revertBtn.setEnabled(false);
		
		// show message priority values from highest to lowest
		List<DmsMsgPriority> mpList = Arrays.asList(DmsMsgPriority.values());
		Collections.reverse(mpList);
		DmsMsgPriority[] mpValues = mpList.toArray(new DmsMsgPriority[0]);
		msgPriorityCBox = new JComboBox<DmsMsgPriority>(mpValues);
		msgPriorityCBox.setSelectedIndex(-1);
		msgPriorityCBox.setEnabled(false);
		
		// pre/post alert time boxes
		preAlertTimeLbl = new JLabel(I18N.get("alert.config.pre_alert_time")
				+ TOOLTIP_ICON_CHAR);
		preAlertTimeLbl.setToolTipText(
				I18N.get("alert.pre_alert_time.tooltip"));
		preAlertTimeBx = new JTextField(2);
		postAlertTimeLbl = new JLabel(I18N.get(
				"alert.config.post_alert_time") + TOOLTIP_ICON_CHAR);
		postAlertTimeLbl.setToolTipText(
				I18N.get("alert.post_alert_time.tooltip"));
		postAlertTimeBx = new JTextField(2);
		preAlertTimeBx.setEnabled(false);
		postAlertTimeBx.setEnabled(false);
		
		// setup image panels for rendering DMS
		dmsPane = new JTabbedPane();
		currentMsgPnl = new DmsImagePanel(DMS_PNL_W, DMS_PNL_H, true);
		alertMsgPnl = new DmsImagePanel(DMS_PNL_W, DMS_PNL_H, true);
		
		// TODO these should be centered
		dmsPane.add(I18N.get("alert.dms.current_msg"), currentMsgPnl);
		dmsPane.add(I18N.get("alert.dms.alert_msg"), alertMsgPnl);
	}
	
	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		setTitle(I18N.get("alert.dms"));
		add(new JScrollPane(dmsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), Stretch.DOUBLE);
		
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p2.add(showAllDmsChk);
		p.add(p2);
		p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p2.add(new JScrollPane(multiBox,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		p2.add(previewBtn);
		p.add(p2);
		p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p2.add(msgPriorityLbl);
		p2.add(msgPriorityCBox);
		p2.add(preAlertTimeLbl);
		p2.add(preAlertTimeBx);
		p2.add(postAlertTimeLbl);
		p2.add(postAlertTimeBx);
		p.add(p2);
		setRowCol(row+1, 0);
		add(p, Stretch.DOUBLE);
		
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createTitledBorder(
				I18N.get("alert.dms.selected")));
		p.add(dmsPane);
		setRowCol(row+1, 0);
		add(p, Stretch.DOUBLE);
	}
	
	/** Set the selected alert, updating the list of DMS. */
	public void setSelectedAlert(IpawsAlertDeployer iad, IpawsAlert ia) {
		selectedAlertDepl = iad;
		selectedAlert = ia;
		
		// check the style of the alert deployer to see if it's pending (and
		// that the user has permission to write alerts)
		if (manager.checkStyle(ItemStyle.PENDING, iad)
				&& session.isWritePermitted(iad)) {
			// if it is, automatically turn on edit mode
			manager.setEditing(true);
		} else if (manager.checkStyle(ItemStyle.PAST, iad))
			// if the alert is in the past, force edit mode to off
			manager.setEditing(false);
		
		// uncheck the "show all DMS" button
		showAllDmsChk.setSelected(false);
		
		// update the DMS list given the selected alert and edit mode state
		updateDmsList();
		
		// update message parameters from the alert deployer
		updateMsgParams();
	}
	
	/** Process a press of the edit/deploy button, starting edit mode or
	 *  deploying the alert.
	 */
	public void processEditDeploy() {
		if (manager.getEditing())
			// deploy or update the alert
			deployAlert();
		else
			// start editing the alert
			editAlert();
		
		// update the DMS list, message parameters, and map
		updateDmsList();
		updateMsgParams();
		manager.updateMap();
	}
	
	/** Deploy (or update) the selected alert. */
	private void deployAlert() {
		if (selectedAlert != null && selectedAlertDepl != null
				&& !manager.checkStyle(ItemStyle.PAST,  selectedAlertDepl)) {
			System.out.println("Deploying alert " +
					selectedAlert.getName() + "...");
			
			// get the selected DMS and set the deployed DMS
			ArrayList<String> selectedDms = new ArrayList<String>();
			for (DMS d: dmsSuggestions.keySet()) {
				if (dmsSuggestions.get(d))
					selectedDms.add(d.getName());
			}
			String[] dms = selectedDms.toArray(new String[0]);
			selectedAlertDepl.setDeployedDms(dms);
			
			// try to update MULTI and pre/post alert times
			String newMulti = multiBox.getText();
			if (newMulti != null && !newMulti.isEmpty())
				selectedAlertDepl.setDeployedMulti(multiBox.getText());
			int preAlertTime = -1;
			try {
				preAlertTime = Integer.valueOf(preAlertTimeBx.getText());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			if (preAlertTime >= 0)
				selectedAlertDepl.setPreAlertTime(preAlertTime);
			int postAlertTime = -1;
			try {
				postAlertTime = Integer.valueOf(postAlertTimeBx.getText());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			if (postAlertTime >= 0)
				selectedAlertDepl.setPostAlertTime(postAlertTime);
			
			// update approval time/user
			selectedAlertDepl.setApprovedTime(new Date());
			selectedAlertDepl.setApprovedBy(session.getUser().getName());
			
			// set the deployed state - this triggers the deployment/update
			selectedAlertDepl.setDeployed(true);
			
			// update the style counts in case the alert was pending
			manager.updateStyleCounts();
			
			// check if there are any notifications that haven't been addressed
			// and address them
			PushNotificationHelper.addressAllRef(selectedAlertDepl, session);
		}
		manager.setEditing(false);
	}
	
	/** Cancel the selected alert (removing it from signs) */
	public void cancelAlert() {
		// set deployed to false to trigger the cancel
		if (selectedAlertDepl != null) {
			selectedAlertDepl.setDeployed(false);
			// check if there are any notifications that haven't been addressed
			// and address them
			PushNotificationHelper.addressAllRef(selectedAlertDepl, session);
		}
		manager.updateStyleCounts();
	}
	
	/** Start editing the selected alert. */
	private void editAlert() {
		// make sure the alert is not in the past and that the user can write
		// (this) alert
		if (!manager.checkStyle(ItemStyle.PAST, selectedAlertDepl)
				&& session.isWritePermitted(selectedAlertDepl)) {
			// if they can, set edit mode
			manager.setEditing(true);
		} else
			manager.setEditing(false);
	}
	
	/** Cancel editing of an alert. */
	public void cancelEdit() {
		manager.setEditing(false);
		updateDmsList();
		updateMsgParams();
		manager.updateMap();
	}
	
	/** Clear the selected alert. */
	public void clearSelectedAlert() {
		selectedAlertDepl = null;
		selectedAlert = null;
		
		// empty the DMS table, clear the msg params, and update the map
		dmsTableModel.setRowCount(0);
		updateMsgParams();
		manager.updateMap();
	}
	
	/** Update the DMS list model with the list of DMS provided. Runs a
	 *  SwingWorker to get DMS info before updating the GUI.
	 */
	private void updateDmsList() {
		// clear the suggestions before starting the worker
		dmsSuggestions.clear();
		
		// get the DMS list from the alert - do it in a worker thread since
		// it might take a second
		IWorker<HashMap<DMS,Boolean>> dmsWorker =
				new IWorker<HashMap<DMS,Boolean>>() {
			@Override
			protected HashMap<DMS,Boolean> doInBackground()
					throws Exception {
				// wait a bit before updating
				Thread.sleep(200);
				
				// use a HashMap to support a check box for controlling
				// inclusion in the alert and a list to control sorting
				HashMap<DMS,Boolean> dm = new HashMap<DMS,Boolean>();
				
				// if edit mode is on, show a list of auto and optional DMS
				if (manager.getEditing()) {
					String[] dmsAuto = selectedAlertDepl.getAutoDms();
					String[] dmsOptional = selectedAlertDepl.getOptionalDms();
					
					// if deployed already, preserve inclusion
					String[] dmsDeployed = selectedAlertDepl.getDeployedDms();
					HashSet<String> include = new HashSet<String>();
					for (String n: dmsDeployed) {
						if (n != null)
							include.add(n);
					}
					
					// add all the auto DMS
					for (String n: dmsAuto) {
						if (n != null) {
							DMS d = dmsCache.lookupObject(n);
							if (d != null) {
								dm.put(d, include.isEmpty()
										|| include.contains(n));
							}
						}
					}
					
					// add any optional DMS not already included
					for (String n: dmsOptional) {
						if (n != null) {
							DMS d = dmsCache.lookupObject(n);
							if (!dm.containsKey(d) && (d != null))
								dm.put(d, include.contains(n));
						}
					}
					
					// if the "show all" check box is checked, add all DMS in
					// the group
					if (showAllDmsChk.isSelected() &&
							selectedAlertDepl.getSignGroup() != null) {
						ArrayList<DMS> groupDms = DmsSignGroupHelper.
							getSignsInGroup(selectedAlertDepl.getSignGroup());
						for (DMS d: groupDms) {
							if (!dm.containsKey(d))
								dm.put(d, include.contains(d.getName()));
						}
					}
				} else {
					// if edit mode is off, show a list of deployed DMS
					for (String n: selectedAlertDepl.getDeployedDms()) {
						if (n != null) {
							DMS d = dmsCache.lookupObject(n);
							if (d != null)
								dm.put(d, true);
						}
					}
				}
				return dm;
			}
			
			@Override
			public void done() {
				// get the map, get a list of DMS from it, then sort by name
				dmsSuggestions = getResult();
				
				if (dmsSuggestions != null) {
					// use a separate list to sort by name
					dmsList.clear();
					for (DMS d: dmsSuggestions.keySet())
						dmsList.add(d);
					dmsList.sort(new DmsComparator());
					
					// fill the list and table model
					for (DMS d: dmsList) {
						Object[] r = {d};
						dmsTableModel.addRow(r);
					}
					dmsTableModel.setRowCount(dmsList.size());
					dmsTableModel.fireTableDataChanged();
				}
			}
		};
		dmsWorker.execute();
	}
	
	/** Get the most appropriate MULTI string for the selected alert. This is
	 *  either the deployed MULTI (if not empty) or the auto-generated MULTI.
	 */
	private String getAlertMulti() {
		String deplMulti = selectedAlertDepl.getDeployedMulti();
		if (deplMulti != null && !deplMulti.isEmpty())
			return deplMulti;
		return selectedAlertDepl.getAutoMulti();
	}
	
	/** Update the message parameters from the selected alert deployer. */
	private void updateMsgParams() {
		boolean allowEdit = false;
		if (selectedAlertDepl != null) {
			showAllDmsChk.setText(I18N.get("alert.dms.show_all") +
					" " + selectedAlertDepl.getSignGroup());
			
			// set the text in the MULTI box - use the deployed MULTI if we
			// have it, otherwise the auto-generated MULTI
			multiBox.setText(getAlertMulti());
			
			// set the message priority
			Integer mpi = selectedAlertDepl.getMsgPriorty();
			if (mpi != null) {
				DmsMsgPriority mp = DmsMsgPriority.fromOrdinal(mpi);
				msgPriorityCBox.setSelectedItem(mp);
			}
			
			// set the pre/post alert times
			preAlertTimeBx.setText(String.valueOf(
					selectedAlertDepl.getPreAlertTime()));
			postAlertTimeBx.setText(String.valueOf(
					selectedAlertDepl.getPostAlertTime()));
			
			// if this alert is in the past or editing is off, disable the
			// editing features (we still show them for information)
			allowEdit = manager.getEditing() && !manager.checkStyle(
					ItemStyle.PAST, selectedAlertDepl);
			
		} else {
			showAllDmsChk.setText(I18N.get("alert.dms.show_all"));
			multiBox.setText("");
			msgPriorityCBox.setSelectedIndex(-1);
			preAlertTimeBx.setText("");
			postAlertTimeBx.setText("");
		}
		showAllDmsChk.setEnabled(allowEdit);
		multiBox.setEnabled(allowEdit);
		previewBtn.setEnabled(allowEdit);
		msgPriorityCBox.setEnabled(allowEdit);
		preAlertTimeBx.setEnabled(allowEdit);
		postAlertTimeBx.setEnabled(allowEdit);
	}
	
	/** Set the selected DMS */
	public void setSelectedDms(DMS dms) {
		selectedDms = dms;
		if (selectedDms != null) {
			// set the image on the DMS panels
			try {
				// first set the MultiConfig for this sign
				MultiConfig mc = MultiConfig.from(selectedDms);
				currentMsgPnl.setMultiConfig(mc);
				alertMsgPnl.setMultiConfig(mc);
				
				// then set the MULTI of the message (both current and alert)
				currentMsgPnl.setMulti(DMSHelper.getMultiString(selectedDms));
				
				// use the text in the MULTI box in case the user has edited
				// it (allows them to flip through signs more quickly)
				alertMsgPnl.setMulti(multiBox.getText());
			} catch (TMSException e1) {
				e1.printStackTrace();
			}
		} else {
			// if deselected, clear
			currentMsgPnl.clearMultiConfig();
			currentMsgPnl.clearMulti();
			alertMsgPnl.clearMultiConfig();
			alertMsgPnl.clearMulti();
		}
		
		// update the map to show the selected DMS
		manager.setSelectedDms(selectedDms);
		manager.updateMap();
	}
	
	/** Action to preview a message entered into the MULTI box. Switches the
	 *  DMS Pane to the "Alert Msg" pane and sets the MULTI on that panel.
	 */
	private IAction previewMsg = new IAction("alert.dms.preview") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			dmsPane.setSelectedComponent(alertMsgPnl);
			String multi = multiBox.getText();
			alertMsgPnl.setMulti(multi);
		}
		
	};

	/** Action to revert the message in the MULTI box back to the original
	 *  MULTI (either deployed or auto).
	 */
	private IAction revertMsg = new IAction("alert.dms.revert") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			dmsPane.setSelectedComponent(alertMsgPnl);
			String multi = getAlertMulti();
			multiBox.setText(multi);
			alertMsgPnl.setMulti(multi);
		}
		
	};
}















