/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.utils;

import java.rmi.RemoteException;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import us.mn.state.dot.tms.AbstractList;

/**
 * The RemoteListModel class keeps track of a list of Objects (ListModel)
 * which are taken from the IRIS Traffic Management System RMI server.
 *
 * @author Douglas Lau
 */
public class RemoteListModel extends RemoteListAdapter {

	/** Swing list model */
	protected final DefaultListModel model = new DefaultListModel();

	/**
	 * Get a list model for this list which is automatically updated
	 * whenever any items are added, removed or changed in the list.
	 * This model should be considered read-only, as any attempt to
	 * manually change the contents of the model will totally botch
	 * this class.
	 */
	public ListModel getModel() {
		return model;
	}

	/** Create a new remote list model */
	public RemoteListModel(AbstractList l, boolean i)
		throws RemoteException
	{
		super(l);
		if(i)
			initialize();
	}

	/** Create a new remote list model */
	public RemoteListModel(AbstractList l) throws RemoteException {
		this(l, true);
	}

	/** Delete the remote list */
	protected void doDelete() {
		System.err.println("ERROR: Deleting remote list");
	}

	/** Add an element to the list model */
	protected void doAdd(int index, Object element) throws RemoteException {
		model.add(index, element);
	}

	/** Remove an element from the list model */
	protected Object doRemove(int index) throws RemoteException {
		return model.remove(index);
	}

	/** Set the contents of an element in the list model */
	protected void doSet(int index, Object element) throws RemoteException {
		model.set(index, element);
	}
}
