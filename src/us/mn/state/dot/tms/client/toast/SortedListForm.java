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

import java.rmi.RemoteException;
import javax.swing.ImageIcon;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.TMSObject;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.utils.RemoteListModel;

/**
 * SortedListForm
 *
 * @author Douglas Lau
 */
abstract public class SortedListForm extends AbstractListForm {

	/** Sorted list interface from RMI server */
	protected final SortedList sList;

	/** Create a new SortedListForm */
	protected SortedListForm(String t, TmsConnection tc, RemoteListModel l,
		ImageIcon i)
	{
		super(t, tc, l, i);
		sList = (SortedList)l.getList();
	}

	/** Delete an item from the list */
	protected void deleteItem() throws Exception {
		try {
			String name = getSelectedItem();
			if(name != null)
				sList.remove(name);
		}
		finally {
			updateButtons();
		}
	}

	/** Determine if a particular item is deletable */
	protected boolean isDeletable(int index) throws TMSException,
		RemoteException
	{
		if(index < 0)
			return false;
		String name = (String)model.getElementAt( index );
		TMSObject item = (TMSObject)sList.getElement( name );
		if(item != null)
			return item.isDeletable();
		else
			return false;
	}
}
