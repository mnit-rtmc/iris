/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2007  Minnesota Department of Transportation
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

import java.awt.GridBagConstraints;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.map.PointSelector;
import us.mn.state.dot.tms.Location;
import us.mn.state.dot.tms.Roadway;
import us.mn.state.dot.tms.TMSObject;
import us.mn.state.dot.tms.utils.ActionJob;
import us.mn.state.dot.tms.utils.TMSProxy;

/**
 * LocationPanel is a Swing panel for viewing and editing object locations.
 *
 * @author Douglas Lau
 */
public class LocationPanel extends FormPanel {

	/** Get the int value of a spinner */
	static protected int getSpinnerInt(JSpinner s) {
		return ((Number)s.getValue()).intValue();
	}

	/** Remote location object */
	protected final Location loc;

	/** TMS proxy object */
	protected final TMSProxy tms;

	/** Freeway combobox */
	protected final JComboBox freeBox = new JComboBox();

	/** Freeway direction combo box */
	protected final JComboBox freeDir = new JComboBox(TMSObject.DIR_LONG);

	/** Cross street modifier combobox */
	protected final JComboBox crossMod = new JComboBox(TMSObject.MODIFIER);

	/** Cross street combobox */
	protected final JComboBox crossBox = new JComboBox();

	/** Cross street direction combobox */
	protected final JComboBox crossDir = new JComboBox(TMSObject.DIRECTION);

	/** UTM Easting */
	protected final JSpinner easting = new JSpinner(
		new SpinnerNumberModel(0, 0, 1000000, 1));

	/** UTM Easting offset */
	protected final JSpinner eastOff = new JSpinner(
		new SpinnerNumberModel(0, -9999, 1000000, 1));

	/** UTM Northing */
	protected final JSpinner northing = new JSpinner(
		new SpinnerNumberModel(0, 0, 10000000, 1));

	/** UTM Northing offset */
	protected final JSpinner northOff = new JSpinner(
		new SpinnerNumberModel(0, -9999, 10000000, 1));

	/** Button to select a point from the map */
	protected final JButton select = new JButton("Select Point");

	/** Create a new location panel */
	public LocationPanel(boolean enable, Location l, TMSProxy t) {
		super(enable);
		loc = l;
		tms = t;
	}

	/** Initialize the location panel */
	public void initialize() throws RemoteException {
		freeBox.setModel(new WrapperComboBoxModel(
			tms.getFreeways().getModel()));
		crossBox.setModel(new WrapperComboBoxModel(
			tms.getRoadways().getModel()));
		add("Freeway", freeBox);
		setWidth(2);
		addRow(freeDir);
		add(crossMod);
		setWest();
		setWidth(2);
		add(crossBox);
		setWidth(1);
		addRow(crossDir);
		add("Easting", easting);
		setEast();
		addRow("East Offset", eastOff);
		add("Northing", northing);
		setEast();
		addRow("North Offset", northOff);
	}

	/** Add a "Select Point" button */
	public void addSelectPointButton(final MapBean map) {
		if(enable) {
			setWidth(4);
			addRow(select);
			new ActionJob(this, select) {
				public void perform() throws Exception {
					selectPressed(map);
				}
			};
		}
	}

	/** Add a notes text pane */
	public void addNote(JTextArea notes) {
		notes.setWrapStyleWord(true);
		notes.setLineWrap(true);
		JScrollPane note_pane = new JScrollPane(notes,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(new JLabel("Notes"));
		bag.anchor = GridBagConstraints.WEST;
		bag.fill = GridBagConstraints.BOTH;
		bag.gridwidth = 4;
		bag.weightx = 1;
		bag.weighty = 1;
		addRow(note_pane);
	}

	/** Update the location panel */
	public void doUpdate() throws RemoteException {
		Roadway freeway = loc.getFreeway();
		if(freeway != null)
			freeBox.setSelectedItem(freeway.getName());
		else
			freeBox.setSelectedItem(null);
		freeDir.setSelectedIndex(loc.getFreeDir());
		crossMod.setSelectedIndex(loc.getCrossMod());
		Roadway xstreet = loc.getCrossStreet();
		if(xstreet != null)
			crossBox.setSelectedItem(xstreet.getName());
		else
			crossBox.setSelectedItem(null);
		crossDir.setSelectedIndex(loc.getCrossDir());
		easting.setValue(loc.getEasting());
		eastOff.setValue(loc.getEastOffset());
		northing.setValue(loc.getNorthing());
		northOff.setValue(loc.getNorthOffset());
	}

	/** Apply button is pressed */
	public void applyPressed() throws Exception {
		loc.setFreeway((String)freeBox.getSelectedItem());
		loc.setFreeDir((short)freeDir.getSelectedIndex());
		loc.setCrossMod((short)crossMod.getSelectedIndex());
		loc.setCrossStreet((String)crossBox.getSelectedItem());
		loc.setCrossDir((short)crossDir.getSelectedIndex());
		int x = getSpinnerInt(easting);
		int xO = getSpinnerInt(eastOff);
		int y = getSpinnerInt(northing);
		int yO = getSpinnerInt(northOff);
		loc.setEasting(x);
		loc.setEastOffset(xO);
		loc.setNorthing(y);
		loc.setNorthOffset(yO);
	}

	/** Select point button is pressed */
	protected void selectPressed(MapBean map) {
		map.addPointSelector(new PointSelector() {
			public void selectPoint(Point2D p) {
				int x = (int)p.getX();
				eastOff.setValue(x - getSpinnerInt(easting));
				int y = (int)p.getY();
				northOff.setValue(y - getSpinnerInt(northing));
			}
		});
	}
}
