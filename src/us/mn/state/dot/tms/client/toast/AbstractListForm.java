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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.utils.ActionJob;
import us.mn.state.dot.tms.utils.ListSelectionJob;
import us.mn.state.dot.tms.utils.RemoteListModel;

/**
 * AbstractListForm is a swing-based widget for presenting a TextSearchList
 * along with 'add', 'edit' and 'delete' buttons for editing the list.
 *
 * @author Douglas Lau
 */
abstract public class AbstractListForm extends TMSObjectForm {

	/** Add button */
	protected final JButton add = new JButton( "Add" );

	/** Edit button */
	protected final JButton edit = new JButton( "Edit" );

	/** Delete button */
	protected final JButton delete = new JButton( "Delete" );

	/** Text search list */
	protected final TextSearchList list;

	/** Model which represents this list */
	protected final ListModel model;

	/** Icon */
	protected final ImageIcon icon;

	/** Create an AbstractListForm component */
	protected AbstractListForm(String t, TmsConnection tc,
		RemoteListModel l, ImageIcon i)
	{
		super(t, tc);
		model = l.getModel();
		list = new TextSearchList(model, getPrototypeCellValue());
		icon = i;
		obj = l.getList();
	}

	/** Initializze the widgets in the form */
	protected void initialize() throws RemoteException {
		list.registerKeyboardAction(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					edit.doClick();
				}
			}, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
			JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
		);
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2)
					edit.doClick();
			}
		});
		super.initialize();
	}

	/** Create the common list panel */
	protected JPanel createListPanel() {
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
		panel.setBorder( BORDER );
		Box vbox = Box.createVerticalBox();

		JLabel image = new JLabel( icon );
		vbox.add( image );
		vbox.add( Box.createVerticalStrut( 12 ) );

		if( admin ) {
			vbox.add( add );
			vbox.add( Box.createVerticalStrut( VGAP ) );
			new ActionJob( this, add ) {
				public void perform() throws Exception {
					addItem();
				}
			};
		}

		if(!admin)
			edit.setText("View");
		edit.setEnabled( false );
		vbox.add( edit );
		new ActionJob( this, edit ) {
			public void perform() throws Exception {
				try { editItem(); }
				finally {
					updateButtons();
				}
			}
		};

		if( admin ) {
			delete.setEnabled( false );
			vbox.add( Box.createVerticalStrut( VGAP ) );
			vbox.add( delete );
			new ActionJob( this, delete ) {
				public void perform() throws Exception {
					deleteItem();
				}
			};
		}
		panel.add( vbox );
		panel.add( Box.createHorizontalStrut( 8 ) );

		new ListSelectionJob(this, list.getList()) {
			public void perform() throws Exception {
				if(!event.getValueIsAdjusting())
					updateButtons();
			}
		};
		panel.add( list );
		return panel;
	}

	/** Get the currently selected item */
	protected String getSelectedItem() {
		int i = list.getList().getSelectedIndex();
		if(i >= 0)
			return (String)model.getElementAt(i);
		else
			return null;
	}

	/** Add an item */
	abstract protected void addItem() throws Exception;

	/** Edit an item */
	abstract protected void editItem() throws Exception;

	/** Delete an item */
	abstract protected void deleteItem() throws Exception;

	/** Get the prototype cell value */
	abstract protected String getPrototypeCellValue();

	/** Is the item deletable? */
	abstract protected boolean isDeletable(int index)
		throws TMSException, RemoteException;

	/** Update the buttons' enabled state */
	protected void updateButtons() throws Exception {
		add.setEnabled( true );
		if( list.isSelectionEmpty() ) {
			edit.setEnabled( false );
			delete.setEnabled( false );
			return;
		}
		edit.setEnabled( true );
		int i = list.getList().getSelectedIndex();
		delete.setEnabled(isDeletable(i));
	}
}
