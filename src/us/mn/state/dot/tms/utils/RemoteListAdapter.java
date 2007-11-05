/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.AbstractList;
import us.mn.state.dot.tms.RemoteListImpl;
import us.mn.state.dot.tms.Scheduler;

/**
 * RemoteListAdapter can be subclassed without specifying all abstract methods
 *
 * @author Douglas Lau
 */
public class RemoteListAdapter extends RemoteListImpl {

	/** Exception handler for scheduler worker */
	static public final Scheduler.ExceptionHandler HANDLER =
		new Scheduler.ExceptionHandler()
	{
		public void handleException(Exception e) {
			new ExceptionDialog(e).setVisible(true);
		}
	};

	/** Set the exception handler for the worker */
	static {
		RWORKER.setHandler(HANDLER);
	}

	/** Create a new remote list adapter */
	public RemoteListAdapter(AbstractList l) throws RemoteException {
		super(l);
	}

	/** Update the remote list */
	protected void doUpdate() {}

	/** Update the status of the remote list */
	protected void doStatus() throws RemoteException {}

	/** Delete the remote list */
	protected void doDelete() {}

	/** Add an element to the list */
	protected void doAdd(int index, Object element) throws RemoteException {
	}

	/** Remove an element from the list */
	protected Object doRemove(int index) throws RemoteException {
		return null;
	}

	/** Set the contents of an element in the list */
	protected void doSet(int index, Object element) throws RemoteException {
	}
}
