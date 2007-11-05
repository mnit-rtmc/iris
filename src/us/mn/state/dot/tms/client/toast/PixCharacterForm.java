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
import java.awt.GridLayout;
import java.awt.Insets;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.IndexedList;
import us.mn.state.dot.tms.PixCharacter;
import us.mn.state.dot.tms.PixFont;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.utils.ActionJob;

/**
 * PixCharacterForm is a Swing dialog for entering and editing font characters
 *
 * @author Douglas Lau
 */
public class PixCharacterForm extends TMSObjectForm {

	/** Maximum allowed width of a character */
	private static final int MAX_WIDTH = 14;

	/** Remote font object */
	protected final PixFont font;

	/** Remote font character object */
	protected PixCharacter character;

	/** Character index */
	protected final int index;

	/** Character height */
	protected int height;

	/** Character width */
	protected int width;

	/** Pixel panel */
	protected JPanel pixelPanel;

	/** Grid layout */
	protected GridLayout grid;

	/** Pixel toggle buttons */
	protected JToggleButton[] pixels;

	/** "Narrow" button */
	protected final JButton narrow = new JButton( "<<" );

	/** "Wide" button */
	protected final JButton wide = new JButton( ">>" );

	/** Apply button */
	protected final JButton apply = new JButton( "Apply Changes" );

	/** Create a PixCharacterForm */
	public PixCharacterForm(String title, TmsConnection tc, PixFont f,
		int in)
	{
		super(title, tc);
		font = f;
		index = in;
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		IndexedList cList = (IndexedList)font.getCharacterList();
		character = (PixCharacter)cList.getElement(index);
		obj = character;
		height = font.getHeight();
		width = character.getWidth();
		grid = new GridLayout(height, width);
		int max = MAX_WIDTH * height;
		pixels = new JToggleButton[max];
		for(int i = 0; i < max; i++) {
			pixels[i] = new JToggleButton(
				Icons.getIcon("pixel-off"));
			pixels[i].setSelectedIcon(
				Icons.getIcon("pixel-on"));
			pixels[i].setBorder(null);
			pixels[i].setContentAreaFilled(false);
			pixels[i].setMargin(new Insets(0, 0, 0, 0));
		}
		super.initialize();
		GridBagLayout lay = new GridBagLayout();
		setLayout(lay);
		GridBagConstraints con = new GridBagConstraints();
		con.gridx = 0;
		con.gridy = 0;
		con.gridwidth = 3;
		con.anchor = GridBagConstraints.CENTER;
		pixelPanel = new JPanel( grid );
		pixelPanel.setBorder( BORDER );
		lay.setConstraints( pixelPanel, con );
		add(pixelPanel);
		if(admin) {
			con.gridy = 1;
			con.gridwidth = 1;
			con.weightx = 1;
			narrow.setEnabled( admin );
			lay.setConstraints( narrow, con );
			add(narrow);
			new ActionJob( this, narrow ) {
				public void perform() throws Exception {
					narrowPressed();
				}
			};
			con.gridx = 1;
			con.weightx = 0;
			JLabel label = new JLabel( String.valueOf(
				(char)index ) );
			lay.setConstraints( label, con );
			add(label);
			con.gridx = 2;
			con.weightx = 1;
			wide.setEnabled( admin );
			lay.setConstraints( wide, con );
			add(wide);
			new ActionJob( this, wide ) {
				public void perform() throws Exception {
					widePressed();
				}
			};
			con.gridx = 0;
			con.gridy = 2;
			con.gridwidth = 3;
			con.weightx = 0;
			lay.setConstraints( apply, con );
			add(apply);
			new ActionJob( this, apply ) {
				public void perform() throws Exception {
					applyPressed();
				}
			};
		}
	}

	/** Update the RoadwayForm with the current state of the roadway */
	protected void doUpdate() throws RemoteException {
		width = character.getWidth();
		byte[] bitmap = character.getBitmap();
		pixelPanel.removeAll();
		for( int b = 0, i = 0; b < bitmap.length; b++ ) {
			for(int bit = 128; bit > 0; bit >>= 1, i++)
				pixels[i].setSelected((bitmap[b] & bit) != 0);
		}
		for( int i = 0; i < height * width; i++ )
			pixelPanel.add( pixels[ i ] );
		setSize( getPreferredSize() );
	}

	/** Called when the "narrow" button is pressed */
	protected void narrowPressed() throws ChangeVetoException {
		if(width < 2)
			throw new ChangeVetoException("Minimum width");
		int max = height * width - 1;
		for( int i = max; i > max - height; i-- )
			pixelPanel.remove( i );
		grid.setColumns( --width );
		setSize( getPreferredSize() );
	}

	/** Called when the "wide" button is pressed */
	protected void widePressed() throws ChangeVetoException {
		if(width >= MAX_WIDTH)
			throw new ChangeVetoException("Maximum width");
		int max = height * width;
		grid.setColumns( ++width );
		for( int i = max; i < max + height; i++ )
			pixelPanel.add( pixels[ i ] );
		setSize( getPreferredSize() );
	}

	/** This is called when the 'apply' button is pressed */
	protected void applyPressed() throws TMSException, RemoteException {
		int size = ( height * width ) / 8;
		if((height * width) % 8 != 0)
			size++;
		byte[] bitmap = new byte[ size ];
		for( int b = 0, i = 0; b < bitmap.length; b++ ) {
			for( int bit = 128; bit > 0; bit >>= 1, i++ ) {
				if( pixels[ i ].isSelected() )
					bitmap[ b ] |= bit;
			}
		}
		character.setWidth( width );
		character.setBitmap( bitmap );
		character.notifyUpdate();
	}
}
