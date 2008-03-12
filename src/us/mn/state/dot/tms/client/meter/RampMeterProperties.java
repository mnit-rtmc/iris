/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.meter;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.MeterPlan;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.TrafficDevice;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.DetectorForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.TrafficDeviceForm;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;
import us.mn.state.dot.tms.utils.ActionJob;
import us.mn.state.dot.tms.utils.TMSProxy;

/**
 * This is a form for viewing and editing the properties of a ramp meter.
 *
 * @author Douglas Lau
 */
public class RampMeterProperties extends TrafficDeviceForm {

	/** Frame title */
	static private final String TITLE = "Ramp Meter: ";

	/** Remote ramp meter object */
	protected RampMeter meter;

	/** Camera combo box */
	protected final JComboBox camera = new JComboBox();

	/** Field for Storage length (in feet). */
	protected final JTextField storage = new JTextField();

	/** Field for Maximum wait time (in seconds). */
	protected final JTextField wait = new JTextField();

	/** Ramp meter control mode component */
	protected final JComboBox controlMode =
		new JComboBox(RampMeter.MODE);

	/** Single release ramp meter check box */
	protected final JCheckBox singleRelease = new JCheckBox();

	/** Status component */
	protected final JLabel l_status = new JLabel();

	/** Minimum flow component */
	protected final JLabel minimum = new JLabel();

	/** Demand component */
	protected final JLabel demand = new JLabel();

	/** Metering on radio button */
	protected final JRadioButton meter_on = new JRadioButton("On");

	/** Metering off radio button */
	protected final JRadioButton meter_off = new JRadioButton("Off");

	/** Release rate component */
	protected final JLabel release = new JLabel();

	/** Cycle time component */
	protected final JLabel cycle = new JLabel();

	/** Queue component */
	protected final JLabel queue = new JLabel();

	/** Queue shrink button */
	protected final JButton shrink = new JButton("Shrink");

	/** Queue grow button */
	protected final JButton grow = new JButton("Grow");

	/** Meter locked button */
	protected final JCheckBox locked = new JCheckBox("Locked:");

	/** User who locked the meter */
	protected final JLabel lock_setter = new JLabel();

	/** Detail status description */
	protected final JLabel detail = new JLabel();

	/** View cycle time data for this meter */
	protected final JButton data = new JButton("Data");

	/** Timing plan table component */
	protected final JTable plan_table = new JTable();

	/** Add AM plan button */
	protected final JButton am_plan = new JButton("Add AM Plan");

	/** Add PM plan button */
	protected final JButton pm_plan = new JButton("Add PM Plan");

	/** Button to delete the selected timing plan */
	protected final JButton delete = new JButton("Delete");

	/** Stratified AM plan button */
	protected final JButton strat_am_plan =
		new JButton("Stratified AM Plan");

	/** Stratified PM plan button */
	protected final JButton strat_pm_plan =
		new JButton("Stratified PM Plan");

	/** Create a new ramp meter properties form */
	public RampMeterProperties(TmsConnection tc, String id) {
		super(TITLE + id, tc, id);
		ButtonGroup group = new ButtonGroup();
		group.add(meter_on);
		group.add(meter_off);
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		TMSProxy tms = connection.getProxy();
		SortedList s = (SortedList)tms.getMeterList();
		meter = (RampMeter)s.getElement(id);
		ListModel cameraModel = tms.getCameras().getModel();
		camera.setModel(new WrapperComboBoxModel(cameraModel));
		setDevice(meter);
		super.initialize();
		location.addRow("Camera", camera);
		JPanel panel = createSetupPanel();
		tab.add("Setup", panel);
		panel = createTimingPlanPanel();
		tab.add("Timing Plans", panel);
		panel = createStatusPanel();
		panel.setBorder(BORDER);
		tab.add("Status", panel);
	}

	/** Create ramp meter setup panel */
	protected JPanel createSetupPanel() {
		FormPanel panel = new FormPanel(admin);
		panel.addRow("Storage (feet)", storage);
		panel.addRow("Max Wait (seconds)", wait);
		panel.addRow("Control mode", controlMode);
		panel.addRow("Single Release", singleRelease);
		return panel;
	}

	/** Create timing plan panel */
	protected JPanel createTimingPlanPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BORDER);
		//plan_table.setPreferredScrollableViewportSize(
		//	new Dimension(200, 150));
		plan_table.setAutoCreateColumnsFromModel(false);
		final ListSelectionModel sel = plan_table.getSelectionModel();
		sel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting())
					return;
				delete.setEnabled(!sel.isSelectionEmpty());
			}
		} );
		DefaultTableCellRenderer renderer =
			new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		TableColumnModel colModel = new DefaultTableColumnModel();
		TableColumn column = new TableColumn(0);
		column.setHeaderValue("Plan Type");
		column.setCellRenderer(renderer);
		colModel.addColumn(column);
		column = new TableColumn(1);
		column.setHeaderValue("Start Time");
		column.setCellRenderer(renderer);
		colModel.addColumn(column);
		column = new TableColumn(2);
		column.setHeaderValue("Stop Time");
		column.setCellRenderer(renderer);
		colModel.addColumn(column);
		column = new TableColumn(3);
		column.setHeaderValue("Target Rate");
		column.setCellRenderer(renderer);
		colModel.addColumn(column);
		column = new TableColumn(4);
		column.setHeaderValue("Cycle Time");
		column.setCellRenderer(renderer);
		colModel.addColumn(column);
		column = new TableColumn(5);
		column.setHeaderValue("Active");
		colModel.addColumn(column);
		column = new TableColumn(6);
		column.setHeaderValue("Testing");
		colModel.addColumn(column);
		plan_table.setColumnModel(colModel);
		plan_table.setEnabled(admin);
		JScrollPane pane = new JScrollPane(plan_table);
		panel.add(pane);
		if(admin) {
			panel.add(Box.createHorizontalStrut(VGAP));
			Box vbox = Box.createVerticalBox();
			vbox.add(Box.createVerticalGlue());
			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
			box.add(am_plan);
			box.add(Box.createHorizontalStrut(HGAP));
			box.add(pm_plan);
			box.add(Box.createHorizontalStrut(HGAP));
			box.add(delete);
			box.add(Box.createHorizontalGlue());
			vbox.add(box);
			box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
			box.add(strat_am_plan);
			box.add(Box.createHorizontalStrut(HGAP));
			box.add(strat_pm_plan);
			box.add(Box.createHorizontalGlue());
			vbox.add(box);
			panel.add(vbox);
			new ActionJob(this, am_plan) {
				public void perform() throws Exception {
					meter.addSimpleTimingPlan(0);
					meter.notifyUpdate();
				}
			};
			new ActionJob(this, pm_plan) {
				public void perform() throws Exception {
					meter.addSimpleTimingPlan(1);
					meter.notifyUpdate();
				}
			};
			new ActionJob(this, delete) {
				public void perform() throws Exception {
					MeterPlan[] plans =
						meter.getTimingPlans();
					int[] p = plan_table.getSelectedRows();
					for(int i = 0; i < p.length; i++) {
						MeterPlan tp = plans[p[i]];
						meter.removeTimingPlan(tp);
					}
					if(p.length > 0)
						meter.notifyUpdate();
				}
			};
			new ActionJob(this, strat_am_plan) {
				public void perform() throws Exception {
					meter.addStratifiedTimingPlan(0);
					meter.notifyUpdate();
				}
			};
			new ActionJob(this, strat_pm_plan) {
				public void perform() throws Exception {
					meter.addStratifiedTimingPlan(1);
					meter.notifyUpdate();
				}
			};
		}
		return panel;
	}

	/** Create ramp meter status panel */
	protected JPanel createStatusPanel() {
		GridBagLayout lay = new GridBagLayout();
		JPanel panel = new JPanel(lay);
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.top = VGAP;
		bag.insets.right = HGAP;
		bag.anchor = GridBagConstraints.EAST;
		bag.gridx = 0;
		bag.gridy = GridBagConstraints.RELATIVE;
		JLabel label = new JLabel("Status:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Minimum:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Demand:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Metering:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Release rate:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Cycle time:");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridheight = 2;
		label = new JLabel("Queue:");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridheight = 1;
		lay.setConstraints(locked, bag);
		panel.add(locked);
		label = new JLabel("Detail status:");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.insets.right = 0;
		bag.anchor = GridBagConstraints.WEST;
		bag.gridx = 1;
		bag.gridy = 0;
		bag.gridheight = 1;
		bag.gridwidth = 2;
		lay.setConstraints(l_status, bag);
		panel.add(l_status);
		bag.gridy = GridBagConstraints.RELATIVE;
		lay.setConstraints(minimum, bag);
		panel.add(minimum);
		lay.setConstraints(demand, bag);
		panel.add(demand);
		bag.gridwidth = 1;
		lay.setConstraints(meter_on, bag);
		panel.add(meter_on);
		bag.gridwidth = 2;
		lay.setConstraints(release, bag);
		panel.add(release);
		lay.setConstraints(cycle, bag);
		panel.add(cycle);
		bag.gridheight = 2;
		bag.gridwidth = 1;
		lay.setConstraints(queue, bag);
		panel.add(queue);
		bag.gridheight = 1;
		bag.gridx = 2;
		bag.gridy = 3;
		lay.setConstraints(meter_off, bag);
		panel.add(meter_off);
		bag.gridy = 6;
		lay.setConstraints(shrink, bag);
		panel.add(shrink);
		bag.gridy = GridBagConstraints.RELATIVE;
		lay.setConstraints(grow, bag);
		panel.add(grow);
		bag.gridx = 1;
		bag.gridwidth = 2;
		bag.fill = GridBagConstraints.HORIZONTAL;
		lay.setConstraints(lock_setter, bag);
		panel.add(lock_setter);
		lay.setConstraints(detail, bag);
		panel.add(detail);
		new ActionJob(this, meter_on) {
			public void perform() throws Exception {
				meter.startMetering();
			}
		};
		new ActionJob(this, meter_off) {
			public void perform() throws Exception {
				meter.stopMetering();
			}
		};
		new ActionJob(this, shrink) {
			public void perform() throws Exception {
				meter.shrinkQueue();
			}
		};
		new ActionJob(this, grow) {
			public void perform() throws Exception {
				meter.growQueue();
			}
		};
		new ActionJob(this, locked) {
			public void perform() throws Exception {
				meter.setLocked(locked.isSelected(), "");
			}
		};
		return panel;
	}

	/** Update the form with the current state of the ramp meter */
	protected void doUpdate() throws RemoteException {
		super.doUpdate();
		TrafficDevice c = meter.getCamera();
		if(c != null)
			camera.setSelectedItem(c.getId());
		wait.setText("" + meter.getMaxWait());
		storage.setText("" + meter.getStorage());
		singleRelease.setSelected(meter.isSingleRelease());
		controlMode.setSelectedIndex(meter.getControlMode());
		plan_table.setModel(new TimingPlanModel(meter));
		am_plan.setEnabled(true);
		pm_plan.setEnabled(true);
		delete.setEnabled(false);
	}

	/** Refresh the status of the ramp meter */
	protected void doStatus() throws RemoteException {
		Color color = Color.GRAY;
		String s_status = UNKNOWN;
		String s_minimum = UNKNOWN;
		String s_demand = UNKNOWN;
		boolean metering = false;
		boolean meter_en = false;
		String s_release = UNKNOWN;
		String s_cycle = UNKNOWN;
		String s_queue = UNKNOWN;
		boolean is_locked = false;
		String s_lock_setter = "";
		String status_detail = UNKNOWN;
		try {
			s_status = meter.getStatus();
			if(s_status == null) {
				s_status = UNKNOWN;
				return;
			}
			s_minimum = meter.getMinimum() + " veh/hour";
			s_demand = meter.getDemand() + " veh/hour";
			int mode = meter.getControlMode();
			if(mode == RampMeter.MODE_STANDBY ||
				mode == RampMeter.MODE_CENTRAL)
			{
				meter_en = true;
			}
			if(meter.isMetering()) {
				metering = true;
				int r = meter.getReleaseRate();
				s_release = r + " veh/hour";
				int i_cycle = Math.round(36000.0f / r);
				s_cycle = (i_cycle / 10) + "." +
					(i_cycle % 10) + " seconds";
			}
			else {
				s_release = "N/A";
				s_cycle = "N/A";
			}
			if(meter.queueExists())
				s_queue = "Yes";
			else
				s_queue = "No";
			is_locked = meter.isLocked();
			if(is_locked)
				s_lock_setter = meter.getLock().getUser();
			status_detail = RampMeter.STATUS[meter.getStatusCode()];
			color = OK;
		}
		finally {
			l_status.setForeground(color);
			l_status.setText(s_status);
			minimum.setForeground(color);
			minimum.setText(s_minimum);
			demand.setForeground(color);
			demand.setText(s_demand);
			meter_on.setEnabled(meter_en & !metering);
			meter_on.setForeground(color);
			meter_on.setSelected(metering);
			meter_off.setEnabled(meter_en & metering);
			meter_off.setForeground(color);
			meter_off.setSelected(!metering);
			release.setForeground(color);
			release.setText(s_release);
			cycle.setForeground(color);
			cycle.setText(s_cycle);
			queue.setForeground(color);
			queue.setText(s_queue);
			shrink.setEnabled(meter_en & metering);
			grow.setEnabled(meter_en & metering);
			locked.setEnabled(meter_en);
			locked.setSelected(is_locked);
			lock_setter.setForeground(color);
			lock_setter.setText(s_lock_setter);
			detail.setForeground(color);
			detail.setText(status_detail);
		}
	}

	/** Apply button is pressed */
	protected void applyPressed() throws Exception {
		int det = 0;
		try {
			super.applyPressed();
			meter.checkStratifiedPlans();
			meter.setCamera((String)camera.getSelectedItem());
			meter.setSingleRelease(singleRelease.isSelected());
			meter.setControlMode(controlMode.getSelectedIndex());
			meter.setMaxWait(Integer.parseInt(wait.getText()));
			meter.setStorage(Integer.parseInt(storage.getText()));
		}
		finally { meter.notifyUpdate(); }
	}
}
