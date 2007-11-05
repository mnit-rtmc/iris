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
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import us.mn.state.dot.tms.CommunicationLine;
import us.mn.state.dot.tms.IndexedList;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.utils.ActionJob;
import us.mn.state.dot.tms.utils.TMSProxy;

/**
 * CommunicationLineForm is a Swing dialog for entering and editing
 * CommunicationLine records
 *
 * @author Douglas Lau
 * @author Sandy Dinh
 */
class CommunicationLineForm extends TMSObjectForm {

	/** Frame title */
	static protected final String TITLE = "Communication Line ";

	/** Baud rate constants */
	static protected final String[] BAUDS =
		{ "1200", "2400", "9600", "19200", "38400" };

	/** Communication line index */
	protected final int index;

	/** Remote list object */
	protected IndexedList rList;

	/** Remote communication line object */
	protected CommunicationLine line;

	/** Description text field */
	protected final JTextField desc = new JTextField( 20 );

	/** Port text field */
	protected final JTextField port = new JTextField( 12 );

	/** Protocol combo box */
	protected final JComboBox protocol = new JComboBox(
		CommunicationLine.PROTOCOLS);

	/** Baud rate combo box */
	protected final JComboBox baudRates = new JComboBox( BAUDS );

	/** Poll timeout text field */
	protected final JTextField timeout = new JTextField(8);

	/** Status label */
	protected final JLabel stat = new JLabel( "OK" );

	/** Load label */
	protected final JLabel load = new JLabel(UNKNOWN);

	/** Drops list model */
	protected final DefaultListModel model = new DefaultListModel();

	/** "Apply changes" button */
	protected final JButton apply = new JButton( "Apply Changes" );

	/** Create a new CommunicationLineForm */
	public CommunicationLineForm(TmsConnection tc, int i) {
		super(TITLE + i, tc);
		index = i;
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		TMSProxy tms = connection.getProxy();
		rList = (IndexedList)tms.getLines().getList();
		obj = rList.getElement(index);
		line = (CommunicationLine)obj;
		super.initialize();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(createPortPanel());
		if(admin) {
			add(Box.createVerticalStrut(VGAP));
			new ActionJob(this, apply) {
				public void perform() throws Exception {
					applyPressed();
				}
			};
			add(apply);
		}
		add(Box.createVerticalStrut(VGAP));
	}

	/** Create the port panel */
	protected JPanel createPortPanel() {
		JPanel panel = new JPanel();
		GridBagLayout bag = new GridBagLayout();
		GridBagConstraints con = new GridBagConstraints();
		panel.setLayout( bag );
		panel.setBorder( BORDER );
		con.gridx = 0;
		con.gridy = GridBagConstraints.RELATIVE;
		con.anchor = GridBagConstraints.EAST;
		con.insets.top = 8;
		con.insets.right = 4;
		JLabel label = new JLabel( "Description" );
		bag.setConstraints( label, con );
		panel.add( label );
		label = new JLabel( "Serial Port" );
		bag.setConstraints( label, con );
		panel.add( label );
		label = new JLabel( "Protocol" );
		bag.setConstraints( label, con );
		panel.add( label );
		label = new JLabel( "Baud Rate" );
		bag.setConstraints( label, con );
		panel.add( label );
		label = new JLabel("Timeout (ms)");
		bag.setConstraints(label, con);
		panel.add(label);
		con.gridx = 1;
		con.gridy = 0;
		con.anchor = GridBagConstraints.WEST;
		con.insets.right = 0;
		bag.setConstraints( desc, con );
		desc.setEnabled( admin );
		panel.add( desc );
		con.gridy = GridBagConstraints.RELATIVE;
		bag.setConstraints( port, con );
		port.setEnabled( admin );
		panel.add( port );
		bag.setConstraints( protocol, con );
		protocol.setEnabled( admin );
		panel.add( protocol );
		bag.setConstraints( baudRates, con );
		baudRates.setEnabled( admin );
		panel.add( baudRates );
		bag.setConstraints(timeout, con);
		timeout.setEnabled(admin);
		panel.add(timeout);
		return panel;
	}

	/** Update the form with the current state */
	protected void doUpdate() throws RemoteException {
		desc.setText( line.getDescription() );
		port.setText( line.getPort() );
		protocol.setSelectedIndex( line.getProtocol() );
		switch( line.getBitRate() ) {
			case 1200:
				baudRates.setSelectedIndex( 0 );
				break;
			case 2400:
				baudRates.setSelectedIndex( 1 );
				break;
			case 9600:
				baudRates.setSelectedIndex( 2 );
				break;
			case 19200:
				baudRates.setSelectedIndex( 3 );
				break;
			case 38400:
				baudRates.setSelectedIndex( 4 );
				break;
		}
		timeout.setText(Integer.toString(line.getTimeout()));
	}

	/** This is called when the 'apply' button is pressed */
	protected void applyPressed() throws Exception {
		int t = Integer.parseInt(timeout.getText());
		line.setProtocol( (short)protocol.getSelectedIndex() );
		line.setDescription( desc.getText() );
		line.setPort( port.getText() );
		switch( baudRates.getSelectedIndex() ) {
			case 0:
				line.setBitRate( 1200 );
				break;
			case 1:
				line.setBitRate( 2400 );
				break;
			case 2:
				line.setBitRate( 9600 );
				break;
			case 3:
				line.setBitRate( 19200 );
				break;
			case 4:
				line.setBitRate( 38400 );
				break;
		}
		line.setTimeout(t);
		line.notifyUpdate();
		rList.update( index );
	}
}
