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
package us.mn.state.dot.tms.client.tour;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.Tour;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.SwitcherComponentForm;

/**
 * This is a form for viewing and editing the properties of a camera.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class TourProperties extends SwitcherComponentForm {

	/** Frame title */
	static private final String TITLE = "Tour: ";

	/** The text entry field for the tour description */
	protected final JTextField description = new JTextField();

	/** Tour ID */
	protected final String id;

	/** Remote tour object */
	protected Tour tour;

	/** Create a new tour properties form */
	public TourProperties(TmsConnection tc, String _id) {
		super(TITLE + _id, tc);
		id = _id;
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		SortedList s = (SortedList)connection.getProxy().getTourList();
		tour = (Tour)s.getElement(id);
		obj = tour;
		super.initialize();
		tab.add("Definition", createDefinitionPanel());
	}

	/** Apply button is pressed */
	protected void applyPressed() throws Exception {
		super.applyPressed();
		tour.notifyUpdate();
	}

	/** Update the form with the current state of the camera */
	protected void doUpdate() throws RemoteException {
		super.doUpdate();
	}

	protected JPanel createDefinitionPanel(){
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 0;
		p.add(new JLabel("Description"), c);
		p.add(description, c);
		return p;
	}
}
