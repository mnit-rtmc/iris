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
import us.mn.state.dot.tms.client.TmsConnection;

/**
 * DetectorListForm
 *
 * @author Douglas Lau
 */
public class DetectorListForm extends IndexedListForm {

	/** Frame title */
	static private final String TITLE = "Detectors";

	/** Create a new detector list form */
	public DetectorListForm(TmsConnection tc) {
		super(TITLE, tc, tc.getProxy().getDetectors(),
			Icons.getIcon("detector"));
	}

	/** Initializze the widgets in the form */
	protected void initialize() throws RemoteException {
		add(createListPanel());
		super.initialize();
	}

	/** Edit an item in the list */
	protected void editItem() throws RemoteException {
		int i = list.getList().getSelectedIndex();
		if(i >= 0) {
			connection.getDesktop().show(
				new DetectorForm(connection, i + 1));
		}
	}

	/** Get the prototype cell value */
	protected String getPrototypeCellValue() {
		return "2456  694/MtrhrnW3 ";
	}
}
