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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import us.mn.state.dot.tms.IndexedList;
import us.mn.state.dot.tms.PixFont;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.utils.ActionJob;
import us.mn.state.dot.tms.utils.RemoteListModel;

/**
 * PixFontForm is a Swing dialog for entering and editing pixel fonts
 *
 * @author Douglas Lau
 */
public class PixFontForm extends IndexedListForm {

	/** Frame title */
	static private final String TITLE = "Font: ";

	/** Remote font list object */
	protected IndexedList fList;

	/** Font index */
	protected final int index;

	/** Remote pixel font object */
	protected PixFont font;

	/** Font name */
	protected final JTextField name = new JTextField( 6 );

	/** Font height slider */
	protected final JSlider height = new JSlider( 4, 24, 7 );

	/** Character spacing slider */
	protected final JSlider cSpace = new JSlider( 0, 8, 1 );

	/** Line spacing slider */
	protected final JSlider lSpace = new JSlider( 0, 8, 0 );

	/** Version ID */
	protected final JTextField version = new JTextField( 6 );

	/** Apply button */
	protected final JButton apply = new JButton( "Apply Changes" );

	/** Create a PixFontForm */
	public PixFontForm(TmsConnection tc, RemoteListModel l, int i) {
		super(TITLE + i, tc, l, Icons.getIcon("font"));
		index = i;
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		fList = (IndexedList)connection.getProxy().getFonts().getList();
		obj = fList.getElement(index);
		font = (PixFont)obj;
		super.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add( "General", createGeneralPanel() );
		tab.add( "Characters", createListPanel() );
		add(tab);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Create the general panel */
	protected JPanel createGeneralPanel() {
		JPanel panel = new JPanel();
		panel.setBorder( BORDER );
		GridBagLayout lay = new GridBagLayout();
		panel.setLayout( lay );
		GridBagConstraints con = new GridBagConstraints();
		con.gridx = 0;
		con.gridy = GridBagConstraints.RELATIVE;
		con.insets.top = 2;
		con.insets.right = 4;
		con.anchor = GridBagConstraints.EAST;
		JLabel label = new JLabel( "Name" );
		lay.setConstraints( label, con );
		panel.add( label );
		label = new JLabel( "Height" );
		lay.setConstraints( label, con );
		panel.add( label );
		label = new JLabel( "Char Spacing" );
		lay.setConstraints( label, con );
		panel.add( label );
		label = new JLabel( "Line Spacing" );
		lay.setConstraints( label, con );
		panel.add( label );
		label = new JLabel( "Version ID" );
		lay.setConstraints( label, con );
		panel.add( label );
		con.gridx = 1;
		con.gridy = 0;
		con.insets.right = 0;
		con.anchor = GridBagConstraints.WEST;
		con.fill = GridBagConstraints.HORIZONTAL;
		name.setEnabled( admin );
		lay.setConstraints( name, con );
		panel.add( name );
		con.gridy = GridBagConstraints.RELATIVE;
		height.setEnabled( admin );
		height.setMajorTickSpacing( 4 );
		height.setMinorTickSpacing( 1 );
		height.setSnapToTicks( true );
		height.setPaintTicks( true );
		height.setLabelTable( height.createStandardLabels( 4 ) );
		height.setPaintLabels( true );
		lay.setConstraints( height, con );
		panel.add( height );
		cSpace.setEnabled( admin );
		cSpace.setSnapToTicks( true );
		cSpace.setLabelTable( cSpace.createStandardLabels( 1 ) );
		cSpace.setPaintLabels( true );
		lay.setConstraints( cSpace, con );
		panel.add( cSpace );
		lSpace.setEnabled( admin );
		lSpace.setSnapToTicks( true );
		lSpace.setLabelTable( lSpace.createStandardLabels( 1 ) );
		lSpace.setPaintLabels( true );
		lay.setConstraints( lSpace, con );
		panel.add( lSpace );
		version.setEnabled( admin );
		lay.setConstraints( version, con );
		panel.add( version );
		con.fill = GridBagConstraints.NONE;
		if( admin ) {
			con.gridx = 0;
			con.gridwidth = 2;
			con.insets.top = 8;
			con.anchor = GridBagConstraints.CENTER;
			lay.setConstraints( apply, con );
			panel.add( apply );
			new ActionJob( this, apply ) {
				public void perform() throws Exception {
					applyPressed();
				}
			};
		}
		return panel;
	}

	/** Update the RoadwayForm with the current state of the roadway */
	protected void doUpdate() throws RemoteException {
		name.setText( font.getName() );
		height.setValue( font.getHeight() );
		cSpace.setValue( font.getCharacterSpacing() );
		lSpace.setValue( font.getLineSpacing() );
		version.setText( String.valueOf( font.getVersionID() ) );
	}

	/** This is called when the 'apply' button is pressed */
	protected void applyPressed() throws TMSException, RemoteException {
		int v = Integer.parseInt( version.getText().trim() );
		font.setName( name.getText() );
		font.setHeight( height.getValue() );
		font.setCharacterSpacing( cSpace.getValue() );
		font.setLineSpacing( lSpace.getValue() );
		font.setVersionID( v );
		font.notifyUpdate();
		fList.update( index );
	}

	/** Edit a character in the font */
	protected void editItem() throws RemoteException {
		int i = list.getList().getSelectedIndex();
		if(i >= 0) {
			String title = "Font: " + font.getIndex() +
				", Character: " + (i + 1);
			connection.getDesktop().show(new PixCharacterForm(title,
				connection, font, i + 1));
		}
	}

	/** Get the prototype cell value */
	protected String getPrototypeCellValue() {
		return "127  (z)";
	}
}
