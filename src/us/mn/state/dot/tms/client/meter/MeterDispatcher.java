/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.RampMeterLock;
import us.mn.state.dot.tms.RampMeterQueue;
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

	/** Operation label */
	private final JLabel operation_lbl = createValueLabel();

	/** Release rate label */
	private final JLabel release_lbl = createValueLabel();

	/** Cycle time label */
	private final JLabel cycle_lbl = createValueLabel();

	/** Queue label */
	private final JLabel queue_lbl = createValueLabel();

	/** Queue shrink button */
	private final JButton shrink_btn = new JButton();

	/** Queue grow button */
	private final JButton grow_btn = new JButton();

	/** Reason the meter was locked */
	private final JComboBox<RampMeterLock> lock_cbx = new JComboBox
		<RampMeterLock>(RampMeterLock.values());

	/** Metering on radio button */
	private final JRadioButton on_btn = new JRadioButton(
		I18N.get("ramp.meter.on"));

	/** Metering off radio button */
	private final JRadioButton off_btn = new JRadioButton(
		I18N.get("ramp.meter.off"));

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
		ButtonGroup group = new ButtonGroup();
		group.add(on_btn);
		group.add(off_btn);
		JPanel b_pnl = new JPanel(new GridLayout(1, 2));
		b_pnl.add(on_btn);
		b_pnl.add(off_btn);
		setTitle(I18N.get("ramp_meter.selected"));
		add("device.name");
		add(name_lbl);
		add("camera");
		add(preset_btn, Stretch.LAST);
		preset_btn.setBorder(UI.buttonBorder());
		add("location");
		add(location_lbl, Stretch.LAST);
		add("device.operation");
		add(operation_lbl, Stretch.LAST);
		// Make label opaque so that we can set the background color
		operation_lbl.setOpaque(true);
		add("ramp.meter.rate");
		add(release_lbl);
		add("ramp.meter.cycle");
		add(cycle_lbl, Stretch.LAST);
		add("ramp.meter.queue");
		add(queue_lbl);
		add(shrink_btn, Stretch.NONE);
		add(grow_btn, Stretch.LAST);
		add("ramp.meter.lock");
		add(lock_cbx, Stretch.LAST);
		add("ramp.meter.metering");
		add(b_pnl, Stretch.LAST);
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
		if(a == null)
			updateConfig(rm);
		if(a == null || a.equals("name"))
			name_lbl.setText(rm.getName());
		if (a == null || a.equals("preset"))
			setPresetAction(rm);
		// FIXME: this won't update when geoLoc attributes change
		if(a == null || a.equals("geoLoc")) {
			location_lbl.setText(GeoLocHelper.getOnRampDescription(
				rm.getGeoLoc()));
		}
		if(a == null || a.equals("operation"))
			operation_lbl.setText(rm.getOperation());
		if(a == null || a.equals("rate")) {
			Integer rt = rm.getRate();
			release_lbl.setText(RampMeterHelper.formatRelease(rt));
			cycle_lbl.setText(RampMeterHelper.formatCycle(rt));
			if(rt != null)
				on_btn.setSelected(true);
			else
				off_btn.setSelected(true);
			boolean up = isWritePermitted(rm) && rt != null;
			shrink_btn.setEnabled(up);
			grow_btn.setEnabled(up);
		}
		if(a == null || a.equals("queue")) {
			RampMeterQueue q = RampMeterQueue.fromOrdinal(
				rm.getQueue());
			queue_lbl.setText(q.description);
		}
		if(a == null || a.equals("mLock")) {
			lock_cbx.setAction(null);
			lock_cbx.setSelectedIndex(getMLock(rm));
			lock_cbx.setAction(new LockMeterAction(rm, lock_cbx,
				isWritePermitted(rm)));
		}
		if(a == null || a.equals("styles")) {
			if(ItemStyle.FAILED.checkBit(rm.getStyles())) {
				operation_lbl.setForeground(Color.WHITE);
				operation_lbl.setBackground(Color.GRAY);
			} else {
				operation_lbl.setForeground(null);
				operation_lbl.setBackground(null);
			}
		}
	}

	/** Update the ramp meter config */
	private void updateConfig(RampMeter rm) {
		boolean update = isWritePermitted(rm);
		setPresetAction(rm);
		shrink_btn.setAction(new ShrinkQueueAction(rm, update));
		grow_btn.setAction(new GrowQueueAction(rm, update));
		on_btn.setAction(new TurnOnAction(rm, update));
		off_btn.setAction(new TurnOffAction(rm, update));
		lock_cbx.setAction(new LockMeterAction(rm, lock_cbx, update));
	}

	/** Set the camera preset action */
	private void setPresetAction(RampMeter rm) {
		CameraPreset cp = RampMeterHelper.getPreset(rm);
		preset_btn.setAction(new CameraPresetAction(session, cp));
	}

	/** Get the current meter lock */
	private int getMLock(RampMeter rm) {
		Integer ml = rm.getMLock();
		return (ml != null) ? ml : RampMeterLock.OFF.ordinal();
	}

	/** Clear the proxy view */
	@Override
	public void clear() {
		name_lbl.setText("");
		setPresetAction(null);
		location_lbl.setText("");
		operation_lbl.setText("");
		operation_lbl.setForeground(null);
		operation_lbl.setBackground(null);
		release_lbl.setText("");
		cycle_lbl.setText("");
		queue_lbl.setText("");
		shrink_btn.setAction(new ShrinkQueueAction(null, false));
		grow_btn.setAction(new GrowQueueAction(null, false));
		lock_cbx.setAction(new LockMeterAction(null, lock_cbx, false));
		lock_cbx.setSelectedIndex(0);
		on_btn.setAction(new TurnOnAction(null, false));
		off_btn.setAction(new TurnOffAction(null, false));
	}

	/** Check if the user is permitted to update the given ramp meter */
	private boolean isWritePermitted(RampMeter rm) {
		return session.isWritePermitted(rm, "rateNext") &&
		       session.isWritePermitted(rm, "mLock");
	}
}
