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
import java.rmi.NoSuchObjectException;
import java.rmi.server.UnicastRemoteObject;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;

/**
 * RemoteObserverImpl is a simple implementation of the RemoteObserver
 * interface.
 *
 * @author Douglas Lau
 */
abstract public class RemoteObserverImpl extends UnicastRemoteObject
	implements RemoteObserver
{
	/** Worker thread */
	static protected final Scheduler RWORKER = new Scheduler(
		"Remote Worker");

	/** Flag if object is exported for RMI */
	protected boolean rmi_exported;

	/** Create a new remote observer */
	public RemoteObserverImpl() throws RemoteException {
		super();
		rmi_exported = true;
	}

	/** Update the observed object */
	public final void update() {
		RWORKER.addJob(new Job() {
			public void perform() throws Exception {
				doUpdate();
			}
		});
	}

	/** Do the actual update operation */
	abstract protected void doUpdate() throws RemoteException;

	/** Status change for the observed object */
	public final void status() {
		RWORKER.addJob(new Job() {
			public void perform() throws Exception {
				doStatus();
			}
		});
	}

	/** Do the actual status operation */
	abstract protected void doStatus() throws RemoteException;

	/** Delete the observed object */
	public final void delete() {
		RWORKER.addJob(new Job() {
			public void perform() throws Exception {
				doDelete();
			}
		});
	}

	/** Do the actual delete operation */
	abstract protected void doDelete();

	/** Dispose of the observer */
	public void dispose() {
		try {
			if(rmi_exported)
				rmi_exported = !unexportObject(this, true);
		}
		catch(NoSuchObjectException e) {
			System.err.println("ERROR: RemoteObserverImpl." +
				"dispose() " + e.getMessage());
			System.err.println("\tClass: " + getClass().getName());
			rmi_exported = false;
		}
	}

	/** Finalize the remote observer */
	protected void finalize() {
		dispose();
	}
}
