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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sonar.User;
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
	static private final String TITLE = "System Attributes";

	/** Create a SONAR name to check for allowed updates */
	static protected String createNamespaceString(String name) {
		return SystemAttribute.SONAR_TYPE + "/" + name;
	}

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

	/** System attribute editor tab */
	protected SystemAttributeTab systemAttributeTab=null;

	/** SONAR User for permission checks */
	protected final User user;

	/** Proxy listener for System Attribute proxies */
	protected final ProxyListener<SystemAttribute> sa_listener =
		new ProxyListener<SystemAttribute>()
	{
		public void proxyAdded(SystemAttribute p) {
			doUpdateAttribute(p.getName());
		}
		public void enumerationComplete() { }
		public void proxyRemoved(SystemAttribute p) {
			doUpdateAttribute(p.getName());
		}
		public void proxyChanged(SystemAttribute p, String a) {
			doUpdateAttribute(p.getName());
		}
	};

	/** Create a new policy form */
	public PolicyForm(TypeCache<SystemAttribute> c, User u) {
		super(TITLE);
		cache = c;
		user = u;
	}

	/** Initialise the widgets on the form */
	protected void initialize() {
		cache.addProxyListener(sa_listener);
		JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP);
		add(tab);
		tab.add("Meters", createMeterPanel());
		tab.add("DMS", createDMSPanel());
		tab.add("Incidents", createIncidentPanel());
		systemAttributeTab = new SystemAttributeTab(true, this, cache);
		tab.add(systemAttributeTab);
		updateAttribute(null);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	protected void dispose() {
		cache.removeProxyListener(sa_listener);
	}

	/** Check if the user can add the named attribute */
	public boolean canAddAttribute(String name) {
		return name != null && user.canAdd(createNamespaceString(name));
	}

	/** Check if the user can change the named attribute */
	public boolean canUpdateAttribute(String name) {
		return name != null && user.canUpdate(createNamespaceString(
			name));
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
		if(canUpdateAttribute(SystemAttribute.METER_GREEN_SECS)) {
			new ChangeJob(this, green) {
				public void perform() {
					setAtt(SystemAttribute.METER_GREEN_SECS,
						green.getValue());
				}
			};
		}
		panel.addRow("Yellow", yellow);
		if(canUpdateAttribute(SystemAttribute.METER_YELLOW_SECS)) {
			new ChangeJob(this, yellow) {
				public void perform() {
					setAtt(SystemAttribute.METER_YELLOW_SECS,
						yellow.getValue());
				}
			};
		}
		panel.addRow("Minimum Red", min_red);
		if(canUpdateAttribute(SystemAttribute.METER_MIN_RED_SECS)) {
			new ChangeJob(this, min_red) {
				public void perform() {
					setAtt(SystemAttribute.METER_MIN_RED_SECS,
						min_red.getValue());
				}
			};
		}
		return panel;
	}

	/** Create the DMS policy panel */
	protected JPanel createDMSPanel() {
		FormPanel panel = new FormPanel(true);
		panel.setCenter();
		panel.addRow(new JLabel("Page time (seconds)"));
		panel.addRow("On", page_on);
		if(canUpdateAttribute(SystemAttribute.DMS_PAGE_ON_SECS)) {
			new ChangeJob(this, page_on) {
				public void perform() {
					setAtt(SystemAttribute.DMS_PAGE_ON_SECS,
						page_on.getValue());
				}
			};
		}
		panel.addRow("Off", page_off);
		if(canUpdateAttribute(SystemAttribute.DMS_PAGE_OFF_SECS)) {
			new ChangeJob(this, page_off) {
				public void perform() {
					setAtt(SystemAttribute.DMS_PAGE_OFF_SECS,
						page_off.getValue());
				}
			};
		}
		return panel;
	}

	/** Create the incident policy panel */
	protected JPanel createIncidentPanel() {
		FormPanel panel = new FormPanel(true);
		panel.setCenter();
		panel.addRow(new JLabel("Ring radii (miles)"));
		panel.addRow("Ring 1", ring1);
		if(canUpdateAttribute(SystemAttribute.INCIDENT_RING_1_MILES)) {
			new ChangeJob(this, ring1) {
				public void perform() {
					setAtt(SystemAttribute.INCIDENT_RING_1_MILES,
						ring1.getValue());
				}
			};
		}
		panel.addRow("Ring 2", ring2);
		if(canUpdateAttribute(SystemAttribute.INCIDENT_RING_2_MILES)) {
			new ChangeJob(this, ring2) {
				public void perform() {
					setAtt(SystemAttribute.INCIDENT_RING_2_MILES,
						ring2.getValue());
				}
			};
		}
		panel.addRow("Ring 3", ring3);
		if(canUpdateAttribute(SystemAttribute.INCIDENT_RING_3_MILES)) {
			new ChangeJob(this, ring3) {
				public void perform() {
					setAtt(SystemAttribute.INCIDENT_RING_3_MILES,
						ring3.getValue());
				}
			};
		}
		panel.addRow("Ring 4", ring4);
		if(canUpdateAttribute(SystemAttribute.INCIDENT_RING_4_MILES)) {
			new ChangeJob(this, ring4) {
				public void perform() {
					setAtt(SystemAttribute.INCIDENT_RING_4_MILES,
						ring4.getValue());
				}
			};
		}
		return panel;
	}

	/** Set the value of the named attribute */
	protected void setAtt(String attr, Object v) {
		SystemAttribute sa = cache.lookupObject(attr);
		if(sa != null) {
			if(canUpdateAttribute(attr))
				sa.setValue(v.toString());
		} else if(canAddAttribute(attr)) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("value", v);
			cache.createObject(attr, attrs);
		}
	}

	/** Update one system attribute on the form (WORKER thread) */
	protected void doUpdateAttribute(final String a) {
		new AbstractJob() {
			public void perform() {
				updateAttribute(a);
			}
		}.addToScheduler();
	}

	/** Update one system attribute on the form */
	protected void updateAttribute(String a) {
		if(a == null || a.equals(SystemAttribute.METER_GREEN_SECS)) {
			green.setValue(
				SystemAttributeHelper.getMeterGreenSecs());
		}
		if(a == null || a.equals(SystemAttribute.METER_YELLOW_SECS)) {
			yellow.setValue(
				SystemAttributeHelper.getMeterYellowSecs());
		}
		if(a == null || a.equals(SystemAttribute.METER_MIN_RED_SECS)) {
			min_red.setValue(
				SystemAttributeHelper.getMeterMinRedSecs());
		}
		if(a == null || a.equals(SystemAttribute.DMS_PAGE_ON_SECS)) {
			page_on.setValue(
				SystemAttributeHelper.getDmsPageOnSecs());
		}
		if(a == null || a.equals(SystemAttribute.DMS_PAGE_OFF_SECS)) {
			page_off.setValue(
				SystemAttributeHelper.getDmsPageOffSecs());
		}
		if(a == null || a.equals(SystemAttribute.INCIDENT_RING_1_MILES))
		{
			ring1.setValue(
				SystemAttributeHelper.getIncidentRing1Miles());
		}
		if(a == null || a.equals(SystemAttribute.INCIDENT_RING_2_MILES))
		{
			ring2.setValue(
				SystemAttributeHelper.getIncidentRing2Miles());
		}
		if(a == null || a.equals(SystemAttribute.INCIDENT_RING_3_MILES))
		{
			ring3.setValue(
				SystemAttributeHelper.getIncidentRing3Miles());
		}
		if(a == null || a.equals(SystemAttribute.INCIDENT_RING_4_MILES))
		{
			ring4.setValue(
				SystemAttributeHelper.getIncidentRing4Miles());
		}
	}
}
