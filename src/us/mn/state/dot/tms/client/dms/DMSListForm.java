/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import us.mn.state.dot.tms.DMSList;
import us.mn.state.dot.tms.SystemPolicy;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.AddForm;
import us.mn.state.dot.tms.client.toast.Icons;
import us.mn.state.dot.tms.client.toast.SortedListForm;
import us.mn.state.dot.tms.client.toast.TmsForm;
import us.mn.state.dot.tms.utils.ActionJob;
import us.mn.state.dot.tms.utils.RemoteListModel;

/**
 * DMSListForm
 *
 * @author Douglas Lau
 */
public class DMSListForm extends SortedListForm {

	/** Frame title */
	static private final String TITLE = "Dynamic Message Signs";

	/** Add title */
	static protected final String ADD_TITLE = "Add DMS";

	/** Slider labels */
	static protected final Hashtable<Integer, JLabel> TIME_LABELS =
		new Hashtable<Integer, JLabel>();
	static {
		TIME_LABELS.put(new Integer(0), new JLabel("0"));
		TIME_LABELS.put(new Integer(10), new JLabel("1"));
		TIME_LABELS.put(new Integer(20), new JLabel("2"));
		TIME_LABELS.put(new Integer(30), new JLabel("3"));
		TIME_LABELS.put(new Integer(40), new JLabel("4"));
		TIME_LABELS.put(new Integer(50), new JLabel("5"));
	}

	/** Remote sign list interface */
	protected final DMSList signList;

	/** System policy */
	protected final SystemPolicy policy;

	/** Ring 1 radius slider */
	protected final JSlider ring1 = new JSlider(0, 20, 1);

	/** Ring 2 radius slider */
	protected final JSlider ring2 = new JSlider(0, 20, 2);

	/** Ring 3 radius slider */
	protected final JSlider ring3 = new JSlider(0, 20, 5);

	/** Ring 4 radius slider */
	protected final JSlider ring4 = new JSlider(0, 20, 10);

	/** Page on time slider */
	protected final JSlider pageOn = new JSlider(0, 50);

	/** Page off time slider */
	protected final JSlider pageOff = new JSlider(0, 50);

	/** Apply button */
	protected final JButton apply1 = new JButton("Apply Changes");
	protected final JButton apply2 = new JButton("Apply Changes");

	/** Alert line 1 message field */
	protected final JTextField line1 = new JTextField();

	/** Alert line 2 message field */
	protected final JTextField line2 = new JTextField();

	/** Alert line 3 message field */
	protected final JTextField line3 = new JTextField();

	/** Create a new DMSListForm */
	public DMSListForm(TmsConnection tc) {
		super(TITLE, tc, tc.getProxy().getDMSListModel(),
			Icons.getIcon("drum-inactive"));
		signList = (DMSList)obj;
		policy = tc.getProxy().getPolicy();
	}

	/** Initialise the widgets on the form */
	protected void initialize() {
		JTabbedPane tab = new JTabbedPane();
		tab.add("List", createListPanel());
		tab.add("Ring", createRingPanel());
		tab.add("Page", createPagePanel());
		if(connection.isAlert())
			tab.add("Alert", createAlertPanel());
		add(tab);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Create the ring radius panel */
	protected JPanel createRingPanel() {
		GridBagLayout lay = new GridBagLayout();
		JPanel panel = new JPanel(lay);
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 0;
		bag.gridy = GridBagConstraints.RELATIVE;
		bag.gridwidth = 2;
		JLabel label = new JLabel("Ring radii (miles)");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridwidth = 1;
		bag.insets.right = 2;
		bag.anchor = GridBagConstraints.EAST;
		label = new JLabel("Ring 1");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Ring 2");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Ring 3");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Ring 4");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridx = 1;
		bag.gridy = 1;
		bag.insets.right = 0;
		bag.insets.top = 4;
		bag.anchor = GridBagConstraints.WEST;
		bag.fill = GridBagConstraints.HORIZONTAL;
		bag.weightx = 1;
		ring1.setEnabled(admin);
		ring1.setMajorTickSpacing(5);
		ring1.setMinorTickSpacing(1);
		ring1.setSnapToTicks(true);
		ring1.setPaintTicks(true);
		ring1.setLabelTable(ring1.createStandardLabels(5));
		ring1.setPaintLabels(true);
		lay.setConstraints(ring1, bag);
		panel.add(ring1);
		bag.gridy = GridBagConstraints.RELATIVE;
		ring2.setEnabled(admin);
		ring2.setMajorTickSpacing(5);
		ring2.setMinorTickSpacing(1);
		ring2.setSnapToTicks(true);
		ring2.setPaintTicks(true);
		ring2.setLabelTable(ring2.createStandardLabels(5));
		ring2.setPaintLabels(true);
		lay.setConstraints(ring2, bag);
		panel.add(ring2);
		ring3.setEnabled(admin);
		ring3.setMajorTickSpacing(5);
		ring3.setMinorTickSpacing(1);
		ring3.setSnapToTicks(true);
		ring3.setPaintTicks(true);
		ring3.setLabelTable(ring3.createStandardLabels(5));
		ring3.setPaintLabels(true);
		lay.setConstraints(ring3, bag);
		panel.add(ring3);
		ring4.setEnabled(admin);
		ring4.setMajorTickSpacing(5);
		ring4.setMinorTickSpacing(1);
		ring4.setSnapToTicks(true);
		ring4.setPaintTicks(true);
		ring4.setLabelTable(ring4.createStandardLabels(5));
		ring4.setPaintLabels(true);
		lay.setConstraints(ring4, bag);
		panel.add(ring4);
		if(admin) {
			bag.gridx = 0;
			bag.gridwidth = 2;
			bag.anchor = GridBagConstraints.CENTER;
			bag.fill = GridBagConstraints.NONE;
			bag.weightx = 0;
			lay.setConstraints(apply1, bag);
			panel.add(apply1);
			new ActionJob(this, apply1) {
				public void perform() throws Exception {
					applyPressed();
				}
			};
		}
		return panel;
	}

	/** Create the page panel */
	protected JPanel createPagePanel() {
		GridBagLayout lay = new GridBagLayout();
		JPanel panel = new JPanel(lay);
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 0;
		bag.gridy = GridBagConstraints.RELATIVE;
		bag.gridwidth = 2;
		JLabel label = new JLabel("Page time (seconds)");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridwidth = 1;
		bag.insets.right = 2;
		bag.anchor = GridBagConstraints.EAST;
		label = new JLabel("On");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Off");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridx = 1;
		bag.gridy = 1;
		bag.insets.right = 0;
		bag.insets.top = 4;
		bag.anchor = GridBagConstraints.WEST;
		bag.fill = GridBagConstraints.HORIZONTAL;
		bag.weightx = 1;
		pageOn.setEnabled( admin );
		pageOn.setMajorTickSpacing(5);
		pageOn.setMinorTickSpacing(1);
		pageOn.setSnapToTicks(true);
		pageOn.setPaintTicks(true);
		pageOn.setLabelTable(TIME_LABELS);
		pageOn.setPaintLabels(true);
		lay.setConstraints(pageOn, bag);
		panel.add(pageOn);
		bag.gridy = GridBagConstraints.RELATIVE;
		pageOff.setEnabled(admin);
		pageOff.setMajorTickSpacing(5);
		pageOff.setMinorTickSpacing(1);
		pageOff.setSnapToTicks(true);
		pageOff.setPaintTicks(true);
		pageOff.setLabelTable(TIME_LABELS);
		pageOff.setPaintLabels(true);
		lay.setConstraints(pageOff, bag);
		panel.add(pageOff);
		if(admin) {
			bag.gridx = 0;
			bag.gridwidth = 2;
			bag.anchor = GridBagConstraints.CENTER;
			bag.fill = GridBagConstraints.NONE;
			bag.weightx = 0;
			lay.setConstraints(apply2, bag);
			panel.add(apply2);
			new ActionJob(this, apply2) {
				public void perform() throws Exception {
					applyPressed();
				}
			};
		}
		return panel;
	}

	/** Create the alert panel */
	protected JPanel createAlertPanel() {
		GridBagLayout lay = new GridBagLayout();
		JPanel panel = new JPanel(lay);
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 0;
		bag.gridwidth = 2;
		JLabel label = new JLabel("WARNING: Alert will be sent to");
		label.setForeground(TmsForm.ERROR);
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("all active Dynamic Message Signs.");
		label.setForeground(TmsForm.ERROR);
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("* Double check your spelling *");
		label.setForeground(TmsForm.ERROR);
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("* Don't use any naughty words *");
		label.setForeground(TmsForm.ERROR);
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridwidth = 1;
		bag.anchor = GridBagConstraints.EAST;
		bag.insets.right = 2;
		label = new JLabel("Line 1");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Line 2");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Line 3");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridx = 1;
		bag.gridy = 4;
		bag.insets.right = 0;
		bag.insets.top = 4;
		bag.anchor = GridBagConstraints.WEST;
		bag.fill = GridBagConstraints.HORIZONTAL;
		bag.weightx = 1;
		lay.setConstraints(line1, bag);
		panel.add(line1);
		bag.gridy = GridBagConstraints.RELATIVE;
		lay.setConstraints(line2, bag);
		panel.add(line2);
		lay.setConstraints(line3, bag);
		panel.add(line3);
		bag.gridx = 0;
		bag.gridwidth = 2;
		bag.weightx = 0;
		bag.anchor = GridBagConstraints.CENTER;
		bag.fill = GridBagConstraints.NONE;
		final JButton send = new JButton("Send");
		lay.setConstraints(send, bag);
		panel.add(send);
		final JButton clear = new JButton("Clear");
		lay.setConstraints(clear, bag);
		panel.add(clear);
		new ActionJob(this, send) {
			public void perform() throws Exception {
				sendPressed(send);
			}
		};
		new ActionJob(this, clear) {
			public void perform() throws Exception {
				clearPressed(clear);
			}
		};
		return panel;
	}

	/** Send an alert when the send button is pressed */
	protected void sendPressed(JButton send) throws Exception {
		String[] text = new String[3];
		text[0] = line1.getText().trim().toUpperCase();
		text[1] = line2.getText().trim().toUpperCase();
		text[2] = line3.getText().trim().toUpperCase();
		signList.sendGroup(null, "Alert", text);
	}

	/** Clear an alert when the clear button is pressed */
	protected void clearPressed(JButton clear) throws Exception {
		line1.setText("");
		line2.setText("");
		line3.setText("");
		signList.clearGroup(null, "USER FIXME");
	}

	/** Update the DMSListForm with the current status */
	protected void doUpdate() throws RemoteException {
		ring1.setValue(policy.getValue(SystemPolicy.RING_RADIUS_0));
		ring2.setValue(policy.getValue(SystemPolicy.RING_RADIUS_1));
		ring3.setValue(policy.getValue(SystemPolicy.RING_RADIUS_2));
		ring4.setValue(policy.getValue(SystemPolicy.RING_RADIUS_3));
		pageOn.setValue(policy.getValue(SystemPolicy.DMS_PAGE_ON_TIME));
		pageOff.setValue(policy.getValue(
			SystemPolicy.DMS_PAGE_OFF_TIME));
	}

	/** Apply button pressed */
	public void applyPressed() throws TMSException, RemoteException {
		policy.setValue(SystemPolicy.RING_RADIUS_0, ring1.getValue());
		policy.setValue(SystemPolicy.RING_RADIUS_1, ring2.getValue());
		policy.setValue(SystemPolicy.RING_RADIUS_2, ring3.getValue());
		policy.setValue(SystemPolicy.RING_RADIUS_3, ring4.getValue());
		policy.setValue(SystemPolicy.DMS_PAGE_ON_TIME,
			pageOn.getValue());
		policy.setValue(SystemPolicy.DMS_PAGE_OFF_TIME,
			pageOff.getValue());
		policy.notifyUpdate();
		signList.notifyUpdate();
	}

	/** Add an item to the list */
	protected void addItem() throws Exception {
		connection.getDesktop().show(new AddForm(ADD_TITLE, sList));
		updateButtons();
	}

	/** Edit an item in the list */
	protected void editItem() throws RemoteException {
		String id = getSelectedItem();
		if(id != null) {
			connection.getDesktop().show(
				new DMSProperties(connection, id));
		}
	}

	/** Get the prototype cell value */
	protected String getPrototypeCellValue() {
		return "V394W02";
	}
}
