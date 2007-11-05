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
package us.mn.state.dot.tms.client.meter;

import java.rmi.RemoteException;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.AddForm;
import us.mn.state.dot.tms.client.toast.Icons;
import us.mn.state.dot.tms.client.toast.SortedListForm;

/**
 * RampMeterListForm
 *
 * @author Douglas Lau
 */
public class RampMeterListForm extends SortedListForm {

	/** Frame title */
	static protected final String TITLE = "Ramp Meters";

	/** Add title */
	static protected final String ADD_TITLE = "Add Ramp Meters";

	/** Create a new ramp meter list form */
	public RampMeterListForm(TmsConnection tc) {
		super(TITLE, tc, tc.getProxy().getRampMeters(),
			Icons.getIcon("meter-inactive"));
	}

	/** Initializze the widgets in the form */
	protected void initialize() throws RemoteException {
		add(createListPanel());
		super.initialize();
	}

	/** Add an item to the list */
	protected void addItem() throws Exception {
		connection.getDesktop().show(new AddMeterForm());
		updateButtons();
	}

	/** Edit an item in the list */
	protected void editItem() throws RemoteException {
		String id = getSelectedItem();
		if(id != null) {
			connection.getDesktop().show(
				new RampMeterProperties(connection, id));
		}
	}

	/** Get the prototype cell value */
	protected String getPrototypeCellValue() {
		return "M694W55";
	}

	/** Form to add a ramp meter to the list */
	protected class AddMeterForm extends AddForm {

		/** Create a ramp meter add form */
		public AddMeterForm() {
			super(ADD_TITLE, sList);
		}

		/** Add an item to the list */
		protected String addItem() throws Exception {
			String id = super.addItem();
			if(id == null)
				return id;
			connection.getDesktop().show(
				new RampMeterProperties(connection, id));
			return id;
		}
	}
}
