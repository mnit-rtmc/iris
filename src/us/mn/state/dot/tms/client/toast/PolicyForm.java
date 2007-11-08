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
package us.mn.state.dot.tms.client.toast;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.Hashtable;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import us.mn.state.dot.tms.SystemPolicy;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.utils.ActionJob;

/**
 * PolicyForm allows administrators to change system-wide policy parameters.
 *
 * @author Douglas Lau
 */
public class PolicyForm extends TMSObjectForm {

	/** Frame title */
	static private final String TITLE = "System-Wide Policy";

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

	/** Create a slider */
	static protected JSlider createSlider(int low, int high) {
		JSlider slider = new JSlider(low, high);
		slider.setMajorTickSpacing(5);
		slider.setMinorTickSpacing(1);
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		return slider;
	}

	/** System policy */
	protected SystemPolicy policy;

	/** Ramp meter green time slider */
	protected final JSlider green = createSlider(0, 30);

	/** Ramp meter yellow time slider */
	protected final JSlider yellow = createSlider(0, 30);

	/** Ramp meter minimum red time slider */
	protected final JSlider min_red = createSlider(0, 30);

	/** Page on time slider */
	protected final JSlider pageOn = createSlider(0, 50);

	/** Page off time slider */
	protected final JSlider pageOff = createSlider(0, 50);

	/** Ring 1 radius slider */
	protected final JSlider ring1 = createSlider(0, 20);

	/** Ring 2 radius slider */
	protected final JSlider ring2 = createSlider(0, 20);

	/** Ring 3 radius slider */
	protected final JSlider ring3 = createSlider(0, 20);

	/** Ring 4 radius slider */
	protected final JSlider ring4 = createSlider(0, 20);

	/** Apply button */
	protected final JButton apply = new JButton("Apply Changes");

	/** Create a new policy form */
	public PolicyForm(TmsConnection tc) {
		super(TITLE, tc);
	}

	/** Initialise the widgets on the form */
	protected void initialize() throws RemoteException {
		policy = tms.getPolicy();
		obj = policy;
		super.initialize();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP);
		add(tab);
		tab.add("Meters", createMeterPanel());
		tab.add("DMS", createDMSPanel());
		tab.add("Incidents", createIncidentPanel());
		if(admin) {
			add(Box.createVerticalStrut(VGAP));
			new ActionJob(this, apply) {
				public void perform() throws Exception {
					applyPressed();
				}
			};
			add(apply);
		}
		setBackground(Color.LIGHT_GRAY);
	}

	/** Create the ramp meter policy panel */
	protected JPanel createMeterPanel() {
		green.setLabelTable(TIME_LABELS);
		yellow.setLabelTable(TIME_LABELS);
		min_red.setLabelTable(TIME_LABELS);

		FormPanel panel = new FormPanel(admin);
		panel.setCenter();
		panel.addRow(new JLabel("System-Wide Ramp Meter"));
		panel.setCenter();
		panel.addRow(new JLabel("Interval Times (seconds)"));
		panel.addRow("Green", green);
		panel.addRow("Yellow", yellow);
		panel.addRow("Minimum Red", min_red);
		return panel;
	}

	/** Create the DMS policy panel */
	protected JPanel createDMSPanel() {
		pageOn.setLabelTable(TIME_LABELS);
		pageOff.setLabelTable(TIME_LABELS);

		FormPanel panel = new FormPanel(admin);
		panel.setCenter();
		panel.addRow(new JLabel("Page time (seconds)"));
		panel.addRow("On", pageOn);
		panel.addRow("Off", pageOff);
		return panel;
	}

	/** Create the incident policy panel */
	protected JPanel createIncidentPanel() {
		ring1.setLabelTable(ring1.createStandardLabels(5));
		ring2.setLabelTable(ring2.createStandardLabels(5));
		ring3.setLabelTable(ring3.createStandardLabels(5));
		ring4.setLabelTable(ring4.createStandardLabels(5));

		FormPanel panel = new FormPanel(admin);
		panel.setCenter();
		panel.addRow(new JLabel("Ring radii (miles)"));
		panel.addRow("Ring 1", ring1);
		panel.addRow("Ring 2", ring2);
		panel.addRow("Ring 3", ring3);
		panel.addRow("Ring 4", ring4);
		return panel;
	}

	/** Update the DMSListForm with the current status */
	protected void doUpdate() throws RemoteException {
		green.setValue(policy.getValue(SystemPolicy.METER_GREEN_TIME));
		yellow.setValue(policy.getValue(
			SystemPolicy.METER_YELLOW_TIME));
		min_red.setValue(policy.getValue(
			SystemPolicy.METER_MIN_RED_TIME));
		pageOn.setValue(policy.getValue(SystemPolicy.DMS_PAGE_ON_TIME));
		pageOff.setValue(policy.getValue(
			SystemPolicy.DMS_PAGE_OFF_TIME));
		ring1.setValue(policy.getValue(SystemPolicy.RING_RADIUS_0));
		ring2.setValue(policy.getValue(SystemPolicy.RING_RADIUS_1));
		ring3.setValue(policy.getValue(SystemPolicy.RING_RADIUS_2));
		ring4.setValue(policy.getValue(SystemPolicy.RING_RADIUS_3));
	}

	/** Apply button pressed */
	public void applyPressed() throws TMSException, RemoteException {
		policy.setValue(SystemPolicy.METER_GREEN_TIME,
			green.getValue());
		policy.setValue(SystemPolicy.METER_YELLOW_TIME,
			yellow.getValue());
		policy.setValue(SystemPolicy.METER_MIN_RED_TIME,
			min_red.getValue());
		policy.setValue(SystemPolicy.DMS_PAGE_ON_TIME,
			pageOn.getValue());
		policy.setValue(SystemPolicy.DMS_PAGE_OFF_TIME,
			pageOff.getValue());
		policy.setValue(SystemPolicy.RING_RADIUS_0, ring1.getValue());
		policy.setValue(SystemPolicy.RING_RADIUS_1, ring2.getValue());
		policy.setValue(SystemPolicy.RING_RADIUS_2, ring3.getValue());
		policy.setValue(SystemPolicy.RING_RADIUS_3, ring4.getValue());
		policy.notifyUpdate();
	}
}
