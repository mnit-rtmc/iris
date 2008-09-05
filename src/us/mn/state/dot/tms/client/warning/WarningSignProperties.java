/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.warning;

import java.awt.GridBagConstraints;
import java.rmi.RemoteException;
import javax.swing.ListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.TrafficDevice;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.client.toast.TrafficDeviceForm;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;

/**
 * WarningSignForm is a Swing dialog for entering and editing warning signs 
 *
 * @author Douglas Lau
 */
public class WarningSignProperties extends TrafficDeviceForm {

	/** Frame title */
	static private final String TITLE = "Warning Sign: ";

	/** Remote warning sign object */
	protected WarningSign sign;

	/** Sonar state */
	protected final SonarState state;

	/** Camera combo box */
	protected final JComboBox camera = new JComboBox();

	/** Sign text area */
	protected final JTextArea area = new JTextArea(3, 24);

	/** Create a new warning sign form */
	public WarningSignProperties(TmsConnection tc, String id) {
		super(TITLE + id, tc, id);
		state = tc.getSonarState();
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		SortedList s = (SortedList)
			connection.getProxy().getWarningSignList().getList();
		sign = (WarningSign)s.getElement(id);
		setDevice(sign);
		super.initialize();
		ListModel m = state.getCameraModel();
		camera.setModel(new WrapperComboBoxModel(m));
		location.addRow("Camera", camera);
		location.add(new JLabel("Sign Text"));
		location.setWest();
		location.setWidth(2);
		location.addRow(area);
	}

	/** Update the form with the current state of the warning sign */
	protected void doUpdate() throws RemoteException {
		super.doUpdate();
		camera.setSelectedItem(state.lookupCamera(sign.getCamera()));
		area.setText(sign.getText());
	}

	/** Apply button is pressed */
	protected void applyPressed() throws Exception {
		try {
			super.applyPressed();
			sign.setCamera(getCameraName(
				(Camera)camera.getSelectedItem()));
			sign.setText(area.getText());
		} finally {
			sign.notifyUpdate();
		}
	}
}
