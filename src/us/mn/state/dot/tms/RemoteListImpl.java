/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.rmi.RemoteException;
import us.mn.state.dot.sched.Job;

/**
 * The RemoteListImpl class keeps track of a list of remote objects
 * which are taken from the IRIS Traffic Management System RMI server.
 *
 * @author Douglas Lau
 */
abstract public class RemoteListImpl extends RemoteObserverImpl
	implements RemoteList
{
	/** RMI Interface to the remote list */
	protected AbstractList list;

	/** Get the remote list RMI interface */
	public AbstractList getList() {
		return list;
	}

	/** Create a new remote list */
	public RemoteListImpl(AbstractList l) throws RemoteException {
		super();
		list = l;
	}

	/** Initialize the remote list. This method must be called to
	 * initialize the state of any subclass RemoteListImpl. Java's
	 * draconian superclass constructor rules prevent this from working
	 * in the constructor. */
	public final void initialize() {
		final RemoteListImpl rli = this;
		RWORKER.addJob(new Job() {
			public void perform() throws RemoteException {
				Object[] elem = list.subscribe(rli);
				if(elem != null) {
					for(int i = 0; i < elem.length; i++)
						doAdd(i, elem[i]);
				}
			}
		});
	}

	/** Dispose of the remote list */
	public void dispose() {
		if(list != null) {
			try { list.unsubscribe(this); }
			catch(RemoteException e) {
				System.err.println("ERROR: RemoteListImpl." +
					"dispose() " + e.getMessage());
			}
			list = null;
		}
		super.dispose();
	}

	/** Add an element to the list
	 * - called by the RMI server (do not use this method)
	 */
	public final void add(final int index, final Object element) {
		RWORKER.addJob(new Job() {
			public void perform() throws Exception {
				doAdd(index, element);
			}
		});
	}

	/** Do the actual add operation */
	abstract protected void doAdd(int index, Object element)
		throws RemoteException;

	/** Remove an element from the list
	 * - called by the RMI server (do not use this method)
	 */
	public final void remove(final int index) {
		RWORKER.addJob(new Job() {
			public void perform() throws Exception {
				doRemove(index);
			}
		});
	}

	/** Do the actual remove operation */
	abstract protected Object doRemove(int index) throws RemoteException;

	/** Set an element in the list
	 * - called by the RMI server (do not use this method)
	 */
	public final void set(final int index, final Object element) {
		RWORKER.addJob(new Job() {
			public void perform() throws Exception {
				doSet(index, element);
			}
		});
	}

	/** Do the actual set operation */
	abstract protected void doSet(int index, Object element)
		throws RemoteException;
}
