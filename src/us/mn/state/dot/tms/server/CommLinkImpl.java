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
package us.mn.state.dot.tms.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Constants;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.comm.DatagramMessenger;
import us.mn.state.dot.tms.server.comm.HttpFileMessenger;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ProtocolException;
import us.mn.state.dot.tms.server.comm.SocketMessenger;
import us.mn.state.dot.tms.server.comm.canoga.CanogaPoller;
import us.mn.state.dot.tms.server.comm.aws.AwsPoller;
import us.mn.state.dot.tms.server.comm.dmslite.DmsLitePoller;
import us.mn.state.dot.tms.server.comm.manchester.ManchesterPoller;
import us.mn.state.dot.tms.server.comm.mndot.MndotPoller;
import us.mn.state.dot.tms.server.comm.ntcip.HDLCMessenger;
import us.mn.state.dot.tms.server.comm.ntcip.NtcipPoller;
import us.mn.state.dot.tms.server.comm.pelco.PelcoPoller;
import us.mn.state.dot.tms.server.comm.pelcod.PelcoDPoller;
import us.mn.state.dot.tms.server.comm.smartsensor.SmartSensorPoller;
import us.mn.state.dot.tms.server.comm.vicon.ViconPoller;
import us.mn.state.dot.tms.server.comm.viconptz.ViconPTZPoller;

/**
 * The CommLinkImpl class represents a single communication link which is
 * connected with one or more field device controllers. Many different
 * protocols are supported, including Mn/DOT's 4- and 5-bit 170 protocols,
 * NTCIP class B, Wavetronix SmartSensor, and 3M Canoga.
 *
 * @author Douglas Lau
 */
public class CommLinkImpl extends BaseObjectImpl implements CommLink {

	/** Create an inet socket address */
	static protected InetSocketAddress createSocketAddress(String url)
		throws IOException
	{
		String[] s = url.split(":");
		if(s.length != 2)
			throw new IOException("INVALID SOCKET ADDRESS");
		int p = parsePort(s[1]);
		return new InetSocketAddress(s[0], p);
	}

	/** Parse the port number */
	static protected int parsePort(String p) throws IOException {
		try {
			int i = Integer.parseInt(p);
			if(i >= 0 && i <= 65535)
				return i;
		}
		catch(NumberFormatException e) {
			// Fall out
		}
		throw new IOException("INVALID PORT: " + p);
	}

	/** Load all the comm links */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading comm links...");
		namespace.registerType(SONAR_TYPE, CommLinkImpl.class);
		store.query("SELECT name, description, url, protocol, " +
			"timeout FROM " + SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new CommLinkImpl(
					row.getString(1),	// name
					row.getString(2),	// description
					row.getString(3),	// url
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
		map.put("url", url);
		map.put("protocol", protocol);
		map.put("timeout", timeout);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return SONAR_TYPE;
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
		url = u;
		protocol = p;
		timeout = t;
		openPoller();
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

	/** Remote URL for link */
	protected String url = "";

	/** Set remote URL for link */
	public void setUrl(String u) {
		url = u;
	}

	/** Set remote URL for link */
	public void doSetUrl(String u) throws TMSException {
		if(u.equals(url))
			return;
		store.update(this, "url", u);
		setUrl(u);
		openPoller();
	}

	/** Get remote URL for link */
	public String getUrl() {
		return url;
	}

	/** Communication protocol */
	protected short protocol = PROTO_NTCIP_C;

	/** Set the communication protocol */
	public void setProtocol(short p) {
		protocol = p;
	}

	/** Set the communication protocol */
	public void doSetProtocol(short p) throws TMSException {
		if(p == protocol)
			return;
		if(p < 0 || p >= PROTOCOLS.length)
			throw new ChangeVetoException("Invalid protocol: " + p);
		store.update(this, "protocol", p);
		setProtocol(p);
		openPoller();
	}

	/** Get the communication protocol */
	public short getProtocol() {
		return protocol;
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
	 * access to the poller member variable. */
	public synchronized MessagePoller getPoller() {
		if(poller != null) {
			setStatus(poller.getStatus());
			if(poller.isAlive())
				return poller;
		}
		failControllers();
		return openPoller();
	}

	/** Create a socket messenger */
	protected Messenger createSocketMessenger() throws IOException {
		return new SocketMessenger(createSocketAddress(url));
	}

	/** Create a datagram messenger */
	protected Messenger createDatagramMessenger() throws IOException {
		return new DatagramMessenger(createSocketAddress(url));
	}

	/** Create an http file messenger */
	protected Messenger createHttpFileMessenger() throws IOException {
		return new HttpFileMessenger(new URL(url));
	}

	/** Create an NTCIP Class A poller */
	protected MessagePoller createNtcipAPoller() throws IOException {
		return new NtcipPoller(name, createDatagramMessenger());
	}

	/** Create an NTCIP Class B poller */
	protected MessagePoller createNtcipBPoller() throws IOException {
		HDLCMessenger hdlc = new HDLCMessenger(createSocketMessenger());
		return new NtcipPoller(name, hdlc);
	}

	/** Create an NTCIP Class C poller */
	protected MessagePoller createNtcipCPoller() throws IOException {
		return new NtcipPoller(name, createSocketMessenger());
	}

	/** Create a Mn/DOT poller */
	protected MessagePoller createMndotPoller() throws IOException {
		return new MndotPoller(name, createSocketMessenger(),
			protocol);
	}

	/** Create a SmartSensor poller */
	protected MessagePoller createSmartSensorPoller() throws IOException {
		return new SmartSensorPoller(name, createSocketMessenger());
	}

	/** Create a Canoga poller */
	protected MessagePoller createCanogaPoller() throws IOException {
		return new CanogaPoller(name, createSocketMessenger());
	}

	/** Create a Vicon poller */
	protected MessagePoller createViconPoller() throws IOException {
		return new ViconPoller(name, createSocketMessenger());
	}

	/** Create a Vicon PTZ poller */
	protected MessagePoller createViconPTZPoller() throws IOException {
		return new ViconPTZPoller(name, createDatagramMessenger());
	}

	/** Create a PelcoD poller */
	protected MessagePoller createPelcoDPoller() throws IOException {
		return new PelcoDPoller(name, createSocketMessenger());
	}

	/** Create a Manchester poller */
	protected MessagePoller createManchesterPoller() throws IOException {
		return new ManchesterPoller(name, createSocketMessenger());
	}

	/** Create a DMS Lite poller */
	protected MessagePoller createDmsLitePoller() throws IOException {
		return new DmsLitePoller(name, createSocketMessenger());
	}

	/** Create a AWS poller */
	protected MessagePoller createAwsPoller() throws IOException {
		return new AwsPoller(name, createHttpFileMessenger(), 
			namespace);
	}

	/** Create a Pelco video switch poller */
	protected MessagePoller createPelcoPoller() throws IOException {
		return new PelcoPoller(name, createSocketMessenger());
	}

	/** Try to open the communication link */
	protected MessagePoller createPoller() throws IOException {
		switch(protocol) {
			case PROTO_NTCIP_A:
				return createNtcipAPoller();
			case PROTO_NTCIP_B:
				return createNtcipBPoller();
			case PROTO_NTCIP_C:
				return createNtcipCPoller();
			case PROTO_MNDOT_4:
			case PROTO_MNDOT_5:
				return createMndotPoller();
			case PROTO_SMART_SENSOR:
				return createSmartSensorPoller();
			case PROTO_CANOGA:
				return createCanogaPoller();
			case PROTO_VICON:
				return createViconPoller();
			case PROTO_PELCO_D:
				return createPelcoDPoller();
			case PROTO_MANCHESTER:
				return createManchesterPoller();
			case PROTO_DMSLITE:
				return createDmsLitePoller();
			case PROTO_AWS:
				return createAwsPoller();
			case PROTO_PELCO:
				return createPelcoPoller();
			case PROTO_VICON_PTZ:
				return createViconPTZPoller();
			default:
				throw new ProtocolException("INVALID PROTOCOL");
		}
	}

	/** Open the message poller */
	protected synchronized MessagePoller openPoller() {
		closePoller();
		try {
			poller = createPoller();
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
		if(poller != null)
			poller.stopPolling();
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

	/** Add an operation to the communication link */
	void addOperation(Operation o) {
		MessagePoller p = getPoller();
		if(p != null)
			p.addOperation(o);
	}

	/** Line load */
	protected transient float load;

	/** Get the current link load */
	public float getLoad() {
		return load;
	}

	/** Perform downloads on all controllers on this link */
	public void download() {
		MessagePoller p = getPoller();
		if(p != null) {
			synchronized(controllers) {
				for(ControllerImpl c: controllers.values())
					p.download(c, false);
			}
		}
	}

	/** Poll this communication link for 30-second data */
	public void poll30Second(Completer comp) {
		MessagePoller p = getPoller();
		if(p != null) {
			load = p.getLoad();
			synchronized(controllers) {
				for(ControllerImpl c: controllers.values())
					p.poll30Second(c, comp);
			}
		} else
			load = 0;
	}

	/** Poll this communication link for 5-minute data */
	public void poll5Minute(Completer comp) {
		MessagePoller p = getPoller();
		if(p != null) {
			synchronized(controllers) {
				for(ControllerImpl c: controllers.values()) {
					c.resetPeriod(ErrorCounter.PERIOD_5_MIN);
					p.poll5Minute(c, comp);
				}
			}
		}
	}

	/** Poll this communication link for 1-hour data */
	public void poll1Hour() {
		synchronized(controllers) {
			for(ControllerImpl c: controllers.values())
				c.resetPeriod(ErrorCounter.PERIOD_1_HOUR);
		}
	}

	/** Poll this communication link for 1-day data */
	public void poll1Day() {
		synchronized(controllers) {
			for(ControllerImpl c: controllers.values())
				c.resetPeriod(ErrorCounter.PERIOD_1_DAY);
		}
	}
}
