/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import java.rmi.RemoteException;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.AddForm;
import us.mn.state.dot.tms.client.toast.Icons;
import us.mn.state.dot.tms.client.toast.SortedListForm;

/**
 * CameraListForm
 *
 * @author Douglas Lau
 */
public class CameraListForm extends SortedListForm {

	/** Frame title */
	static private final String TITLE = "Cameras";

	/** Add title */
	static private final String ADD_TITLE = "Add Cameras";

	/** Create a new camera list form */
	public CameraListForm(TmsConnection tc) {
		super(TITLE, tc, tc.getProxy().getCameras(),
			Icons.getIcon("camera"));
	}

	/** Initializze the widgets in the form */
	protected void initialize() throws RemoteException {
		add(createListPanel());
		super.initialize();
	}

	/** Add an item to the list */
	protected void addItem() throws Exception {
		connection.getDesktop().show(new AddForm(ADD_TITLE, sList));
		updateButtons();
	}

	/** Edit an item in the list */
	protected void editItem() throws RemoteException {
		String id = getSelectedItem();
		if(id != null) {
			connection.getDesktop().show(
				new CameraProperties(connection, id));
		}
	}

	/** Get the prototype cell value */
	protected String getPrototypeCellValue() {
		return "C694W55";
	}
}
