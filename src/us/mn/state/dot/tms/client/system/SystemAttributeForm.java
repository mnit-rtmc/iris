/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * The system attribute allows administrators to change system-wide policy
 * attributes.
 *
 * @author Douglas Lau
 */
public class SystemAttributeForm extends AbstractForm {

	/** Frame title */
	static private final String TITLE = "System Attributes";

	/** Caution text for ramp meters */
	static protected final String CAUTION_RMETERS = 
		I18NMessages.get("SystemAttributeForm.RampMeterCaution");

	/** Caution text for DMS */
	static protected final String CAUTION_DMS = 
		I18NMessages.get("SystemAttributeForm.DmsCaution");

	/** Create a SONAR name to check for allowed updates */
	static protected String createNamespaceString(String name) {
		return SystemAttribute.SONAR_TYPE + "/" + name;
	}

	/** Create a spinner */
	static protected JSpinner createSpinner() {
		return new JSpinner(new SpinnerNumberModel(0, 0, 5, 0.1));
	}

	/** Create a longer time spinner */
	static protected JSpinner createTimeSpinner() {
		return new JSpinner(new SpinnerNumberModel(30, 5, 86400, 5));
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

	/** Poll frequency spinner */
	protected final JSpinner poll_freq = createTimeSpinner();

	/** Pixel test timeout spinner */
	protected final JSpinner pxl_tst_timeout = createTimeSpinner();

	/** Lamp test timeout spinner */
	protected final JSpinner lmp_tst_timeout = createTimeSpinner();

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
	protected final SystemAttributeTab systemAttributeTab;

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

	/** Create a new system attribute form */
	public SystemAttributeForm(TypeCache<SystemAttribute> c, User u) {
		super(TITLE);
		setHelpPageName("Help.SystemAttributeForm");
		cache = c;
		user = u;
		systemAttributeTab = new SystemAttributeTab(cache, this);
	}

	/** Initialise the widgets on the form */
	protected void initialize() {
		cache.addProxyListener(sa_listener);
		JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP);
		add(tab);
		tab.add("Meters", createMeterPanel());
		tab.add("DMS", createDMSPanel());
		tab.add("Incidents", createIncidentPanel());
		tab.add(systemAttributeTab);
		updateAttribute(null);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	protected void dispose() {
		cache.removeProxyListener(sa_listener);
		systemAttributeTab.dispose();
	}

	/** Check if the user can add the named attribute */
	public boolean canAddAttribute(String name) {
		return name != null && user.canAdd(createNamespaceString(name));
	}

	/** Check if the user can update the named attribute */
	public boolean canUpdateAttribute(String name) {
		return name != null && user.canUpdate(createNamespaceString(
			name));
	}

	/** Check if the user can change the named attribute */
	public boolean canChangeAttribute(String name) {
		SystemAttribute sa = cache.lookupObject(name);
		if(sa != null)
			return canUpdateAttribute(name);
		else
			return canAddAttribute(name);
	}

	/** Check if the user can remove the named attribute */
	public boolean canRemoveAttribute(String name) {
		return name != null && user.canRemove(createNamespaceString(
			name));
	}

	/** Initialize one spinner widget */
	protected void initSpinner(final JSpinner spinner, SystemAttrEnum sa) {
		final String aname = sa.aname();
		if(canChangeAttribute(aname)) {
			new ChangeJob(this, spinner) {
				public void perform() {
					setAttribute(aname, spinner.getValue());
				}
			};
		} else
			spinner.setEnabled(false);
	}

	/** Create the ramp meter policy panel */
	protected JPanel createMeterPanel() {
		FormPanel panel = new FormPanel(true);
		panel.setCenter();
		panel.addRow(new JLabel("Ramp Meter Interval Times (seconds)"));
		panel.addRow("Green", green);
		initSpinner(green, SystemAttrEnum.METER_GREEN_SECS);
		panel.addRow("Yellow", yellow);
		initSpinner(yellow, SystemAttrEnum.METER_YELLOW_SECS);
		panel.addRow("Minimum Red", min_red);
		initSpinner(min_red, SystemAttrEnum.METER_MIN_RED_SECS);
		panel.setCenter();
		JLabel area = new JLabel(CAUTION_RMETERS);
		area.setBackground(null);
		panel.addRow(area);
		return panel;
	}

	/** Create the DMS policy panel */
	protected JPanel createDMSPanel() {
		FormPanel panel = new FormPanel(true);
		panel.setCenter();
		panel.addRow(new JLabel("Time values (seconds)"));
		panel.addRow("Polling Frequency", poll_freq);
		initSpinner(poll_freq, SystemAttrEnum.DMS_POLL_FREQ_SECS);
		panel.addRow("Pixel Test Timeout", pxl_tst_timeout);
		initSpinner(pxl_tst_timeout,
			SystemAttrEnum.DMS_PIXEL_TEST_TIMEOUT_SECS);
		panel.addRow("Lamp Test Timeout", lmp_tst_timeout);
		initSpinner(lmp_tst_timeout,
			SystemAttrEnum.DMS_LAMP_TEST_TIMEOUT_SECS);
		panel.addRow("Page On Time", page_on);
		initSpinner(page_on, SystemAttrEnum.DMS_PAGE_ON_SECS);
		panel.addRow("Page Off Time", page_off);
		initSpinner(page_off, SystemAttrEnum.DMS_PAGE_OFF_SECS);
		panel.setCenter();
		JLabel area = new JLabel(CAUTION_DMS);
		area.setBackground(null);
		panel.addRow(area);
		return panel;
	}

	/** Create the incident policy panel */
	protected JPanel createIncidentPanel() {
		FormPanel panel = new FormPanel(true);
		panel.setCenter();
		panel.addRow(new JLabel("Ring radii (miles)"));
		panel.addRow("Ring 1", ring1);
		initSpinner(ring1, SystemAttrEnum.INCIDENT_RING_1_MILES);
		panel.addRow("Ring 2", ring2);
		initSpinner(ring2, SystemAttrEnum.INCIDENT_RING_2_MILES);
		panel.addRow("Ring 3", ring3);
		initSpinner(ring3, SystemAttrEnum.INCIDENT_RING_3_MILES);
		panel.addRow("Ring 4", ring4);
		initSpinner(ring4, SystemAttrEnum.INCIDENT_RING_4_MILES);
		return panel;
	}

	/** Set the value of the named attribute */
	protected void setAttribute(String attr, Object v) {
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
		if(a == null ||
		   a.equals(SystemAttrEnum.METER_GREEN_SECS.aname()))
		{
			green.setValue(
				SystemAttrEnum.METER_GREEN_SECS.getFloat());
		}
		if(a == null ||
		   a.equals(SystemAttrEnum.METER_YELLOW_SECS.aname()))
		{
			yellow.setValue(
				SystemAttrEnum.METER_YELLOW_SECS.getFloat());
		}
		if(a == null ||
		   a.equals(SystemAttrEnum.METER_MIN_RED_SECS.aname()))
		{
			min_red.setValue(
				SystemAttrEnum.METER_MIN_RED_SECS.getFloat());
		}
		if(a == null ||
		   a.equals(SystemAttrEnum.DMS_POLL_FREQ_SECS.aname()))
		{
			poll_freq.setValue(
				SystemAttrEnum.DMS_POLL_FREQ_SECS.getInt());
		}
		if(a == null ||
		   a.equals(SystemAttrEnum.DMS_PIXEL_TEST_TIMEOUT_SECS.aname()))
		{
			pxl_tst_timeout.setValue(SystemAttrEnum.
				DMS_PIXEL_TEST_TIMEOUT_SECS.getInt());
		}
		if(a == null ||
		   a.equals(SystemAttrEnum.DMS_LAMP_TEST_TIMEOUT_SECS.aname()))
		{
			lmp_tst_timeout.setValue(SystemAttrEnum.
				DMS_LAMP_TEST_TIMEOUT_SECS.getInt());
		}
		if(a == null ||
		   a.equals(SystemAttrEnum.DMS_PAGE_ON_SECS.aname()))
		{
			page_on.setValue(
				SystemAttrEnum.DMS_PAGE_ON_SECS.getFloat());
		}
		if(a == null ||
		   a.equals(SystemAttrEnum.DMS_PAGE_OFF_SECS.aname()))
		{
			page_off.setValue(
				SystemAttrEnum.DMS_PAGE_OFF_SECS.getFloat());
		}
		if(a == null ||
		   a.equals(SystemAttrEnum.INCIDENT_RING_1_MILES.aname()))
		{
			ring1.setValue(
				SystemAttrEnum.INCIDENT_RING_1_MILES.getInt());
		}
		if(a == null ||
		   a.equals(SystemAttrEnum.INCIDENT_RING_2_MILES.aname()))
		{
			ring2.setValue(
				SystemAttrEnum.INCIDENT_RING_2_MILES.getInt());
		}
		if(a == null ||
		   a.equals(SystemAttrEnum.INCIDENT_RING_3_MILES.aname()))
		{
			ring3.setValue(
				SystemAttrEnum.INCIDENT_RING_3_MILES.getInt());
		}
		if(a == null ||
		   a.equals(SystemAttrEnum.INCIDENT_RING_4_MILES.aname()))
		{
			ring4.setValue(
				SystemAttrEnum.INCIDENT_RING_4_MILES.getInt());
		}
	}
}
