/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import us.mn.state.dot.sched.SwingRunner;
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
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.IButton;
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
	protected final JTextField nameTxt = createTextField();

	/** Camera component */
	private final IButton camera_btn = new IButton("camera.select");

	/** Location component */
	protected final JTextField locationTxt = createTextField();

	/** Operation component */
	protected final JTextField operationTxt = createTextField();

	/** Release rate component */
	protected final JTextField releaseTxt = createTextField();

	/** Cycle time component */
	protected final JTextField cycleTxt = createTextField();

	/** Queue component */
	protected final JTextField queueTxt = createTextField();

	/** Queue shrink button */
	private final IButton shrink_btn = new IButton("ramp.meter.shrink");

	/** Queue grow button */
	private final IButton grow_btn = new IButton("ramp.meter.grow");

	/** Reason the meter was locked */
	protected final JComboBox lockCmb = new JComboBox(
		RampMeterLock.getDescriptions());

	/** Metering on radio button */
	private final JRadioButton on_btn = new JRadioButton(
		I18N.get("ramp.meter.on"));

	/** Metering off radio button */
	private final JRadioButton off_btn = new JRadioButton(
		I18N.get("ramp.meter.off"));

	/** Current session */
	protected final Session session;

	/** Ramp meter manager */
	protected final MeterManager manager;

	/** Selection model */
	protected final ProxySelectionModel<RampMeter> selectionModel;

	/** Ramp meter proxy cache */
	protected final TypeCache<RampMeter> cache;

	/** Selected ramp meter */
	protected RampMeter selected = null;

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
		setTitle(I18N.get("ramp.meter.selected"));
		setEnabled(false);
		add(I18N.get("device.name"), nameTxt);
		addRow(I18N.get("camera"), camera_btn);
		camera_btn.setBorder(BorderFactory.createEtchedBorder(
			EtchedBorder.LOWERED));
		addRow(I18N.get("location"), locationTxt);
		addRow(I18N.get("device.operation"), operationTxt);
		add(I18N.get("ramp.meter.rate"), releaseTxt);
		addRow(I18N.get("ramp.meter.cycle"), cycleTxt);
		add(I18N.get("ramp.meter.queue"), queueTxt);
		add(shrink_btn);
		addRow(grow_btn);
		add(I18N.get("ramp.meter.lock"), lockCmb);
		finishRow();
		add(I18N.get("ramp.meter.metering"), on_btn);
		addRow(off_btn);
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
			SwingRunner.invoke(new Runnable() {
				public void run() {
					setSelected(null);
				}
			});
		}
	}

	/** A proxy has been changed */
	public void proxyChanged(final RampMeter proxy, final String a) {
		if(proxy == selected) {
			SwingRunner.invoke(new Runnable() {
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
		if(proxy != null) {
			shrink_btn.setAction(new ShrinkQueueAction(proxy));
			grow_btn.setAction(new GrowQueueAction(proxy));
			lockCmb.setAction(new LockMeterAction(proxy, lockCmb));
			on_btn.setAction(new TurnOnAction(proxy));
			off_btn.setAction(new TurnOffAction(proxy));
			updateAttribute(proxy, null);
		} else {
			nameTxt.setText("");
			locationTxt.setText("");
			operationTxt.setText("");
			operationTxt.setForeground(null);
			operationTxt.setBackground(null);
			releaseTxt.setText("");
			cycleTxt.setText("");
			queueTxt.setText("");
			shrink_btn.setEnabled(false);
			grow_btn.setEnabled(false);
		}
		setEnabled(canUpdate(proxy));
	}

	/** Enable or disable the status panel */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		lockCmb.setEnabled(enabled);
		on_btn.setEnabled(enabled);
		off_btn.setEnabled(enabled);
	}

	/** Set the camera action */
	protected void setCameraAction(RampMeter meter) {
		Camera cam = RampMeterHelper.getCamera(meter);
		if(cam != null) {
			camera_btn.setAction(new CameraSelectAction(cam,
			    session.getCameraManager().getSelectionModel()));
		} else
			camera_btn.setAction(null);
		camera_btn.setEnabled(cam != null);
	}

	/** Update one attribute on the form */
	protected void updateAttribute(RampMeter meter, String a) {
		if(a == null || a.equals("name"))
			nameTxt.setText(meter.getName());
		if(a == null || a.equals("camera"))
			setCameraAction(meter);
		// FIXME: this won't update when geoLoc attributes change
		if(a == null || a.equals("geoLoc")) {
			locationTxt.setText(GeoLocHelper.getOnRampDescription(
				meter.getGeoLoc()));
		}
		if(a == null || a.equals("operation")) {
			if(RampMeterHelper.isFailed(meter)) {
				operationTxt.setForeground(Color.WHITE);
				operationTxt.setBackground(Color.GRAY);
			} else {
				operationTxt.setForeground(null);
				operationTxt.setBackground(null);
			}
			operationTxt.setText(meter.getOperation());
		}
		if(a == null || a.equals("rate")) {
			Integer rate = meter.getRate();
			releaseTxt.setText(formatRelease(rate));
			cycleTxt.setText(formatCycle(rate));
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
			queueTxt.setText(q.description);
		}
		if(a == null || a.equals("mLock")) {
			Integer ml = meter.getMLock();
			if(ml != null)
				lockCmb.setSelectedIndex(ml);
			else
				lockCmb.setSelectedIndex(0);
		}
	}

	/** Check if the user can update the given ramp meter */
	protected boolean canUpdate(RampMeter meter) {
		return session.canUpdate(meter, "rateNext") &&
		       session.canUpdate(meter, "mLock");
	}
}
