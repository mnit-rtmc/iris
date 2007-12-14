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
import java.util.HashMap;
import java.util.Hashtable;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.SystemPolicy;
import us.mn.state.dot.tms.utils.ActionJob;

/**
 * PolicyForm allows administrators to change system-wide policy parameters.
 *
 * @author Douglas Lau
 */
public class PolicyForm extends AbstractForm {

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

	/** System policy type cache */
	protected final TypeCache<SystemPolicy> cache;

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

	/** Proxy listener for System Policy proxies */
	protected final ProxyListener<SystemPolicy> sp_listener =
		new ProxyListener<SystemPolicy>()
	{
		public void proxyAdded(SystemPolicy p) { }
		public void proxyRemoved(SystemPolicy p) { }
		public void proxyChanged(SystemPolicy p, String a) {
			doUpdate();
		}
	};

	/** Create a new policy form */
	public PolicyForm(TypeCache<SystemPolicy> c) {
		super(TITLE);
		cache = c;
	}

	/** Initialise the widgets on the form */
	protected void initialize() {
		cache.addProxyListener(sp_listener);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP);
		add(tab);
		tab.add("Meters", createMeterPanel());
		tab.add("DMS", createDMSPanel());
		tab.add("Incidents", createIncidentPanel());
		add(Box.createVerticalStrut(VGAP));
		new ActionJob(this, apply) {
			public void perform() throws Exception {
				applyPressed();
			}
		};
		add(apply);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	protected void dispose() {
		cache.removeProxyListener(sp_listener);
	}

	/** Create the ramp meter policy panel */
	protected JPanel createMeterPanel() {
		green.setLabelTable(TIME_LABELS);
		yellow.setLabelTable(TIME_LABELS);
		min_red.setLabelTable(TIME_LABELS);

		FormPanel panel = new FormPanel(true);
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

		FormPanel panel = new FormPanel(true);
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

		FormPanel panel = new FormPanel(true);
		panel.setCenter();
		panel.addRow(new JLabel("Ring radii (miles)"));
		panel.addRow("Ring 1", ring1);
		panel.addRow("Ring 2", ring2);
		panel.addRow("Ring 3", ring3);
		panel.addRow("Ring 4", ring4);
		return panel;
	}

	/** Get the value of the named policy */
	protected int getPolicyValue(String p) {
		SystemPolicy sp = cache.getObject(p);
		if(sp != null)
			return sp.getValue();
		else
			return 0;
	}

	/** Update the DMSListForm with the current status */
	protected void doUpdate() {
		green.setValue(getPolicyValue(SystemPolicy.METER_GREEN_TIME));
		yellow.setValue(getPolicyValue(SystemPolicy.METER_YELLOW_TIME));
		min_red.setValue(getPolicyValue(
			SystemPolicy.METER_MIN_RED_TIME));
		pageOn.setValue(getPolicyValue(SystemPolicy.DMS_PAGE_ON_TIME));
		pageOff.setValue(getPolicyValue(
			SystemPolicy.DMS_PAGE_OFF_TIME));
		ring1.setValue(getPolicyValue(SystemPolicy.RING_RADIUS_0));
		ring2.setValue(getPolicyValue(SystemPolicy.RING_RADIUS_1));
		ring3.setValue(getPolicyValue(SystemPolicy.RING_RADIUS_2));
		ring4.setValue(getPolicyValue(SystemPolicy.RING_RADIUS_3));
	}

	/** Set the value of the named policy */
	protected void setPolicyValue(String p, int v) {
		SystemPolicy sp = cache.getObject(p);
		if(sp != null)
			sp.setValue(v);
		else {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("value", v);
			cache.createObject(p, attrs);
		}
	}

	/** Apply button pressed */
	public void applyPressed() {
		setPolicyValue(SystemPolicy.METER_GREEN_TIME,
			green.getValue());
		setPolicyValue(SystemPolicy.METER_YELLOW_TIME,
			yellow.getValue());
		setPolicyValue(SystemPolicy.METER_MIN_RED_TIME,
			min_red.getValue());
		setPolicyValue(SystemPolicy.DMS_PAGE_ON_TIME,
			pageOn.getValue());
		setPolicyValue(SystemPolicy.DMS_PAGE_OFF_TIME,
			pageOff.getValue());
		setPolicyValue(SystemPolicy.RING_RADIUS_0, ring1.getValue());
		setPolicyValue(SystemPolicy.RING_RADIUS_1, ring2.getValue());
		setPolicyValue(SystemPolicy.RING_RADIUS_2, ring3.getValue());
		setPolicyValue(SystemPolicy.RING_RADIUS_3, ring4.getValue());
	}
}
