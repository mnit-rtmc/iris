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
package us.mn.state.dot.tms.client.roads;

import java.rmi.RemoteException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.Icons;
import us.mn.state.dot.tms.client.toast.AbstractListForm;
import us.mn.state.dot.tms.utils.ActionJob;
import us.mn.state.dot.tms.utils.ListSelectionJob;

/**
 * Swing dialog to view list of TMS stations
 *
 * @author Douglas Lau
 */
public class StationListForm extends AbstractListForm {

	/** Frame title */
	static protected final String TITLE = "Stations";

	/** Create a station list form component */
	public StationListForm(TmsConnection tc) {
		super(TITLE, tc, tc.getProxy().getStations(),
			Icons.getIcon("station"));
	}

	/** Initializze the widgets in the form */
	protected void initialize() throws RemoteException {
		add(createListPanel());
		super.initialize();
	}

	/** Create the common list panel */
	protected JPanel createListPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(BORDER);
		Box vbox = Box.createVerticalBox();
		JLabel image = new JLabel(icon);
		vbox.add(image);
		vbox.add(Box.createVerticalStrut(12));
		if(!admin)
			edit.setText("View");
		edit.setEnabled(false);
		vbox.add(edit);
		new ActionJob(this, edit) {
			public void perform() throws RemoteException {
				try {
					editItem();
				}
				finally {
					updateButtons();
				}
			}
		};
		panel.add(vbox);
		panel.add(Box.createHorizontalStrut(8));
		new ListSelectionJob(this, list.getList()) {
			public void perform() throws Exception {
				if(!event.getValueIsAdjusting())
					updateButtons();
			}
		};
		panel.add(list);
		return panel;
	}

	/** Add an item to the list */
	protected void addItem() {
		// Do nothing
	}

	/** Edit the specified station */
	protected void editStation(String s) throws RemoteException {
// FIXME
//		R_Node station = R_NodeProperties.lookupStation(connection, s);
//		connection.getDesktop().show(new R_NodeProperties(connection,
//			station, station.getOID()));
	}

	/** Edit an item in the list */
	protected void editItem() throws RemoteException {
		Object o = list.getList().getSelectedValue();
		// FIXME: this is an abomination
		if(o instanceof String) {
			String s = ((String)o).substring(0, 5).trim();
			editStation(s);
		}
	}

	/** Delete an item from the list */
	protected void deleteItem() {
		// Do nothing
	}

	/** Determine if a particular item is deletable */
	protected boolean isDeletable(int index) {
		return false;
	}

	/** Get the prototype cell value */
	protected String getPrototypeCellValue() {
		return "860> 694/MtrhrnW";
	}

	/** Update the buttons' enabled state */
	protected void updateButtons() {
		if(list.isSelectionEmpty())
			edit.setEnabled(false);
		else
			edit.setEnabled(true);
	}
}
