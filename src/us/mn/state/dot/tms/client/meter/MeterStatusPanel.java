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
package us.mn.state.dot.tms.client.meter;

import java.awt.Color;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterLock;
import us.mn.state.dot.tms.RampMeterQueue;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.sonar.ProxySelectionListener;
import us.mn.state.dot.tms.client.sonar.ProxySelectionModel;
import us.mn.state.dot.tms.client.toast.FormPanel;

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
	/** Get the verification camera name */
	static protected String getCameraName(RampMeter proxy) {
		Camera camera = proxy.getCamera();
		if(camera == null)
			return " ";
		else
			return camera.getName();
	}

	/** Get the controller status */
	static protected String getControllerStatus(RampMeter proxy) {
		Controller c = proxy.getController();
		if(c == null)
			return "???";
		else
			return c.getStatus();
	}

	/** Format the meter cycle time from the given release rate */
	static protected String formatCycle(float rate) {
		int i_cycle = Math.round(36000 / rate);
		return "" + (i_cycle / 10) + "." + (i_cycle % 10) + " seconds";
	}

	/** Name component */
	protected final JTextField nameTxt = createTextField();

	/** Camera component */
	protected final JTextField cameraTxt = createTextField();

	/** Location component */
	protected final JTextField locationTxt = createTextField();

	/** Operation component */
	protected final JTextField operationTxt = createTextField();

	/** Metering on radio button */
	protected final JRadioButton meterOnBtn = new JRadioButton("On");

	/** Metering off radio button */
	protected final JRadioButton meterOffBtn = new JRadioButton("Off");

	/** Cycle time component */
	protected final JTextField cycleTxt = createTextField();

	/** Queue component */
	protected final JTextField queueTxt = createTextField();

	/** Queue shrink button */
	protected final JButton shrinkBtn = new JButton("Shrink");

	/** Queue grow button */
	protected final JButton growBtn = new JButton("Grow");

	/** Reason the meter was locked */
	protected final JComboBox lockCmb = new JComboBox(
		RampMeterLock.getDescriptions());

	/** Button for data plotlet */
	protected final JButton dataBtn = new JButton("Data");

	/** TMS connection */
	protected final TmsConnection connection;

	/** Ramp meter manager */
	protected final MeterManager manager;

	/** Selection model */
	protected final ProxySelectionModel<RampMeter> selectionModel;

	/** Ramp meter proxy cache */
	protected final TypeCache<RampMeter> cache;

	/** Selected ramp meter */
	protected RampMeter selected = null;

	/** Create a new MeterStatusPanel */
	public MeterStatusPanel(TmsConnection tc, MeterManager m) {
		super(true);
		connection = tc;
		manager = m;
		selectionModel = manager.getSelectionModel();
		cache = tc.getSonarState().getRampMeters();
		ButtonGroup group = new ButtonGroup();
		group.add(meterOnBtn);
		group.add(meterOffBtn);
		setTitle("Selected Ramp Meter");
		setEnabled(false);
		add("Name", nameTxt);
		addRow("Camera", cameraTxt);
		addRow("Location", locationTxt);
		addRow("Operation", operationTxt);
		add("Metering", meterOnBtn);
		addRow(meterOffBtn);
		addRow("Cycle Time", cycleTxt);
		add("Queue", queueTxt);
		add(shrinkBtn);
		addRow(growBtn);
		add("Lock", lockCmb);
		finishRow();
// FIXME	addRow(dataBtn);

		setSelected(null);
		cache.addProxyListener(this);
		selectionModel.addProxySelectionListener(this);
	}

	/** Dispose of the panel */
	public void dispose() {
		selectionModel.removeProxySelectionListener(this);
		cache.removeProxyListener(this);
		setSelected(null);
		removeAll();
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
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setSelected(null);
				}
			});
		}
	}

	/** A proxy has been changed */
	public void proxyChanged(final RampMeter proxy, final String a) {
		if(proxy == selected) {
			SwingUtilities.invokeLater(new Runnable() {
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
		selected = proxy;
		if(proxy != null) {
			meterOnBtn.setAction(new TurnOnAction(proxy));
			meterOffBtn.setAction(new TurnOffAction(proxy));
			shrinkBtn.setAction(new ShrinkQueueAction(proxy));
			growBtn.setAction(new GrowQueueAction(proxy));
			lockCmb.setAction(new LockMeterAction(proxy, lockCmb));
			dataBtn.setAction(new MeterDataAction(proxy,
				connection.getDesktop(),
				connection.getDataFactory()));
			updateAttribute(proxy, null);
		} else {
			nameTxt.setText("");
			cameraTxt.setText("");
			locationTxt.setText("");
			operationTxt.setText("");
			cycleTxt.setText("");
			queueTxt.setText("");
		}
		setEnabled(proxy != null);
	}

	/** Enable or disable the status panel */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		meterOnBtn.setEnabled(enabled);
		meterOffBtn.setEnabled(enabled);
		shrinkBtn.setEnabled(enabled);
		growBtn.setEnabled(enabled);
		lockCmb.setEnabled(enabled);
		dataBtn.setEnabled(enabled);
	}

	/** Update one attribute on the form */
	protected void updateAttribute(RampMeter meter, String a) {
		if(a == null || a.equals("name"))
			nameTxt.setText(meter.getName());
		if(a == null || a.equals("camera"))
			cameraTxt.setText(getCameraName(meter));
		// FIXME: this won't update when geoLoc attributes change
		if(a == null || a.equals("geoLoc")) {
			locationTxt.setText(GeoLocHelper.getDescription(
				meter.getGeoLoc()));
		}
		if(a == null || a.equals("operation")) {
			String status = getControllerStatus(meter);
			if("".equals(status)) {
				operationTxt.setForeground(null);
				operationTxt.setBackground(null);
			} else {
				operationTxt.setForeground(Color.WHITE);
				operationTxt.setBackground(Color.GRAY);
			}
			operationTxt.setText(meter.getOperation());
		}
		if(a == null || a.equals("rate")) {
			Integer rate = meter.getRate();
			if(rate != null) {
				meterOnBtn.setSelected(true);
				cycleTxt.setText(formatCycle(rate));
			} else {
				meterOffBtn.setSelected(true);
				cycleTxt.setText("N/A");
			}
			shrinkBtn.setEnabled(rate != null);
			growBtn.setEnabled(rate != null);
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
}
