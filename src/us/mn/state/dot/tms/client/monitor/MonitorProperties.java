/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.monitor;

import java.rmi.RemoteException;
import javax.swing.JCheckBox;
import us.mn.state.dot.tms.Monitor;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.SwitcherComponentForm;

/**
 * This is a form for viewing and editing the properties of a monitor.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class MonitorProperties extends SwitcherComponentForm {

	/** Frame title */
	static private final String TITLE = "Monitor: ";

	/** Monitor ID */
	protected final String id;

	/** Remote monitor object */
	protected Monitor monitor;

	/** Active checkbox */
	protected final JCheckBox active = new JCheckBox("Active");

	/** Create a new monitor properties form */
	public MonitorProperties(TmsConnection tc, String _id) {
		super(TITLE + _id, tc);
		id = _id;
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		SortedList s =
			(SortedList)connection.getProxy().getMonitorList();
		monitor = (Monitor)s.getElement(id);
		obj = monitor;
		super.initialize();
	}

	/** Apply button is pressed */
	protected void applyPressed() throws Exception {
		super.applyPressed();
		monitor.notifyUpdate();
	}

	/** Update the form with the current state of the monitor */
	protected void doUpdate() throws RemoteException {
		super.doUpdate();
	}
}
