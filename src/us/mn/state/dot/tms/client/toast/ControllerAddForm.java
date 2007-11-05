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
package us.mn.state.dot.tms.client.toast;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Circuit;
import us.mn.state.dot.tms.CommunicationLine;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.utils.ActionJob;

/**
 * ControllerAddForm is a Swing dialog for adding a Controller
 *
 * @author Sandy Dinh
 * @author Douglas Lau
 */
public class ControllerAddForm extends TMSObjectForm {

	/** Frame title */
	static protected final String TITLE = "Add Controller;";

	/** Remote CommunicationLine object */
	protected final CommunicationLine line;

	/** Add button */
	protected final JButton add = new JButton( "Add" );

	/** Circuits array */
	protected Circuit[] circuits;

	/** Circuits list model */
	protected final DefaultComboBoxModel circuitModel =
		new DefaultComboBoxModel();

	/** Circuit combo box */
	protected final JComboBox circuitBox = new JComboBox( circuitModel );

	/** Circuit button */
	protected final JButton circuitButton = new JButton( "Circuit" );

	/** Text field for drop address */
	protected final JTextField drop = new JTextField(8);

	/** Create a new controller adding form */
	public ControllerAddForm(TmsConnection tc, CommunicationLine l,
		int index)
	{
		super(TITLE + " Line " + index, tc);
		line = l;
		obj = line;
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		circuits = line.getCircuits();
		for(Circuit c: circuits)
			circuitModel.addElement(c.getId());

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(createAddPanel());
		add(Box.createVerticalStrut(VGAP));

		new ActionJob( this, add ) {
			public void perform() throws Exception {
				addPressed();
			}
		};
	}

	/** Get the selected circuit */
	protected Circuit getSelectedCircuit() throws ChangeVetoException {
		try {
			return circuits[circuitBox.getSelectedIndex()];
		}
		catch(IndexOutOfBoundsException e) {
			throw new ChangeVetoException("Invalid circuit");
		}
	}

	/** This is called when the 'add' button is pressed */
	protected void addPressed() throws Exception {
		Circuit circuit = getSelectedCircuit();
		circuit.addController(Short.parseShort(drop.getText()));
		circuit.notifyUpdate();
		line.notifyUpdate();
		drop.setText("");
	}

	/** Create the device panel */
	protected JPanel createAddPanel() {
		GridBagLayout lay = new GridBagLayout();
		JPanel panel = new JPanel( lay );
		panel.setBorder( BORDER );
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 0;
		bag.gridy = GridBagConstraints.RELATIVE;
		bag.insets.top = 4;
		bag.insets.right = 2;
		bag.anchor = GridBagConstraints.EAST;
		JLabel dropLabel = new JLabel( "Drop" );
		lay.setConstraints( dropLabel, bag );
		panel.add( dropLabel );
		lay.setConstraints( circuitButton, bag );
		panel.add( circuitButton );
		bag.gridwidth = 2;
		bag.insets.top = 16;
		bag.anchor = GridBagConstraints.CENTER;
		lay.setConstraints(add, bag);
		new ActionJob(this, circuitButton) {
			public void perform() throws RemoteException {
				connection.getDesktop().show(
					new SonetRingForm(connection));
			}
		};
		panel.add( add );
		bag.gridwidth = 1;
		bag.gridx = 1;
		bag.gridy = 0;
		bag.insets.left = 2;
		bag.insets.top = 4;
		bag.fill = GridBagConstraints.HORIZONTAL;
		lay.setConstraints(drop, bag);
		panel.add(drop);
		bag.gridy = GridBagConstraints.RELATIVE;
		lay.setConstraints( circuitBox, bag );
		panel.add( circuitBox );
		return panel;
	}
}
