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
package us.mn.state.dot.tms;

import java.util.Iterator;
import java.util.LinkedList;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * AbstractListImpl is a collection class which keeps track of a set of
 * Remote listeners which will be notified when an element is added,
 * removed, or changed in the list.
 *
 * @author Douglas Lau
 */
abstract class AbstractListImpl extends TMSObjectImpl implements AbstractList {

	/** ObjectVault table name */
	static public final String tableName = "abstract_list";

	/** Flag to indicate whether the list is stored in a vault */
	protected transient final boolean stored;
	
	/** Create a new abstract list */
	public AbstractListImpl(boolean s) throws RemoteException {
		super();
		stored = s;
	}

	/** Get the key */
	public String getKey() {
		return null;
	}

	/** Get an iterator of the elements in the list (for SubsetList) */
 	abstract Iterator<TMSObjectImpl> iterator();

	/** List of subscribed remote listeners of this list
	 * WARNING: access allowed only by the WORKER thread */
	private transient final LinkedList<Remote> subscribers =
		new LinkedList<Remote>();

	/** Subscribe a listener to this list */
	public Object[] subscribe(final RemoteList listener) {
		WORKER.addJob(new Scheduler.Job(0) {
			public void perform() {
				subscribers.add(listener);
			}
		});
		return null;
	}

	/** Unsubscribe a listener from this list */
	public final void unsubscribe(final RemoteList listener) {
		WORKER.addJob(new Scheduler.Job(0) {
			public void perform() {
				subscribers.remove(listener);
			}
		});
	}

	/** Notify all subscribed remote listeners that an element is being
	 * added to the list */
	protected void notifyAdd(final int index, final Object element) {
		scheduleNotify(subscribers, new Notifier() {
			public void notify(Remote r) throws RemoteException {
				((RemoteList)r).add(index, element);
			}
		});
	}

	/** Notify all subscribed remote listeners that an element is being
	 * removed from the list */
	protected void notifyRemove(final int index) {
		scheduleNotify(subscribers, new Notifier() {
			public void notify(Remote r) throws RemoteException {
				((RemoteList)r).remove(index);
			}
		});
	}

	/** Notify all subscribed remote listeners that an element is being
	 * set in the list */
	protected void notifySet(final int index, final Object element) {
		scheduleNotify(subscribers, new Notifier() {
			public void notify(Remote r) throws RemoteException {
				((RemoteList)r).set(index, element);
			}
		});
	}
}
