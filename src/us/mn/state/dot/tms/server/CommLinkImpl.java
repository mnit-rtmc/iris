/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
 * Copyright (C) 2011  Berkeley Transportation Systems Inc.
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
package us.mn.state.dot.tms.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.comm.MessagePoller;

/**
 * The CommLinkImpl class represents a single communication link which is
 * connected with one or more field device controllers.
 *
 * @see us.mn.state.dot.tms.CommProtocol
 * @author Douglas Lau
 * @author Michael Darter
 */
public class CommLinkImpl extends BaseObjectImpl implements CommLink {

	/** Load all the comm links */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading comm links...");
		namespace.registerType(SONAR_TYPE, CommLinkImpl.class);
		store.query("SELECT name, description, uri, protocol, " +
			"timeout FROM iris." + SONAR_TYPE  + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new CommLinkImpl(
					row.getString(1),	// name
					row.getString(2),	// description
					row.getString(3),	// uri
					row.getShort(4),	// protocol
					row.getInt(5)		// timeout
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("description", description);
		map.put("uri", uri);
		map.put("protocol", (short)protocol.ordinal());
		map.put("timeout", timeout);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new comm link */
	public CommLinkImpl(String n) {
		super(n);
	}

	/** Create a new comm link */
	public CommLinkImpl(String n, String d, String u, short p, int t) {
		super(n);
		description = d;
		uri = u;
		CommProtocol cp = CommProtocol.fromOrdinal(p);
		if(cp != null)
			protocol = cp;
		timeout = t;
		poller = null;
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		closePoller();
		super.doDestroy();
	}

	/** Description of communication link */
	protected String description = "<New Link>";

	/** Set text description */
	public void setDescription(String d) {
		description = d;
	}

	/** Set text description */
	public void doSetDescription(String d) throws TMSException {
		if(d.equals(description))
			return;
		store.update(this, "description", d);
		setDescription(d);
	}

	/** Get text description */
	public String getDescription() {
		return description;
	}

	/** Remote URI for link */
	protected String uri = "";

	/** Set remote URI for link */
	public void setUri(String u) {
		uri = u;
	}

	/** Set remote URI for link */
	public void doSetUri(String u) throws TMSException {
		if(u.equals(uri))
			return;
		store.update(this, "uri", u);
		setUri(u);
		closePoller();
	}

	/** Get remote URI for link */
	public String getUri() {
		return uri;
	}

	/** Communication protocol */
	protected CommProtocol protocol = CommProtocol.NTCIP_C;

	/** Set the communication protocol */
	public void setProtocol(short p) {
		CommProtocol cp = CommProtocol.fromOrdinal(p);
		if(cp != null)
			protocol = cp;
	}

	/** Set the communication protocol */
	public void doSetProtocol(short p) throws TMSException {
		CommProtocol cp = CommProtocol.fromOrdinal(p);
		if(cp == null)
			throw new ChangeVetoException("Invalid protocol: " + p);
		if(cp == protocol)
			return;
		store.update(this, "protocol", p);
		setProtocol(p);
		closePoller();
	}

	/** Get the communication protocol */
	public short getProtocol() {
		return (short)protocol.ordinal();
	}

	/** Polling timeout (milliseconds) */
	protected int timeout = 750;

	/** Set the polling timeout (milliseconds) */
	public void setTimeout(int t) {
		timeout = t;
	}

	/** Set the polling timeout (milliseconds) */
	public void doSetTimeout(int t) throws TMSException {
		if(t == timeout)
			return;
		try {
			MessagePoller p = poller;
			if(p != null)
				p.setTimeout(t);
		}
		catch(IOException e) {
			throw new TMSException(e);
		}
		store.update(this, "timeout", t);
		setTimeout(t);
	}

	/** Get the polling timeout (milliseconds) */
	public int getTimeout() {
		return timeout;
	}

	/** Message poller for communication */
	protected transient MessagePoller poller;

	/** Get the message poller.  This must be synchronized to protect
	 * access to the poller member variable.  Only call this method when
	 * an operation needs to be queued, since a modem may be acquired to
	 * create the poller.  */
	public synchronized MessagePoller getPoller() {
		if(poller != null) {
			setStatus(poller.getStatus());
			if(poller.isAlive())
				return poller;
			else
				closePoller();
		}
		return openPoller();
	}

	/** Open the message poller.  Poller must be null prior to calling. */
	protected synchronized MessagePoller openPoller() {
		assert poller == null;
		try {
			poller = MessagePoller.create(name, protocol, uri);
			poller.setTimeout(timeout);
			poller.start();
		}
		catch(IOException e) {
			closePoller();
			setStatus("I/O error: " + e.getMessage());
		}
		return poller;
	}

	/** Close the message poller */
	protected synchronized void closePoller() {
		if(poller != null && !poller.wasHungUp()) {
			failControllers();
			poller.stopPolling();
		}
		poller = null;
	}

	/** Communication link status */
	protected transient String status = Constants.UNKNOWN;

	/** Set the communication status */
	public void setStatus(String s) {
		if(s == null || s.equals(status))
			return;
		status = s;
		notifyAttribute("status");
	}

	/** Get the communication status */
	public String getStatus() {
		return status;
	}

	/** Field device controllers */
	protected transient final TreeMap<Integer, ControllerImpl> controllers =
		new TreeMap<Integer, ControllerImpl>();

	/** Put a controller on the link */
	public void putController(int d, ControllerImpl c)
		throws ChangeVetoException
	{
		synchronized(controllers) {
			if(controllers.containsKey(d)) {
				throw new ChangeVetoException("Drop " + d +
					" exists");
			}
			controllers.put(d, c);
		}
	}

	/** Pull a controller from the link */
	public void pullController(ControllerImpl c) {
		Integer d = new Integer(c.getDrop());
		synchronized(controllers) {
			controllers.remove(d);
		}
	}

	/** Get a controller by drop */
	public Controller getController(short drop) {
		Integer d = new Integer(drop);
		synchronized(controllers) {
			return controllers.get(d);
		}
	}

	/** Get the controllers defined for this communication link */
	public Controller[] getControllers() {
		synchronized(controllers) {
			return (Controller [])controllers.values().toArray(
				new Controller[0]);
		}
	}

	/** Find the controller */
	public ControllerImpl findController(Controller c) {
		synchronized(controllers) {
			for(ControllerImpl cont: controllers.values()) {
				if(cont.equals(c))
					return cont;
			}
			return null;
		}
	}

	/** Set all controllers to a failed status */
	protected void failControllers() {
		synchronized(controllers) {
			for(ControllerImpl c: controllers.values()) {
				c.setFailed(true);
			}
		}
	}

	/** Line load */
	protected transient float load;

	/** Get the current link load */
	public float getLoad() {
		return load;
	}

	/** Check if a modem is required for the link */
	public boolean isModemLink() {
		return uri.startsWith("modem:");
	}

	/** Check if the comm link is currently connected */
	public boolean isConnected() {
		MessagePoller p = poller;
		return p != null && p.isAlive();
	}

	/** Print the comm link as an XML element */
	public void printXml(PrintWriter out) {
		out.print("<commlink");
		out.print(XmlWriter.createAttribute("name", getName()));
		out.print(XmlWriter.createAttribute("description", getDescription()));
		String p = CommProtocol.fromOrdinal(getProtocol()).toString();
		out.print(XmlWriter.createAttribute("protocol", p));
		out.println("/>");
	}
}
