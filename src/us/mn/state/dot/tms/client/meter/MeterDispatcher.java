/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
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
import java.awt.GridLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.MeterLock;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.CameraPresetAction;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IPanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * The MeterDispatcher provides a GUI representation for RampMeter status
 * information.
 *
 * @author Douglas Lau
 */
public class MeterDispatcher extends IPanel implements ProxyView<RampMeter> {

	/** Current session */
	private final Session session;

	/** Ramp meter manager */
	private final MeterManager manager;

	/** Selection model */
	private final ProxySelectionModel<RampMeter> sel_mdl;

	/** Selection listener */
	private final ProxySelectionListener sel_listener =
		new ProxySelectionListener()
	{
		public void selectionChanged() {
			setSelected(sel_mdl.getSingleSelection());
		}
	};

	/** Name label */
	private final JLabel name_lbl = createValueLabel();

	/** Camera preset button */
	private final JButton preset_btn = new JButton();

	/** Location label */
	private final JLabel location_lbl = createValueLabel();

	/** Status label */
	private final JLabel status_lbl = createValueLabel();

	/** Reason the meter was locked */
	private final JComboBox<String> reason_cbx = new JComboBox<String>(
		MeterLock.REASONS);

	/** Queue label */
	private final JLabel queue_lbl = createValueLabel();

	/** Queue shrink button */
	private final JButton shrink_btn = new JButton();

	/** Queue grow button */
	private final JButton grow_btn = new JButton();

	/** Release rate label */
	private final JLabel release_lbl = createValueLabel();

	/** Cycle time label */
	private final JLabel cycle_lbl = createValueLabel();

	/** Proxy watcher */
	private final ProxyWatcher<RampMeter> watcher;

	/** Create a new ramp meter dispatcher */
	public MeterDispatcher(Session s, MeterManager m) {
		session = s;
		manager = m;
		sel_mdl = manager.getSelectionModel();
		TypeCache<RampMeter> cache =
			session.getSonarState().getRampMeters();
		watcher = new ProxyWatcher<RampMeter>(cache, this, true);
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		setTitle(I18N.get("ramp_meter.selected"));
		add("device.name");
		add(name_lbl);
		add("camera");
		add(preset_btn, Stretch.LAST);
		preset_btn.setBorder(UI.buttonBorder());
		add("location");
		add(location_lbl, Stretch.LAST);
		add("device.status");
		add(status_lbl, Stretch.LAST);
		// Make label opaque so that we can set the background color
		status_lbl.setOpaque(true);
		add("ramp.meter.lock");
		add(reason_cbx, Stretch.LAST);
		add("ramp.meter.queue");
		add(queue_lbl);
		add(shrink_btn, Stretch.NONE);
		add(grow_btn, Stretch.LAST);
		add("ramp.meter.rate");
		add(release_lbl);
		add("ramp.meter.cycle");
		add(cycle_lbl, Stretch.LAST);
		watcher.initialize();
		clear();
		sel_mdl.addProxySelectionListener(sel_listener);
	}

	/** Dispose of the panel */
	@Override
	public void dispose() {
		watcher.dispose();
		sel_mdl.removeProxySelectionListener(sel_listener);
		clear();
		super.dispose();
	}

	/** Set the selected ramp meter */
	public void setSelected(RampMeter rm) {
		watcher.setProxy(rm);
	}

	/** Called when all proxies have been enumerated (from ProxyView). */
	@Override
	public void enumerationComplete() { }

	/** Update one attribute on the form */
	@Override
	public void update(RampMeter rm, String a) {
		if (a == null || a.equals("name"))
			name_lbl.setText(rm.getName());
		if (a == null || a.equals("preset"))
			setPresetAction(rm);
		// FIXME: this won't update when geoLoc attributes change
		if (a == null || a.equals("geoLoc")) {
			location_lbl.setText(GeoLocHelper.getOnRampLocation(
				rm.getGeoLoc()));
		}
		if (a == null || a.equals("lock"))
			updateLock(rm);
		if (a == null || a.equals("status") || a.equals("styles")) {
			updateRate(rm);
			updateStatus(rm);
			String q = RampMeterHelper.optQueue(rm);
			queue_lbl.setText((q != null) ? q : "");
		}
	}

	/** Set the camera preset action */
	private void setPresetAction(RampMeter rm) {
		CameraPreset cp = (rm != null) ? rm.getPreset() : null;
		preset_btn.setAction(new CameraPresetAction(session, cp));
	}

	/** Update the ramp meter lock */
	private void updateLock(RampMeter rm) {
		// Remove action so we can update the lock reason in peace
		reason_cbx.setAction(null);
		MeterLock lk = new MeterLock(RampMeterHelper.optLock(rm));
		String r = lk.optReason();
		reason_cbx.setSelectedItem((r != null) ? r : "");
		String user = session.getUser().getName();
		LockReasonAction reason_act = new LockReasonAction(rm, user,
			reason_cbx);
		ShrinkQueueAction sq_act = new ShrinkQueueAction(rm, user);
		GrowQueueAction gq_act = new GrowQueueAction(rm, user);
		if (!isWritePermitted(rm)) {
			reason_act.setEnabled(false);
			sq_act.setEnabled(false);
			gq_act.setEnabled(false);
		}
		boolean metering = RampMeterHelper.optRate(rm) != null;
		if (!metering)
			sq_act.setEnabled(false);
		reason_cbx.setAction(reason_act);
		shrink_btn.setAction(sq_act);
		grow_btn.setAction(gq_act);
	}

	/** Update the ramp meter release rate */
	private void updateRate(RampMeter rm) {
		Integer rt = RampMeterHelper.optRate(rm);
		release_lbl.setText(RampMeterHelper.formatRelease(rt));
		cycle_lbl.setText(RampMeterHelper.formatCycle(rt));
	}

	/** Update the ramp meter status */
	private void updateStatus(RampMeter rm) {
		String fault = RampMeterHelper.optFault(rm);
		String status = (fault != null) ? fault : "";
		if (ItemStyle.OFFLINE.checkBit(rm.getStyles())) {
			status_lbl.setForeground(Color.WHITE);
			status_lbl.setBackground(Color.GRAY);
			status = "OFFLINE";
		} else if (fault != null) {
			status_lbl.setForeground(Color.WHITE);
			status_lbl.setBackground(Color.BLACK);
		} else {
			status_lbl.setForeground(null);
			status_lbl.setBackground(null);
		}
		status_lbl.setText(status);
	}

	/** Clear the proxy view */
	@Override
	public void clear() {
		name_lbl.setText("");
		setPresetAction(null);
		location_lbl.setText("");
		status_lbl.setText("");
		status_lbl.setForeground(null);
		status_lbl.setBackground(null);
		queue_lbl.setText("");
		release_lbl.setText("");
		cycle_lbl.setText("");
		updateLock(null);
	}

	/** Check if the user is permitted to update the given ramp meter */
	private boolean isWritePermitted(RampMeter rm) {
		return session.isWritePermitted(rm, "lock");
	}
}
