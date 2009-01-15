/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import us.mn.state.dot.tms.RemoteObserverImpl;
import us.mn.state.dot.tms.TMSObject;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.utils.TMSProxy;

/**
 * TMSObjectForm is an abstract Swing dialog for all TMSObject forms
 *
 * @author Douglas Lau
 */
abstract public class TMSObjectForm extends AbstractForm {

	/** TMS connection */
	protected final TmsConnection connection;

	/** TMS proxy object */
	protected final TMSProxy tms;

	/** Remote TMS object */
	protected TMSObject obj;

	/** Remote object observer */
	protected RemoteObserverImpl observer;

	/** Administrator privilege flag */
	protected final boolean admin;

	/** Create a new TMSObjectForm */
	protected TMSObjectForm(String t, TmsConnection tc) {
		super(t);
		connection = tc;
		tms = connection.getProxy();
		admin = connection.isAdmin();
	}

	/** Get the TMS connection */
	public TmsConnection getConnection() {
		return connection;
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		try {
			startObserving(obj);
		}
		catch(RemoteException e) {
			e.printStackTrace();
		}
	}

	/** Update the form with the current state of the object */
	protected void doUpdate() throws RemoteException {}

	/** Refresh the status of the object */
	protected void doStatus() throws RemoteException {}

	/** Dispose of the form */
	protected void dispose() {
		stopObserving(obj, observer);
		obj = null;
		observer = null;
	}

	/** Tell the server we want to start observing an object */
	protected void startObserving(final TMSObject obj)
		throws RemoteException
	{
		if(obj == null)
			return;
		final TMSObjectForm _this = this;
		observer = new RemoteObserverImpl() {
			protected void doStatus() throws RemoteException {
				_this.doStatus();
			}
			protected void doUpdate() throws RemoteException {
				_this.doUpdate();
			}
			protected void doDelete() {
				_this.doDelete();
			}
		};
		obj.addObserver(observer);
	}

	/** Tell the server we want to stop observing an object */
	protected void stopObserving(final TMSObject obj,
		final RemoteObserverImpl observer)
	{
		if(observer == null)
			return;
		try {
			obj.deleteObserver(observer);
		}
		catch(NoSuchObjectException e) {
			// Ignore
		}
		catch(RemoteException e) {
			// Ignore
		}
		observer.dispose();
	}

	/** Called when the object is being deleted */
	protected void doDelete() {
		close();
	}
}
