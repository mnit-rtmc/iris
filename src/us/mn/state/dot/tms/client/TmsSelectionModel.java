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
package us.mn.state.dot.tms.client;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;

import us.mn.state.dot.tms.RemoteObserverImpl;
import us.mn.state.dot.tms.client.proxy.TmsMapProxy;
import us.mn.state.dot.tms.utils.AbstractJob;

/**
 * Keeps track of selected TMS object.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class TmsSelectionModel {

	/** Run something on the Swing thread */
	static protected void runSwing(Runnable r) {
		if(SwingUtilities.isEventDispatchThread())
			r.run();
		else
			SwingUtilities.invokeLater(r);
	}

	/** Currently selected object */
	protected TmsMapProxy selected;

	/** Remote observer instance */
	protected RemoteObserverImpl observer = null;

	/** The listeners of this model */
	protected final List<TmsSelectionListener> listeners =
		new ArrayList<TmsSelectionListener>();

	/** Set the selected object (must be called from the Swing thread) */
	protected void _setSelected(final TmsMapProxy proxy) {
		final TmsMapProxy sel = selected;
		new AbstractJob() {
			public void perform() throws RemoteException {
				stopObserving(sel);
				startObserving(proxy);
			}
		}.addToScheduler();
		selected = proxy;
		fireSelectionChanged(new TmsSelectionEvent(this, proxy));
	}

	/** Set the selected TMS object */
	public void setSelected(final TmsMapProxy proxy) {
		if(((selected != null) && (!selected.equals(proxy))) ||
			((selected == null) && (proxy != null)))
		{
			runSwing(new Runnable() {
				public void run() {
					_setSelected(proxy);
				}
			});
		}
	}

	/** Start observing a newly selected object */
	protected void startObserving(TmsMapProxy obj)
		throws RemoteException
	{
		if(obj == null)
			return;
		observer = new SelectionObserver();
		obj.addObserver(observer);
	}

	/** Stop observing the currently selected object */
	protected void stopObserving(TmsMapProxy obj)
		throws RemoteException
	{
		if(obj == null || observer == null)
			return;
		try { obj.deleteObserver(observer); }
		catch(NoSuchObjectException e) {
			// Do Nothing
		}
		observer.dispose();
		observer = null;
	}

	/** Get the selected TMS object */
	public TmsMapProxy getSelected() {
		return selected;
	}

	/** Add a TmsSelectionListener to the model */
	public void addTmsSelectionListener(TmsSelectionListener l) {
		listeners.add(l);
	}

	/** Remove a TmsSelectionListener from the model */
	public void removeTmsSelectionListener(TmsSelectionListener l) {
		listeners.remove(l);
	}

	/** Dispose of the TMS selection model */
	public void dispose() {
		listeners.clear();
		setSelected(null);
	}

	/** Fire a selection changed event to all listeners */
	protected void fireSelectionChanged(TmsSelectionEvent e) {
		Iterator<TmsSelectionListener> i = listeners.iterator();
		while(i.hasNext()) {
			TmsSelectionListener l = i.next();
			l.selectionChanged(e);
		}
	}

	/** Fire a status refresh event to all listeners */
	protected void fireStatusRefresh() {
		Iterator<TmsSelectionListener> i = listeners.iterator();
		while(i.hasNext()) {
			TmsSelectionListener l = i.next();
			l.refreshStatus();
		}
	}

	/** Fire an update refresh event to all listeners */
	protected void fireUpdateRefresh() {
		Iterator<TmsSelectionListener> i = listeners.iterator();
		while(i.hasNext()) {
			TmsSelectionListener l = i.next();
			l.refreshUpdate();
		}
	}

	/**
	 * Listens to the remote TraffiDevice object and notifies selection
	 * listeners if the object changes.
	 */
	protected class SelectionObserver extends RemoteObserverImpl {

		/** Create a new selection observer */
		protected SelectionObserver() throws RemoteException {
			super();
		}

		/** Called by server when the selected TMS object status needs
		 * to be updated */
		protected void doStatus() throws RemoteException {
			TmsMapProxy o = selected;
			if(o != null)
				o.updateStatusInfo();
			runSwing(new Runnable() {
				public void run() {
					fireStatusRefresh();
				}
			});
		}

		/** Called by server when selected TMS object is deleted */
		protected void doDelete() {
			setSelected(null);
		}

		/** Called by server when the selected TMS object configuration
		 * needs to be updated */
		protected void doUpdate() throws RemoteException {
			TmsMapProxy o = selected;
			if(o != null)
				o.updateUpdateInfo();
			runSwing(new Runnable() {
				public void run() {
					fireUpdateRefresh();
				}
			});
		}
	}
}
