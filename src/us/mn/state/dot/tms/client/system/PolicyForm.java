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
package us.mn.state.dot.tms.client.system;

import java.awt.Color;
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.SystemAttributeHelper;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;

/**
 * PolicyForm allows administrators to change system-wide policy parameters.
 *
 * @author Douglas Lau
 */
public class PolicyForm extends AbstractForm {

	/** Frame title */
	static private final String TITLE = "System-Wide Policy";

	/** Create a spinner */
	static protected JSpinner createSpinner() {
		return new JSpinner(new SpinnerNumberModel(0, 0, 5, 0.1));
	}

	/** Create an incident miles spinner */
	static protected JSpinner createMileSpinner() {
		SpinnerNumberModel m = new SpinnerNumberModel(0, 0, 20, 1);
		JSpinner s = new JSpinner();
		s.setEditor(new JSpinner.NumberEditor(s, "##"));
		return s;
	}

	/** SystemAttribute type cache */
	protected final TypeCache<SystemAttribute> cache;

	/** Ramp meter green time spinner */
	protected final JSpinner green = createSpinner();

	/** Ramp meter yellow time spinner */
	protected final JSpinner yellow = createSpinner();

	/** Ramp meter minimum red time spinner */
	protected final JSpinner min_red = createSpinner();

	/** Page on time spinner */
	protected final JSpinner page_on = createSpinner();

	/** Page off time spinner */
	protected final JSpinner page_off = createSpinner();

	/** Ring 1 radius spinner */
	protected final JSpinner ring1 = createMileSpinner();

	/** Ring 2 radius spinner */
	protected final JSpinner ring2 = createMileSpinner();

	/** Ring 3 radius spinner */
	protected final JSpinner ring3 = createMileSpinner();

	/** Ring 4 radius spinner */
	protected final JSpinner ring4 = createMileSpinner();

	/** Apply button */
	protected final JButton apply = new JButton("Apply Changes");

	/** System attribute editor tab */
	protected SystemAttributeTab systemAttributeTab=null;

	/** user is an admin */
	protected final boolean admin;	//FIXME: what if user changes to admin? This isn't updated

	/** Proxy listener for System Attribute proxies */
	protected final ProxyListener<SystemAttribute> sa_listener =
		new ProxyListener<SystemAttribute>()
	{
		public void proxyAdded(SystemAttribute p) { }
		public void enumerationComplete() { }
		public void proxyRemoved(SystemAttribute p) { }
		public void proxyChanged(SystemAttribute p, String a) {
			doUpdate();
		}
	};

	/** Create a new policy form */
	public PolicyForm(boolean argAdmin, TypeCache<SystemAttribute> c) {
		super(TITLE);
		admin = argAdmin;
		cache = c;
	}

	/** Initialise the widgets on the form */
	protected void initialize() {
		cache.addProxyListener(sa_listener);
		doUpdate();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP);
		add(tab);
		tab.add("Meters", createMeterPanel());
		tab.add("DMS", createDMSPanel());
		tab.add("Incidents", createIncidentPanel());

		// add system attribute editor tab
		systemAttributeTab = new SystemAttributeTab(admin, this, cache);
		tab.add(systemAttributeTab);

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
		cache.removeProxyListener(sa_listener);
	}

	/** Create the ramp meter policy panel */
	protected JPanel createMeterPanel() {
		FormPanel panel = new FormPanel(true);
		panel.setCenter();
		panel.addRow(new JLabel("Ramp Meter Attributes"));
		panel.setCenter();
		JTextArea area = new JTextArea("Use caution: these values " +
			"are commonly\nstored in controller memory.  A " +
			"download\nmight be required for each controller.");
		area.setBackground(null);
		panel.addRow(area);
		panel.setCenter();
		panel.addRow(new JLabel("Interval Times (seconds)"));
		panel.addRow("Green", green);
		panel.addRow("Yellow", yellow);
		panel.addRow("Minimum Red", min_red);
		return panel;
	}

	/** Create the DMS policy panel */
	protected JPanel createDMSPanel() {
		FormPanel panel = new FormPanel(true);
		panel.setCenter();
		panel.addRow(new JLabel("Page time (seconds)"));
		panel.addRow("On", page_on);
		panel.addRow("Off", page_off);
		return panel;
	}

	/** Create the incident policy panel */
	protected JPanel createIncidentPanel() {
		FormPanel panel = new FormPanel(true);
		panel.setCenter();
		panel.addRow(new JLabel("Ring radii (miles)"));
		panel.addRow("Ring 1", ring1);
		panel.addRow("Ring 2", ring2);
		panel.addRow("Ring 3", ring3);
		panel.addRow("Ring 4", ring4);
		return panel;
	}

	/** Update the DMSListForm with the current status */
	protected void doUpdate() {
		green.setValue(SystemAttributeHelper.getMeterGreenSecs());
		yellow.setValue(SystemAttributeHelper.getMeterYellowSecs());
		min_red.setValue(SystemAttributeHelper.getMeterMinRedSecs());
		page_on.setValue(SystemAttributeHelper.getDmsPageOnSecs());
		page_off.setValue(SystemAttributeHelper.getDmsPageOffSecs());
		ring1.setValue(SystemAttributeHelper.getIncidentRing1Miles());
		ring2.setValue(SystemAttributeHelper.getIncidentRing2Miles());
		ring3.setValue(SystemAttributeHelper.getIncidentRing3Miles());
		ring4.setValue(SystemAttributeHelper.getIncidentRing4Miles());
	}

	/** Set the value of the named attribute */
	protected void setAttributeValue(String attr, Object v) {
		SystemAttribute sa = cache.lookupObject(attr);
		if(sa != null)
			sa.setValue(v.toString());
		else {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("value", v);
			cache.createObject(attr, attrs);
		}
	}

	/** Apply button pressed */
	public void applyPressed() {
		setAttributeValue(SystemAttribute.METER_GREEN_SECS,
			green.getValue());
		setAttributeValue(SystemAttribute.METER_YELLOW_SECS,
			yellow.getValue());
		setAttributeValue(SystemAttribute.METER_MIN_RED_SECS,
			min_red.getValue());
		setAttributeValue(SystemAttribute.DMS_PAGE_ON_SECS,
			page_on.getValue());
		setAttributeValue(SystemAttribute.DMS_PAGE_OFF_SECS,
			page_off.getValue());
		setAttributeValue(SystemAttribute.INCIDENT_RING_1_MILES,
			ring1.getValue());
		setAttributeValue(SystemAttribute.INCIDENT_RING_2_MILES,
			ring2.getValue());
		setAttributeValue(SystemAttribute.INCIDENT_RING_3_MILES,
			ring3.getValue());
		setAttributeValue(SystemAttribute.INCIDENT_RING_4_MILES,
			ring4.getValue());
	}
}
