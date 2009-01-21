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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterLock;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.TmsSelectionEvent;
import us.mn.state.dot.tms.client.TmsSelectionListener;
import us.mn.state.dot.tms.client.toast.FormPanel;

/**
 * The MeterStatusPanel provides a GUI representation for RampMeter status
 * information.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class MeterStatusPanel extends FormPanel
	implements TmsSelectionListener
{
	/** Name component */
	protected final JTextField nameTxt = createTextField();

	/** Camera component */
	protected final JTextField cameraTxt = createTextField();

	/** Location component */
	protected final JTextField locationTxt = createTextField();

	/** Status component */
	protected final JTextField statusTxt = createTextField();

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

	/** Ramp meter manager */
	protected final MeterManager manager;

	/** TMS connection */
	protected final TmsConnection connection;

	/** Ramp meter proxy object */
	protected RampMeter proxy = null;

	/** Create a new MeterStatusPanel */
	public MeterStatusPanel(TmsConnection tc, MeterManager m) {
		super(true);
		connection = tc;
		manager = m;
		setTitle("Selected Ramp Meter");
		setEnabled(false);
		add("Name", nameTxt);
		addRow("Camera", cameraTxt);
		addRow("Location", locationTxt);
		addRow("Status", statusTxt);
		add("Metering", meterOnBtn);
		addRow(meterOffBtn);
		addRow("Cycle Time", cycleTxt);
		add("Queue", queueTxt);
		add(shrinkBtn);
		addRow(growBtn);
		addRow("Lock", lockCmb);
		addRow(dataBtn);
		manager.getSelectionModel().addTmsSelectionListener(this);
	}

	/** Dispose of the panel */
	public void dispose() {
		manager.getSelectionModel().removeTmsSelectionListener(this);
		setMeter(null);
		removeAll();
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

	/** Select a new meter to display */
	public void setMeter(final RampMeter p) {
		proxy = p;
		if(p != null) {
			refreshUpdate();
			refreshStatus();
		} else
			clearMeter();
		setEnabled(p != null);
	}

	/** Clear the meter status panel */
	protected void clearMeter() {
		nameTxt.setText("");
		cameraTxt.setText("");
		locationTxt.setText("");
		statusTxt.setText("");
		lockCmb.setText("");
		cycleTxt.setText("");
		queueTxt.setText("");
	}

	/** Selection changed to another meter */
	public void selectionChanged(TmsSelectionEvent e) {
		final Object o = e.getSelected();
		if(o instanceof RampMeter)
			setMeter((RampMeter)o);
	}

	/** Refresh the update status of the selected ramp meter */
	public void refreshUpdate() {
		final RampMeter p = proxy;
		nameTxt.setText(p.getName());
		cameraTxt.setText(p.getCameraId());
		locationTxt.setText(p.getLocationString());
		meterOnBtn.setAction(new TurnOnAction(p));
		meterOffBtn.setAction(new TurnOffAction(p));
		shrinkBtn.setAction(new ShrinkQueueAction(p));
		growBtn.setAction(new GrowQueueAction(p));
		dataBtn.setAction(new MeterDataAction(p,
			connection.getDesktop(), connection.getDataFactory()));
	}

	/** Refresh the status of the ramp meter */
	public void refreshStatus() {
		final RampMeter p = proxy;
		Color color = Color.GRAY;
		String UNKNOWN = "???";
		String s_status = UNKNOWN;
		boolean metering = false;
		boolean meter_en = false;
		boolean isLocked = false;
		String s_lockName = "";
		String s_lockReason = "";
		String s_cycle = UNKNOWN;
		String s_queue = UNKNOWN;
		try {
			if(p == null)
				return;
			s_status = p.getStatus();
			if(s_status == null) {
				s_status = UNKNOWN;
				return;
			}
			isLocked = p.isLocked();
			if(isLocked) {
				RampMeterLock lock = p.getLock();
				s_lockName = lock.getUser();
				if(s_lockName.indexOf('.') > 0) {
					s_lockName = s_lockName.substring(0,
						s_lockName.indexOf('.'));
				}
				s_lockReason = lock.getReason();
			}
			int mode = p.getControlMode();
			if(mode == RampMeter.MODE_STANDBY ||
				mode == RampMeter.MODE_CENTRAL)
			{
				meter_en = true;
			}
			if(p.isMetering()) {
				metering = true;
				int r = p.getReleaseRate();
				int i_cycle = Math.round(36000.0f / r);
				s_cycle = (i_cycle / 10) + "." +
					(i_cycle % 10) + " seconds";
			} else
				s_cycle = "N/A";
			if(p.queueExists())
				s_queue = "Yes";
			else
				s_queue = "No";
			color = null;
		} finally {
			statusTxt.setBackground(color);
			statusTxt.setText(s_status);
			lockCmb.setText(s_lockReason);
			meterOnBtn.setEnabled(meter_en & !metering);
			meterOnBtn.setSelected(metering);
			meterOffBtn.setEnabled(meter_en & metering);
			meterOffBtn.setSelected(!metering);
			cycleTxt.setBackground(color);
			cycleTxt.setText(s_cycle);
			queueTxt.setBackground(color);
			queueTxt.setText(s_queue);
			shrinkBtn.setEnabled(meter_en && metering);
			growBtn.setEnabled(meter_en && metering);
		}
	}
}
