/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;
import static us.mn.state.dot.sched.SwingRunner.runSwing;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.RampMeterLock;
import us.mn.state.dot.tms.RampMeterQueue;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.CameraSelectAction;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * The MeterStatusPanel provides a GUI representation for RampMeter status
 * information.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class MeterStatusPanel extends FormPanel
	implements ProxyListener<RampMeter>, ProxySelectionListener<RampMeter>
{
	/** Format the meter release rate */
	static public String formatRelease(Integer rate) {
		if(rate !=  null) {
			return rate.toString() + " " +
				I18N.get("units.vehicles.per.hour");
		} else
			return I18N.get("units.na");
	}

	/** Format the meter cycle time from the given release rate */
	static public String formatCycle(Integer rate) {
		if(rate != null) {
			int c = Math.round(36000f / rate);
			return "" + (c / 10) + "." + (c % 10) + " " +
				I18N.get("units.s");
		} else
			return I18N.get("units.na");
	}

	/** Name component */
	private final JLabel name_lbl = createValueLabel();

	/** Camera component */
	private final JButton camera_btn = new JButton();

	/** Location component */
	private final JLabel location_lbl = createValueLabel();

	/** Operation component */
	private final JLabel operation_lbl = createValueLabel();

	/** Release rate component */
	private final JLabel release_lbl = createValueLabel();

	/** Cycle time component */
	private final JLabel cycle_lbl = createValueLabel();

	/** Queue component */
	private final JLabel queue_lbl = createValueLabel();

	/** Queue shrink button */
	private final JButton shrink_btn = new JButton();

	/** Queue grow button */
	private final JButton grow_btn = new JButton();

	/** Reason the meter was locked */
	private final JComboBox lock_cbx = new JComboBox(
		RampMeterLock.getDescriptions());

	/** Metering on radio button */
	private final JRadioButton on_btn = new JRadioButton(
		I18N.get("ramp.meter.on"));

	/** Metering off radio button */
	private final JRadioButton off_btn = new JRadioButton(
		I18N.get("ramp.meter.off"));

	/** Current session */
	private final Session session;

	/** Ramp meter manager */
	private final MeterManager manager;

	/** Selection model */
	private final ProxySelectionModel<RampMeter> selectionModel;

	/** Ramp meter proxy cache */
	private final TypeCache<RampMeter> cache;

	/** Selected ramp meter */
	private RampMeter selected = null;

	/** Create a new MeterStatusPanel */
	public MeterStatusPanel(Session s, MeterManager m) {
		super(true);
		session = s;
		manager = m;
		selectionModel = manager.getSelectionModel();
		cache = session.getSonarState().getRampMeters();
		ButtonGroup group = new ButtonGroup();
		group.add(on_btn);
		group.add(off_btn);
		JPanel b_pnl = new JPanel(new GridLayout(1, 2));
		b_pnl.add(on_btn);
		b_pnl.add(off_btn);
		setTitle(I18N.get("ramp.meter.selected"));
		setEnabled(false);
		setHeavy(true);
		add(I18N.get("device.name"), name_lbl);
		setHeavy(false);
		addRow(I18N.get("camera"), camera_btn);
		camera_btn.setBorder(BorderFactory.createEtchedBorder(
			EtchedBorder.LOWERED));
		addRow(I18N.get("location"), location_lbl);
		addRow(I18N.get("device.operation"), operation_lbl);
		// Make label opaque so that we can set the background color
		operation_lbl.setOpaque(true);
		add(I18N.get("ramp.meter.rate"), release_lbl);
		addRow(I18N.get("ramp.meter.cycle"), cycle_lbl);
		add(I18N.get("ramp.meter.queue"), queue_lbl);
		add(shrink_btn);
		addRow(grow_btn);
		add(I18N.get("ramp.meter.lock"), lock_cbx);
		finishRow();
		addRow(I18N.get("ramp.meter.metering"), b_pnl);
		setSelected(null);
		cache.addProxyListener(this);
		selectionModel.addProxySelectionListener(this);
	}

	/** Dispose of the panel */
	public void dispose() {
		selectionModel.removeProxySelectionListener(this);
		cache.removeProxyListener(this);
		setSelected(null);
		super.dispose();
	}

	/** A new proxy has been added */
	public void proxyAdded(RampMeter proxy) {
		// we're not interested
	}

	/** Enumeration of the proxy type has completed */
	public void enumerationComplete() {
		// we're not interested
	}

	/** A proxy has been removed */
	public void proxyRemoved(RampMeter proxy) {
		if(proxy == selected) {
			runSwing(new Runnable() {
				public void run() {
					setSelected(null);
				}
			});
		}
	}

	/** A proxy has been changed */
	public void proxyChanged(final RampMeter proxy, final String a) {
		if(proxy == selected) {
			runSwing(new Runnable() {
				public void run() {
					updateAttribute(proxy, a);
				}
			});
		}
	}

	/** Called whenever a meter is added to the selection */
	public void selectionAdded(RampMeter s) {
		if(selectionModel.getSelectedCount() <= 1)
			setSelected(s);
	}

	/** Called whenever a meter is removed from the selection */
	public void selectionRemoved(RampMeter s) {
		if(selectionModel.getSelectedCount() == 1) {
			for(RampMeter m: selectionModel.getSelected())
				setSelected(m);
		} else if(s == selected)
			setSelected(null);
	}

	/** Select a new meter to display */
	public void setSelected(final RampMeter proxy) {
		if(selected != null)
			cache.ignoreObject(selected);
		if(proxy != null)
			cache.watchObject(proxy);
		selected = proxy;
		setCameraAction(proxy);
		shrink_btn.setAction(new ShrinkQueueAction(proxy));
		grow_btn.setAction(new GrowQueueAction(proxy));
		if(proxy != null) {
			on_btn.setAction(new TurnOnAction(proxy));
			off_btn.setAction(new TurnOffAction(proxy));
			updateAttribute(proxy, null);
		} else {
			name_lbl.setText("");
			location_lbl.setText("");
			operation_lbl.setText("");
			operation_lbl.setForeground(null);
			operation_lbl.setBackground(null);
			release_lbl.setText("");
			cycle_lbl.setText("");
			queue_lbl.setText("");
			lock_cbx.setAction(null);
			lock_cbx.setSelectedIndex(0);
		}
		setEnabled(canUpdate(proxy));
	}

	/** Enable or disable the status panel */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		lock_cbx.setEnabled(enabled);
		on_btn.setEnabled(enabled);
		off_btn.setEnabled(enabled);
	}

	/** Set the camera action */
	private void setCameraAction(RampMeter meter) {
		Camera cam = RampMeterHelper.getCamera(meter);
		camera_btn.setAction(new CameraSelectAction(cam,
			session.getCameraManager().getSelectionModel()));
	}

	/** Update one attribute on the form */
	private void updateAttribute(RampMeter meter, String a) {
		if(a == null || a.equals("name"))
			name_lbl.setText(meter.getName());
		if(a == null || a.equals("camera"))
			setCameraAction(meter);
		// FIXME: this won't update when geoLoc attributes change
		if(a == null || a.equals("geoLoc")) {
			location_lbl.setText(GeoLocHelper.getOnRampDescription(
				meter.getGeoLoc()));
		}
		if(a == null || a.equals("operation")) {
			if(RampMeterHelper.isFailed(meter)) {
				operation_lbl.setForeground(Color.WHITE);
				operation_lbl.setBackground(Color.GRAY);
			} else {
				operation_lbl.setForeground(null);
				operation_lbl.setBackground(null);
			}
			operation_lbl.setText(meter.getOperation());
		}
		if(a == null || a.equals("rate")) {
			Integer rate = meter.getRate();
			release_lbl.setText(formatRelease(rate));
			cycle_lbl.setText(formatCycle(rate));
			if(rate != null)
				on_btn.setSelected(true);
			else
				off_btn.setSelected(true);
			shrink_btn.setEnabled(canUpdate(meter) && rate != null);
			grow_btn.setEnabled(canUpdate(meter) && rate != null);
		}
		if(a == null || a.equals("queue")) {
			RampMeterQueue q = RampMeterQueue.fromOrdinal(
				meter.getQueue());
			queue_lbl.setText(q.description);
		}
		if(a == null || a.equals("mLock")) {
			lock_cbx.setAction(null);
			lock_cbx.setSelectedIndex(getMLock(meter));
			lock_cbx.setAction(new LockMeterAction(meter,lock_cbx));
		}
	}

	/** Get the current meter lock */
	private int getMLock(RampMeter meter) {
		Integer ml = meter.getMLock();
		return ml != null ? ml : 0;
	}

	/** Check if the user can update the given ramp meter */
	private boolean canUpdate(RampMeter meter) {
		return session.canUpdate(meter, "rateNext") &&
		       session.canUpdate(meter, "mLock");
	}
}
