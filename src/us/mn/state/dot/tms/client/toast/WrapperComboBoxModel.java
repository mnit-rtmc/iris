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

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 * WrapperComboBoxModel is a ComboBoxModel which can be used as a wrapper for
 * any ListModel.  It is useful for making multiple JComboBoxes which have
 * the same underlying ListModel, but have a different "selection".
 *
 * @author Douglas Lau
 */
public class WrapperComboBoxModel extends AbstractListModel
	implements ComboBoxModel
{

	/** Underlying list model */
	private final ListModel model;

	/** Create a new WrapperComboBoxModel */
	public WrapperComboBoxModel( ListModel m ) {
		model = m;
	}

	/** Add a list data listener */
	public void addListDataListener( ListDataListener l ) {
		model.addListDataListener( l );
		super.addListDataListener( l );
	}

	/** Remove a list data listener */
	public void removeListDataListener( ListDataListener l ) {
		model.removeListDataListener( l );
		super.removeListDataListener( l );
	}

	/** Blank entry in combo box */
	static public final String BLANK = " ";

	/** Get an element from the list model */
	public Object getElementAt( int index ) {
		try {
			synchronized( model ) {
				if( extra != null ) {
					if( index == 0 ) return extra;
					index--;
				}
				if( index == 0 ) return BLANK;
				return model.getElementAt( index - 1 );
			}
		}
		catch( ArrayIndexOutOfBoundsException e ) {
			return null;
		}
	}

	/** Get the size of the model */
	public int getSize() {
		synchronized( model ) {
			if( extra != null ) return model.getSize() + 2;
			return model.getSize() + 1;
		}
	}

	/** Selected item in the list */
	private Object selected = null;

	/** Extra item is an item which is not in the underlying list model,
		but was selected with setSelectedItem() */
	private Object extra = null;

	/** Set the selected item */
	public void setSelectedItem( Object s ) {
		synchronized( model ) {
			boolean isExtra = true;
			if( BLANK.equals( s ) ) {
				s = null;
				isExtra = false;
			}
			else {
				int c = model.getSize();
				for( int i = 0; i < c; i++ ) {
					Object o = model.getElementAt( i );
					if( o.equals( s ) ) {
						isExtra = false;
						break;
					}
				}
			}
			if( isExtra ) extra = s;
			selected = s;
		}
		fireContentsChanged( this, -1, -1 );
	}

	/** Get the selected item */
	public Object getSelectedItem() { return selected; }
}
