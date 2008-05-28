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
package us.mn.state.dot.tms.client.dms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;

import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsMessage;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.TimingPlan;
import us.mn.state.dot.tms.TimingPlanList;
import us.mn.state.dot.tms.TrafficDevice;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.TMSObjectForm;
import us.mn.state.dot.tms.client.toast.TrafficDeviceForm;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;
import us.mn.state.dot.tms.utils.TMSProxy;

import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * This is a form for viewing and editing the properties of a dynamic message
 * sign (DMS).
 *
 * @author Douglas Lau
 */
public class DMSProperties extends TrafficDeviceForm {

	/** Format a number for display */
	static protected String formatNumber(int i) {
		if(i > 0)
			return String.valueOf(i);
		else
			return UNKNOWN;
	}

	/** Format milimeter units for display */
	static protected String formatMM(int i) {
		if(i > 0)
			return i + " mm";
		else
			return UNKNOWN;
	}

	/** Format miliamp units for display */
	static protected String formatMA(int i) {
		if(i > 0)
			return i + " (" + (i / 2f) + " ma)";
		else
			return UNKNOWN;
	}

	/** Format pixel units for display */
	static protected String formatPixels(int i) {
		if(i > 0)
			return i + " pixels";
		else if(i == 0)
			return "Variable";
		else
			return UNKNOWN;
	}

	/** Celcius temperature string */
	static protected final String CELCIUS = "\u00B0 C";

	/** Format the temperature */
	static protected String formatTemp(int minTemp, int maxTemp) {
		if(maxTemp == DMS.UNKNOWN_TEMP)
			maxTemp = minTemp;
		if(minTemp == maxTemp) {
			if(minTemp == DMS.UNKNOWN_TEMP)
				return UNKNOWN;
			else
				return "" + minTemp + CELCIUS;
		} else
			return "" + minTemp + "..." + maxTemp + CELCIUS;
	}

	/** Default LedStar brightness table */
	static protected final int[] LEDSTAR_DEFAULT = {
		512, 0, 100,
		6400, 0, 10,
		16384, 10, 20,
		24576, 20, 30,
		32768, 30, 50,
		40960, 50, 70,
		51200, 70, 255
	};

	/** Frame title */
	static protected final String TITLE = "DMS: ";

	/** Remote dynamic message sign interface */
	protected DMS sign;

	/** Camera combo box */
	protected final JComboBox camera = new JComboBox();

	/** Mile Point field */
	protected final JTextField milePoint = new JTextField(10);

	/** Message table model */
	protected DmsMessageModel mess_model;

	/** Model for message line number spinner */
	protected final SpinnerNumberModel spin_model =
		new SpinnerNumberModel(1, 1, 6, 1);

	/** Message line number spinner */
	protected final JSpinner line_spin = new JSpinner(spin_model);

	/** Message global checkbox */
	protected final JCheckBox global = new JCheckBox("Global");

	/** Button to insert a message */
	protected final JButton insert_mess = new JButton("Insert");

	/** Button to delete a message */
	protected final JButton delete_mess = new JButton("Delete");

	/** Message table component */
	protected final JTable mess_table = new JTable();

	/** Travel time template string field */
	protected final JTextArea travel = new JTextArea();

	/** Make label */
	protected final JLabel make = new JLabel();

	/** Model label */
	protected final JLabel model = new JLabel();

	/** Version label */
	protected final JLabel version = new JLabel();

	/** Sign access label */
	protected final JLabel access = new JLabel();

	/** Sign type label */
	protected final JLabel type = new JLabel();

	/** Sign height label */
	protected final JLabel height = new JLabel();

	/** Sign width label */
	protected final JLabel width = new JLabel();

	/** Horizontal border label */
	protected final JLabel hBorder = new JLabel();

	/** Vertical border label */
	protected final JLabel vBorder = new JLabel();

	/** Sign legend label */
	protected final JLabel legend = new JLabel();

	/** Beacon label */
	protected final JLabel beacon = new JLabel();

	/** Sign technology label */
	protected final JLabel tech = new JLabel();

	/** Character height label */
	protected final JLabel cHeight = new JLabel();

	/** Character width label */
	protected final JLabel cWidth = new JLabel();

	/** Sign height (pixels) label */
	protected final JLabel pHeight = new JLabel();

	/** Sign width (pixels) label */
	protected final JLabel pWidth = new JLabel();

	/** Horizontal pitch label */
	protected final JLabel hPitch = new JLabel();

	/** Vertical pitch label */
	protected final JLabel vPitch = new JLabel();

	/** Label to display LDC pot base */
	protected final JLabel ldcPotBase = new JLabel();

	/** Spinner to adjuct LDC pot base */
	protected final JSpinner ldcPotBaseSpn = new JSpinner(
		new SpinnerNumberModel(20, 20, 65, 5));

	/** Pixel current low label */
	protected final JLabel currentLow = new JLabel();

	/** Pixel current low threshold spinner */
	protected final JSpinner currentLowSpn = new JSpinner(
		new SpinnerNumberModel(5, 0, 100, 1));

	/** Pixel current high label */
	protected final JLabel currentHigh = new JLabel();

	/** Pixel current high threshold spinner */
	protected final JSpinner currentHighSpn = new JSpinner(
		new SpinnerNumberModel(40, 0, 100, 1));

	/** Bad pixel limit */
	protected final JLabel badLimit = new JLabel();

	/** Bad pixel limit spinner */
	protected final JSpinner badLimitSpn = new JSpinner(
		new SpinnerNumberModel(35, 0, 2625, 5));

	/** Operation description label */
	protected final JLabel operation = new JLabel();

	/** Power supply status table */
	protected final JTable power_table = new JTable();

	/** Pixel test activation button */
	protected final JButton pixelTest = new JButton("Pixel test");

	/** Status test activation button */
	//protected final JButton statusTest = new JButton("Status test");  mtod

	/** Lamp test activation button */
//	protected final JButton lampTest = new JButton("Lamp test");

	/** Fan test activation button */
	protected final JButton fanTest = new JButton(I18NMessages.i18nMessages.getString ("OK"));

	/** Bad pixel count label */
	protected final JLabel badPixels = new JLabel();

	/** Lamp status label */
	protected final JLabel lamp = new JLabel();

	/** Fan status label */
	protected final JLabel fan = new JLabel();

	/** Heat tape status label */
	protected final JLabel heat_tape = new JLabel();

	/** Cabinet temperature label */
	protected final JLabel cabinetTemp = new JLabel();

	/** Ambient temperature label */
	protected final JLabel ambientTemp = new JLabel();

	/** Housing temperature label */
	protected final JLabel housingTemp = new JLabel();

	/** Brightness table */
	protected BrightnessTable b_table;

	/** Timing plan table component */
	protected final JTable plan_table = new JTable();

	/** Add AM plan button */
	protected final JButton am_plan = new JButton("Add AM Plan");

	/** Add PM plan button */
	protected final JButton pm_plan = new JButton("Add PM Plan");

	/** Photocell brightness control */
	protected final JRadioButton con_photocell =
		new JRadioButton("Photocell control");

	/** Manual brightness control button */
	protected final JRadioButton con_manual =
		new JRadioButton("Manual control");

	/** Form object */
	protected final TMSObjectForm form = this;

	/** Array of timing plans */
	protected TimingPlan[] plans;

	/** Create a new DMS properties from */
	public DMSProperties(TmsConnection tc, String id) {
		super(TITLE + id, tc, id);
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		TMSProxy tms = connection.getProxy();
		SortedList s = tms.getDMSList();
		sign = (DMS)s.getElement(id);
		ListModel model = tms.getCameras().getModel();
		camera.setModel(new WrapperComboBoxModel(model));
		mess_table.setColumnModel(DmsMessageModel.createColumnModel());
		TimingPlanModel plan_model = new TimingPlanModel(
			(TimingPlanList)tms.getTimingPlans().getList(), sign,
			admin);
		plan_table.setModel(plan_model);
		plan_table.setColumnModel(TimingPlanModel.createColumnModel());
		b_table = new BrightnessTable(sign, admin);
		setDevice(sign);
		super.initialize();
		location.addRow("Milepoint", milePoint);
		location.addRow("Camera", camera);
		tab.add("Messages", createMessagePanel());
		tab.add("Travel Time", createTravelTimePanel());
		tab.add("Configuration", createConfigurationPanel());
		tab.add("Brightness", createBrightnessPanel());
		tab.add("Ledstar", createLedstarPanel());
		tab.add("Status", createStatusPanel());
	}

	/** Add an action to the delete button */
	protected void addDeleteAction() {
		new ActionJob(this, delete_mess) {
			public void perform() throws Exception {
				int r = mess_table.getSelectedRow();
				if(r < 0)
					return;
				DmsMessage m = mess_model.getRowMessage(r);
				sign.deleteMessage(m);
				mess_model = new DmsMessageModel(sign,
					connection.isAdmin(),
					connection.isTiger());
				mess_table.setModel(mess_model);
				sign.notifyUpdate();
			}
		};
	}

	/** Create the message panel */
	protected JPanel createMessagePanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BORDER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		mess_table.setAutoCreateColumnsFromModel(false);
		JScrollPane scroll = new JScrollPane(mess_table);
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(scroll);
		box.add(Box.createHorizontalGlue());
		if(connection.isAdmin() || connection.isTiger()) {
			JPanel vbox = new JPanel(new GridBagLayout());
			GridBagConstraints bag = new GridBagConstraints();
			bag.insets.top = 10;
			bag.gridx = 0;
			vbox.add(line_spin, bag);
			if(admin)
				vbox.add(global, bag);
			vbox.add(insert_mess, bag);
			new ActionJob(this, insert_mess) {
				public void perform() throws Exception {
					boolean g = global.isSelected();
					Number l = (Number)line_spin.getValue();
					sign.insertMessage(g, l.shortValue());
					sign.notifyUpdate();
				}
			};
			if(admin) {
				vbox.add(delete_mess, bag);
				addDeleteAction();
			}
			box.add(vbox);
		}
		panel.add(box);
		return panel;
	}

	/** Create the travel time panel */
	protected JPanel createTravelTimePanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BORDER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(new JLabel("Travel template:"));
		box.add(travel);
		box.add(Box.createHorizontalGlue());
		travel.setEnabled(admin);
		panel.add(box);
		panel.add(Box.createVerticalStrut(VGAP));
		plan_table.setAutoCreateColumnsFromModel(false);
		plan_table.setPreferredScrollableViewportSize(
			new Dimension(300, 200));
		JScrollPane scroll = new JScrollPane(plan_table);
		box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(scroll);
		box.add(Box.createHorizontalGlue());
		panel.add(box);
		if(admin) {
			box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
			box.add(am_plan);
			box.add(Box.createHorizontalStrut(8));
			box.add(pm_plan);
			box.add(Box.createHorizontalGlue());
			panel.add(box);
			new ActionJob(this, am_plan) {
				public void perform() throws Exception {
					sign.addTimingPlan(TimingPlan.AM);
				}
			};
			new ActionJob(this, pm_plan) {
				public void perform() throws Exception {
					sign.addTimingPlan(TimingPlan.PM);
				}
			};
		}
		return panel;
	}

	/** Create the configuration panel */
	protected JPanel createConfigurationPanel() {
		GridBagLayout lay = new GridBagLayout();
		JPanel panel = new JPanel(lay);
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 0;
		bag.gridy = 6;
		bag.weightx = 0.6f;
		lay.setConstraints(make, bag);
		panel.add(make);
		bag.gridy = GridBagConstraints.RELATIVE;
		lay.setConstraints(model, bag);
		panel.add(model);
		lay.setConstraints(version, bag);
		panel.add(version);
		bag.gridx = 1;
		bag.gridy = 0;
		bag.anchor = GridBagConstraints.EAST;
		bag.weightx = 0.5f;
		bag.fill = GridBagConstraints.NONE;
		JLabel label = new JLabel("Access:");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridy = GridBagConstraints.RELATIVE;
		label = new JLabel("Type:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Sign height:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Sign height:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Sign width:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Sign width:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Horizontal border:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Vertical border:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Legend:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Beacon:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Technology:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Character height:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Character width:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Horizontal pitch:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Vertical pitch:");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridx = 2;
		bag.gridy = 0;
		bag.anchor = GridBagConstraints.WEST;
		bag.insets.left = 2;
		lay.setConstraints(access, bag);
		panel.add(access);
		bag.gridy = GridBagConstraints.RELATIVE;
		lay.setConstraints(type, bag);
		panel.add(type);
		lay.setConstraints(height, bag);
		panel.add(height);
		lay.setConstraints(pHeight, bag);
		panel.add(pHeight);
		lay.setConstraints(width, bag);
		panel.add(width);
		lay.setConstraints(pWidth, bag);
		panel.add(pWidth);
		lay.setConstraints(hBorder, bag);
		panel.add(hBorder);
		lay.setConstraints(vBorder, bag);
		panel.add(vBorder);
		lay.setConstraints(legend, bag);
		panel.add(legend);
		lay.setConstraints(beacon, bag);
		panel.add(beacon);
		lay.setConstraints(tech, bag);
		panel.add(tech);
		lay.setConstraints(cHeight, bag);
		panel.add(cHeight);
		lay.setConstraints(cWidth, bag);
		panel.add(cWidth);
		lay.setConstraints(hPitch, bag);
		panel.add(hPitch);
		lay.setConstraints(vPitch, bag);
		panel.add(vPitch);
		return panel;
	}

	/** Create Ledstar-specific panel */
	protected JPanel createLedstarPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.top = 2;
		bag.insets.bottom = 2;
		bag.gridx = 0;
		bag.gridy = GridBagConstraints.RELATIVE;
		bag.anchor = GridBagConstraints.EAST;
		panel.add(new JLabel("LDC pot base: "), bag);
		panel.add(new JLabel("Pixel current low threshold: "), bag);
		panel.add(new JLabel("Pixel current high threshold: "), bag);
		panel.add(new JLabel("Bad pixel limit: "), bag);
		bag.gridx = 1;
		bag.anchor = GridBagConstraints.WEST;
		panel.add(ldcPotBase, bag);
		panel.add(currentLow, bag);
		panel.add(currentHigh, bag);
		panel.add(badLimit, bag);
		if(admin) {
			bag.gridx = 2;
			bag.anchor = GridBagConstraints.EAST;
			panel.add(ldcPotBaseSpn, bag);
			panel.add(currentLowSpn, bag);
			panel.add(currentHighSpn, bag);
			panel.add(badLimitSpn, bag);
			bag.gridx = 0;
			bag.gridwidth = 3;
			JButton send = new JButton("Set values");
			panel.add(send, bag);
			new ActionJob(this, send) {
				public void perform() throws Exception {
					setLedstarValues();
				}
			};
		}
		return panel;
	}

	/** Set the Ledstar pixel configuration values on the sign */
	protected void setLedstarValues() throws RemoteException {
		int base = ((Integer)ldcPotBaseSpn.getValue()).intValue();
		int low = ((Integer)currentLowSpn.getValue()).intValue();
		int high = ((Integer)currentHighSpn.getValue()).intValue();
		int bad = ((Integer)badLimitSpn.getValue()).intValue();
		sign.setLdcPotBase(base);
		sign.setPixelCurrentLow(low);
		sign.setPixelCurrentHigh(high);
		sign.setBadPixelLimit(bad);
		sign.notifyUpdate();
	}

	/** Create status panel */
	protected JPanel createStatusPanel() {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
		pane.setBorder(BORDER);
		Box box = Box.createVerticalBox();
		box.add(Box.createVerticalGlue());
		power_table.setAutoCreateColumnsFromModel(false);
		power_table.setPreferredScrollableViewportSize(
			new Dimension(300, 200));
		JScrollPane scroll = new JScrollPane(power_table);
		box.add(scroll);
		box.add(Box.createVerticalGlue());
		pane.add(box);
		GridBagLayout lay = new GridBagLayout();
		JPanel panel = new JPanel(lay);
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 1;
		bag.gridy = 0;
		lay.setConstraints(pixelTest, bag);
		panel.add(pixelTest);
		new ActionJob(this, pixelTest) {
			public void perform() throws Exception {
				sign.testPixels();
			}
		};
		bag.gridy = GridBagConstraints.RELATIVE;
/*		lay.setConstraints(lampTest, bag);
		panel.add(lampTest);
		new ActionJob(this, lampTest) {
			public void perform() throws Exception {
				sign.testLamps();
			}
		}; */
		lay.setConstraints(fanTest, bag);
		panel.add(fanTest);
		new ActionJob(this, fanTest) {
			public void perform() throws Exception {
				sign.testFans();
			}
		};
		bag.gridx = 2;
		bag.gridy = 0;
		bag.anchor = GridBagConstraints.EAST;
		JLabel label = new JLabel("Bad pixels:");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridy = GridBagConstraints.RELATIVE;
		label = new JLabel("Lamp status:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Fan status:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Heat tape:");
		bag.insets.top = 10;
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Cabinet temp:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Ambient temp:");
		bag.insets.top = 0;
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Housing temp:");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.insets.top = 10;
		label = new JLabel("Operation:");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridx = 3;
		bag.gridy = 0;
		bag.anchor = GridBagConstraints.WEST;
		bag.insets.top = 0;
		bag.insets.left = 2;
		lay.setConstraints(badPixels, bag);
		panel.add(badPixels);
		bag.gridy = GridBagConstraints.RELATIVE;
		lay.setConstraints(lamp, bag);
		panel.add(lamp);
		lay.setConstraints(fan, bag);
		panel.add(fan);
		bag.insets.top = 10;
		lay.setConstraints(heat_tape, bag);
		panel.add(heat_tape);
		lay.setConstraints(cabinetTemp, bag);
		panel.add(cabinetTemp);
		bag.insets.top = 0;
		lay.setConstraints(ambientTemp, bag);
		panel.add(ambientTemp);
		lay.setConstraints(housingTemp, bag);
		panel.add(housingTemp);
		bag.insets.top = 10;
		lay.setConstraints(operation, bag);
		panel.add(operation);
		operation.setForeground(Color.BLACK);
		pane.add(panel);
		return pane;
	}

	/** Create a brightness table panel */
	protected JPanel createBrightnessPanel() {
		GridBagLayout lay = new GridBagLayout();
		JPanel panel = new JPanel(lay);
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.fill = GridBagConstraints.BOTH;
		bag.gridwidth = 4;
		bag.weightx = 1;
		bag.weighty = 1;
		b_table.setBorder(b_table.new ScaleBorder(16));
		lay.setConstraints(b_table, bag);
		panel.add(b_table);
		bag.weightx = 0.1f;
		bag.weighty = 0;
		bag.gridx = 0;
		bag.gridy = 1;
		bag.gridwidth = 1;
		bag.fill = GridBagConstraints.NONE;
		bag.insets.top = 6;
		ButtonGroup group = new ButtonGroup();
		group.add(con_photocell);
		new ActionJob(this, con_photocell) {
			public void perform() throws Exception {
				sign.activateManualBrightness(false);
			}
		};
		lay.setConstraints(con_photocell, bag);
		panel.add(con_photocell);
		bag.gridx = 1;
		group.add(con_manual);
		new ActionJob(this, con_manual) {
			public void perform() throws Exception {
				sign.activateManualBrightness(true);
			}
		};
		lay.setConstraints(con_manual, bag);
		panel.add(con_manual);

		return panel;
	}

	/** Update the form with the current state of the sign */
	protected void doUpdate() throws RemoteException {
		super.doUpdate();
		mess_model = new DmsMessageModel(sign, connection.isAdmin(),
			connection.isTiger());
		mess_table.setModel(mess_model);
		TrafficDevice c = sign.getCamera();
		if(c != null)
			camera.setSelectedItem(c.getId());
		Float mile = sign.getMile();
		String t = sign.getTravel();
		Color color = Color.GRAY;
		if(sign.isActive())
			color = OK;
		make.setForeground(color);
		model.setForeground(color);
		version.setForeground(color);
		access.setForeground(color);
		type.setForeground(color);
		height.setForeground(color);
		pHeight.setForeground(color);
		width.setForeground(color);
		pWidth.setForeground(color);
		hBorder.setForeground(color);
		vBorder.setForeground(color);
		legend.setForeground(color);
		beacon.setForeground(color);
		tech.setForeground(color);
		cHeight.setForeground(color);
		cWidth.setForeground(color);
		hPitch.setForeground(color);
		vPitch.setForeground(color);
		badPixels.setForeground(color);
		ldcPotBase.setForeground(color);
		currentLow.setForeground(color);
		currentHigh.setForeground(color);
		badLimit.setForeground(color);
		lamp.setForeground(color);
		fan.setForeground(color);
		heat_tape.setForeground(color);
		cabinetTemp.setForeground(color);
		ambientTemp.setForeground(color);
		housingTemp.setForeground(color);
		b_table.doUpdate();
		if(mile == null)
			milePoint.setText("");
		else
			milePoint.setText(mile.toString());
		travel.setText(t);
	}

	/** Get the station index for the given item */
	protected int stationIndex(Object item) {
		if(item instanceof String) {
			String st = ((String)item).substring(0, 4).trim();
			try { return Integer.parseInt(st); }
			catch(NumberFormatException e) {
				return -1;
			}
		}
		return -1;
	}

	/** Refresh the status of the object */
	protected void doStatus() throws RemoteException {
		super.doStatus();
		make.setText(sign.getMake());
		model.setText(sign.getModel());
		version.setText(sign.getVersion());
		access.setText(sign.getSignAccess());
		type.setText(sign.getSignType());
		height.setText(formatMM(sign.getSignHeight()));
		pHeight.setText(formatPixels(sign.getSignHeightPixels()));
		width.setText(formatMM(sign.getSignWidth()));
		pWidth.setText(formatPixels(sign.getSignWidthPixels()));
		hBorder.setText(formatMM(sign.getHorizontalBorder()));
		vBorder.setText(formatMM(sign.getVerticalBorder()));
		legend.setText(sign.getSignLegend());
		beacon.setText(sign.getBeaconType());
		tech.setText(sign.getSignTechnology());
		cWidth.setText(formatPixels(sign.getCharacterWidthPixels()));
		cHeight.setText(formatPixels(sign.getCharacterHeightPixels()));
		hPitch.setText(formatMM(sign.getHorizontalPitch()));
		vPitch.setText(formatMM(sign.getVerticalPitch()));
		StatusTableModel m = new StatusTableModel(
			sign.getPowerSupplyTable());
		power_table.setColumnModel(m.createColumnModel());
		power_table.setDefaultRenderer(Object.class, m.getRenderer());
		power_table.setModel(m);
		badPixels.setText(String.valueOf(sign.getPixelFailureCount()));
		ldcPotBase.setText(formatNumber(sign.getLdcPotBase()));
		currentLow.setText(formatMA(sign.getPixelCurrentLow()));
		currentHigh.setText(formatMA(sign.getPixelCurrentHigh()));
		badLimit.setText(formatNumber(sign.getBadPixelLimit()));
		lamp.setText(sign.getLampStatus());
		fan.setText(sign.getFanStatus());
		heat_tape.setText(sign.getHeatTapeStatus());
		cabinetTemp.setText(formatTemp(sign.getMinCabinetTemp(),
			sign.getMaxCabinetTemp()));
		ambientTemp.setText(formatTemp(sign.getMinAmbientTemp(),
			sign.getMaxAmbientTemp()));
		housingTemp.setText(formatTemp(sign.getMinHousingTemp(),
			sign.getMaxHousingTemp()));
		operation.setText(sign.getOperation());
		b_table.doStatus();
		if(sign.isManualBrightness())
			con_manual.setSelected(true);
		else
			con_photocell.setSelected(true);
	}

	/** Apply button is pressed */
	protected void applyPressed() throws Exception {
		Float m = new Float(milePoint.getText());
		super.applyPressed();
		sign.setCamera((String)camera.getSelectedItem());
		sign.setMile(m);
		if(b_table.isModified()) {
			int[] table = b_table.getTableData();
			sign.setBrightnessTable(table);
		}
		sign.setTravel(travel.getText());
		sign.notifyUpdate();
	}
}
