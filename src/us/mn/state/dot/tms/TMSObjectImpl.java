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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.ServerNotActiveException;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.vault.ObjectVault;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * The TMSObjectImpl class is an abstract class which is the base class
 * for all Traffic Management System objects.
 *
 * @author Douglas Lau
 */
abstract public class TMSObjectImpl extends UnicastRemoteObject
	implements TMSObject
{
	/** Worker thread */
	static protected final Scheduler WORKER =
		new Scheduler("Scheduler: WORKER");

	/** Unknown status string */
	static protected final String UNKNOWN = "???";

	/** General text validation regex pattern */
	static protected final Pattern TEXT_PATTERN =
		Pattern.compile("[[\\p{Graph}\\p{Blank}]&&[^'\\[\\]]]*");

	/** Object vault */
	static ObjectVault vault;

	/** SQL connection */
	static SQLConnection store;

	/** SONAR namespace */
	static ServerNamespace namespace;

	/** Corridor manager */
	static CorridorManager corridors;

	/** ObjectVault table name */
	static public final String tableName = "tms_object";

	/** Timing plan list */
	static TimingPlanListImpl planList;

	/** Ramp meter list */
	static RampMeterListImpl meterList;

	/** DMS list */
	static public DMSListImpl dmsList;

	/** LCS list */
	static LCSListImpl lcsList;

	/** Mapping of host names to user names */
	static private final HashMap<String, String> users =
		new HashMap<String, String>();

	/** Login a user */
	static void loginUser( String userName )
		throws ServerNotActiveException
	{
		String host = getClientHost();
		System.err.println("Logging in: " + userName + 
			" from " + host + ", " + new Date().toString());
		synchronized(users) {
			users.put(host, userName);
		}
	}

	/** Get the user name */
	static protected String getUserName() {
		try {
			String host = getClientHost();
			synchronized(users) {
				return users.get(host);
			}
		}
		catch( ServerNotActiveException e ) {
			return "unknown";
		}
	}

	/** Validate a string of text */
	static protected void validateText(String s) throws ChangeVetoException
	{
		Matcher m = TEXT_PATTERN.matcher(s);
		if(!m.matches()) throw
			new ChangeVetoException("Invalid text: " + s);
	}

	/** Create a new TMS object */
	public TMSObjectImpl() throws RemoteException {
		super();
	}

	/** Initialize the transient fields of the object */
	public void initTransients() throws TMSException, RemoteException,
		ObjectVaultException {}

	/** Is this object deletable? */
	public boolean isDeletable() throws TMSException {
		try { return vault.isDeletable( this ); }
		catch( ObjectVaultException e ) {
			throw new TMSException( e );
		}
	}

	/** List of observers of this TMS object
	 * WARNING: access allowed only by the WORKER thread */
	private transient final LinkedList<Remote> observers =
		new LinkedList<Remote>();

	/** Check if this object has any observers. This method may be called
	 * from any thread without being synchronized; LinkedList size is
	 * accessible atomically. */
	public boolean hasObserver() {
		return observers.size() > 0;
	}

	/** Add an observer */
	public final void addObserver(final RemoteObserver o) {
		WORKER.addJob(new Job() {
			public void perform() {
				observers.add(o);
			}
		});
	}

	/** Delete an observer */
	public final void deleteObserver(final RemoteObserver o) {
		WORKER.addJob(new Job() {
			public void perform() {
				observers.remove(o);
			}
		});
	}

	/** Abstract notifier class for remote observers */
	abstract protected class Notifier {
		abstract public void notify(Remote r) throws RemoteException;
		public void cleanup() throws RemoteException {}
	}

	/** Notification debug log */
	static protected final DebugLog NOTIFY = new DebugLog("notify");

	/** Actually do the notification */
	protected void doNotify(LinkedList<Remote> o_list, Notifier n)
		throws RemoteException
	{
		long n_start = System.currentTimeMillis();
		int clients = 0;
		Iterator<Remote> it = o_list.iterator();
		while(it.hasNext()) {
			long start = System.currentTimeMillis();
			Remote r = it.next();
			String end = r.toString();
			int e1 = end.indexOf("endpoint:[");
			if(e1 > 0) {
				e1 += 10;
				int e2 = end.indexOf("]", e1);
				if(e2 > 0) end = end.substring(e1, e2);
			}
			try {
				n.notify(r);
				clients++;
			}
			catch(RemoteException e) {
				it.remove();
				String m = e.getMessage();
				if(e.detail != null) m = e.detail.getMessage();
				NOTIFY.log("Disconnected " + end + ", " + m);
			}
			catch(Exception e) {
				it.remove();
				System.err.println("Notifier exception @ " +
					new Date() + "\n" + e);
			}
			finally {
				long el = System.currentTimeMillis() - start;
				NOTIFY.log("  -> " + end + ", " + el + " ms");
			}
		}
		if(clients > 0) {
			String c = getClass().getName();
			int c1 = c.lastIndexOf(".");
			if(c1 > 0)
				c = c.substring(c1 + 1);
			c += ": " + getKey();
			long el = System.currentTimeMillis() - n_start;
			NOTIFY.log("Notified " + clients + " (" + c + "), " +
				el + " ms");
		}
		n.cleanup();
	}

	/** Schedule all observers of a TMS object event to be notified */
	protected void scheduleNotify(final LinkedList<Remote> o_list,
		final Notifier n)
	{
		WORKER.addJob(new Job() {
			public void perform() throws RemoteException {
				doNotify(o_list, n);
			}
		});
	}

	/** Notify all observers for an update */
	public void notifyUpdate() {
		scheduleNotify(observers, new Notifier() {
			public void notify(Remote r) throws RemoteException {
				((RemoteObserver)r).update();
			}
		});
	}

	/** Notify all observers for a status change */
	public void notifyStatus() {
		scheduleNotify(observers, new Notifier() {
			public void notify(Remote r) throws RemoteException {
				((RemoteObserver)r).status();
			}
		});
	}

	/** Notify all observers that this object is being deleted */
	protected final void notifyDelete() {
		final UnicastRemoteObject uro = this;
		scheduleNotify(observers, new Notifier() {
			public void notify(Remote r) throws RemoteException {
				((RemoteObserver)r).delete();
			}
			public void cleanup() throws RemoteException {
				observers.clear();
				UnicastRemoteObject.unexportObject(uro, true);
			}
		});
	}

	/** Get the object ID */
	public Integer getOID() {
		return vault.getOID(this);
	}

	/** Get the primary key name */
	public String getKeyName() {
		return "vault_oid";
	}

	/** Get the object key */
	public String getKey() {
		return getOID().toString();
	}

	/** Lookup a geo location in the SONAR namespace */
	static protected GeoLocImpl lookupGeoLoc(String l) {
		if(l != null) {
			return (GeoLocImpl)namespace.lookupObject(
				GeoLoc.SONAR_TYPE, l);
		} else
			return null;
	}

	/** Lookup the named system policy */
	static protected SystemPolicyImpl lookupPolicy(final String p) {
		return (SystemPolicyImpl)namespace.findObject(
			SystemPolicy.SONAR_TYPE, new Checker<SystemPolicyImpl>()
		{
			public boolean check(SystemPolicyImpl sp) {
				return p.equals(sp.getName());
			}
		});
	}

	/** Get the value of a system policy */
	static public int getPolicyValue(String p) {
		SystemPolicyImpl sp = lookupPolicy(p);
		if(sp != null)
			return sp.getValue();
		else
			return 0;
	}

	/** Lookup a holiday which matches the given calendar */
	static protected HolidayImpl lookupHoliday(final Calendar stamp) {
		return (HolidayImpl)namespace.findObject(Holiday.SONAR_TYPE,
			new Checker<HolidayImpl>()
		{
			public boolean check(HolidayImpl h) {
				return h.matches(stamp);
			}
		});
	}

	/** Check if the given date/time matches any holiday */
	static public boolean isHoliday(Calendar stamp) {
		return lookupHoliday(stamp) != null;
	}

	/** Lookup a camera in the SONAR namespace */
	static public CameraImpl lookupCamera(String name) {
		return (CameraImpl)namespace.lookupObject(Camera.SONAR_TYPE,
			name);
	}

	/** Lookup a controller in the SONAR namespace */
	static protected ControllerImpl lookupController(final String c) {
		if(c == null || c.equals(""))
			return null;
		return (ControllerImpl)namespace.lookupObject(
			Controller.SONAR_TYPE, c);
	}
}
