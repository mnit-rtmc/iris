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
package us.mn.state.dot.tms.client.roads;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import us.mn.state.dot.tms.Roadway;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.TMSObjectForm;
import us.mn.state.dot.tms.utils.ActionJob;
import us.mn.state.dot.tms.utils.TMSProxy;

/**
 * RoadwayForm is a Swing dialog for entering and editing Roadway records
 *
 * @author Douglas Lau
 */
public class RoadwayForm extends TMSObjectForm {

	/** Frame title */
	static private final String TITLE = "Roadway: ";

	/** Roadway directions */
	static protected final String[] DIRECTIONS = {
		" ", "North-South", "East-West"
	};

	/** Roadway name */
	protected final String name;

	/** Roadway list */
	protected SortedList rList;

	/** Remote roadway object */
	protected Roadway roadway;

	/** Roadway abbreviation (for detector names) */
	protected final JTextField abbrev = new JTextField(6);

	/** Roadway type combobox */
	protected final JComboBox type = new JComboBox(Roadway.TYPES);

	/** Roadway direction combobox */
	protected final JComboBox direction = new JComboBox(DIRECTIONS);

	/** Apply button */
	protected final JButton apply = new JButton("Apply Changes");

	/** Create a RoadwayForm */
	public RoadwayForm(TmsConnection tc, String n) {
		super(TITLE + n, tc);
		name = n;
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		TMSProxy tms = connection.getProxy();
		rList = (SortedList)tms.getRoadways().getList();
		roadway = (Roadway)rList.getElement(name);
		obj = roadway;
		super.initialize();
		GridBagLayout lay = new GridBagLayout();
		setLayout(lay);
		GridBagConstraints con = new GridBagConstraints();
		con.gridx = 0;
		con.gridy = GridBagConstraints.RELATIVE;
		con.insets.top = 2;
		con.insets.right = HGAP;
		con.anchor = GridBagConstraints.EAST;
		JLabel label = new JLabel("Abbreviation");
		lay.setConstraints(label, con);
		add(label);
		label = new JLabel("Type");
		lay.setConstraints(label, con);
		add(label);
		label = new JLabel("Direction");
		lay.setConstraints(label, con);
		add(label);
		con.gridx = 1;
		con.gridy = 0;
		con.insets.right = 0;
		con.anchor = GridBagConstraints.WEST;
		con.fill = GridBagConstraints.HORIZONTAL;
		abbrev.setEnabled(admin);
		lay.setConstraints(abbrev, con);
		add(abbrev);
		con.gridy = GridBagConstraints.RELATIVE;
		type.setEnabled(admin);
		lay.setConstraints(type, con);
		add(type);
		direction.setEnabled(admin);
		lay.setConstraints(direction, con);
		add(direction);
		con.fill = GridBagConstraints.NONE;
		if(admin) {
			con.gridx = 0;
			con.gridwidth = 2;
			con.insets.top = 8;
			con.anchor = GridBagConstraints.CENTER;
			lay.setConstraints(apply, con);
			add(apply);
			new ActionJob(this, apply) {
				public void perform() throws Exception {
					applyPressed();
				}
			};
		}
	}

	/** Update the RoadwayForm with the current state of the roadway */
	protected void doUpdate() throws RemoteException {
		abbrev.setText(roadway.getAbbreviated());
		type.setSelectedIndex(roadway.getType());
		short d = Roadway.NONE;
		switch(roadway.getDirection()) {
			case Roadway.NORTH_SOUTH:
				d = 1;
				break;
			case Roadway.EAST_WEST:
				d = 2;
				break;
			default:
				d = Roadway.NONE;
		}
		direction.setSelectedIndex(d);
	}

	/** This is called when the 'apply' button is pressed */
	protected void applyPressed() throws TMSException, RemoteException {
		roadway.setAbbreviated(abbrev.getText());
		roadway.setType((short)type.getSelectedIndex());
		short d = Roadway.NONE;
		switch(direction.getSelectedIndex()) {
			case 1: d = Roadway.NORTH_SOUTH; break;
			case 2: d = Roadway.EAST_WEST; break;
		}
		roadway.setDirection(d);
		roadway.notifyUpdate();
		rList.update(roadway.getName());
	}
}
