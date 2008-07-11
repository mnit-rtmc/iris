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
package us.mn.state.dot.tms.client.toast;

import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.Controller170;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.TMSObject;
import us.mn.state.dot.tms.TrafficDevice;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.meter.RampMeterProperties;

/**
 * Controller170Form is a Swing dialog for editing 170 controller records
 *
 * @author Douglas Lau
 */
public class Controller170Form extends ControllerForm {

	/** Remote 170 controller object */
	protected final Controller170 c170;

	/** Cabinet type combo box */
	protected final JComboBox cabinet =
		new JComboBox( Controller170.CABINET );

	/** Meter lookup button */
	protected final JButton meterButton = new JButton( "Meter (2)" );

	/** Available meter combo box */
	protected final JComboBox meterBox = new JComboBox();

	/** Create a new 170 controller form */
	protected Controller170Form(TmsConnection tc, Controller170 c,
		String label)
	{
		super(tc, c, label);
		c170 = c;
		obj = c170;
	}

	/** Initialize the form */
	protected void initialize() throws RemoteException {
		super.initialize();
		meterBox.setModel(new WrapperComboBoxModel(
			tms.getAvailableMeters().getModel()));
	}

	/** Create the device panel */
	protected JPanel createDevicePanel() {
		FormPanel panel = (FormPanel)super.createDevicePanel();
		deviceButton.setText("Device (1)");
		new ActionJob(this, meterButton) {
			public void perform() throws Exception {
				meterPressed();
			}
		};
		panel.addRow(meterButton, meterBox);
		panel.addRow("Cabinet Type", cabinet);
		return panel;
	}

	/** Get the first device */
	protected TrafficDevice getDevice() throws RemoteException {
		ControllerIO[] io_pins = c170.getIO();
		ControllerIO io = io_pins[Controller170.DEVICE_1_PIN];
		if(io instanceof TrafficDevice)
			return (TrafficDevice)io;
		else
			return null;
	}

	/** Get the second ramp meter */
	protected RampMeter getMeter2() throws RemoteException {
		ControllerIO[] io_pins = c170.getIO();
		ControllerIO io = io_pins[Controller170.METER_2_PIN];
		if(io instanceof RampMeter)
			return (RampMeter)io;
		else
			return null;
	}

	/** Update the form with the current state of the controller */
	protected void doUpdate() throws RemoteException {
		super.doUpdate();
		cabinet.setSelectedIndex(c170.getCabinet());
		RampMeter meter = getMeter2();
		if( meter != null ) {
			meterButton.setEnabled( true );
			// NOTE: must call the model's method directly
			meterBox.getModel().setSelectedItem( meter.getId() );
		} else {
			meterButton.setEnabled( false );
			// NOTE: must call the model's method directly
			meterBox.getModel().setSelectedItem( null );
		}
	}

	/** Get the I/O pin for the first device */
	protected int getDevicePin() {
		return Controller170.DEVICE_1_PIN;
	}

	/** Get the selected ramp meter (second) */
	protected RampMeter getSelectedMeter2() throws Exception {
		String d = (String)meterBox.getSelectedItem();
		if(d == null)
			return null;
		TMSObject o = devices.getElement(d);
		if(o instanceof RampMeter)
			return (RampMeter)o;
		else
			return null;
	}

	/** Set the selected device */
	protected void setSelectedDevice() throws Exception {
		super.setSelectedDevice();
		RampMeter meter = getMeter2();
		ControllerIO cio = getSelectedMeter2();
		if(cio != meter) {
			if(meter != null)
				meter.setController(null);
			if(cio != null) {
				cio.setController(null);
				cio.setPin(Controller170.METER_2_PIN);
				cio.setController(contr);
			}
		}
	}

	/** Called when the 'apply' button is pressed */
	protected void applyPressed() throws Exception {
		if(admin)
			c170.setCabinet((short)cabinet.getSelectedIndex());
		super.applyPressed();
	}

	/** Called when the meter lookup button is pressed */
	protected void meterPressed() throws Exception {
		RampMeter meter = getMeter2();
		if(meter == null)
			meterButton.setEnabled(false);
		else {
			connection.getDesktop().show(
				new RampMeterProperties(connection,
				meter.getId()));
		}
	}

	/** Get the pin for the given detector input */
	protected int getDetectorPin(int inp) {
		return inp + Controller170.DETECTOR_PIN_OFFSET;
	}
}
