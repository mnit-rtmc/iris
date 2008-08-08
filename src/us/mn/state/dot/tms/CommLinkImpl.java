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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.comm.HttpFileMessenger;
import us.mn.state.dot.tms.comm.KillThread;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.Messenger;
import us.mn.state.dot.tms.comm.Operation;
import us.mn.state.dot.tms.comm.ProtocolException;
import us.mn.state.dot.tms.comm.SignPoller;
import us.mn.state.dot.tms.comm.SocketMessenger;
import us.mn.state.dot.tms.comm.canoga.CanogaPoller;
import us.mn.state.dot.tms.comm.caws.CawsPoller;
import us.mn.state.dot.tms.comm.dmslite.DmsLitePoller;
import us.mn.state.dot.tms.comm.manchester.ManchesterPoller;
import us.mn.state.dot.tms.comm.mndot.MndotPoller;
import us.mn.state.dot.tms.comm.ntcip.HDLCMessenger;
import us.mn.state.dot.tms.comm.ntcip.NtcipPoller;
import us.mn.state.dot.tms.comm.pelco.PelcoPoller;
import us.mn.state.dot.tms.comm.smartsensor.SmartSensorPoller;
import us.mn.state.dot.tms.comm.vicon.ViconPoller;

/**
 * The CommLinkImpl class represents a single communication link which is
 * connected with one or more field device controllers. Many different
 * protocols are supported, including Mn/DOT's 4- and 5-bit 170 protocols,
 * NTCIP class B, Wavetronix SmartSensor, and 3M Canoga.
 *
 * @author Douglas Lau
 */
public class CommLinkImpl extends BaseObjectImpl implements CommLink {

	/** Load all the comm links */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading comm links...");
		namespace.registerType(SONAR_TYPE, CommLinkImpl.class);
		store.query("SELECT name, description, url, protocol, " +
			"timeout FROM " + SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.add(new CommLinkImpl(
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
		open();
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
	protected String url;

	/** Set remote URL for link */
	public void setUrl(String u) {
		url = u;
	}

	/** Set remote URL for link */
	public void doSetUrl(String u) throws TMSException {
		if(u.equals(url))
			return;
		store.update(this, "url", u);
		close();
		setUrl(u);
		open();
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
		close();
		setProtocol(p);
		open();
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
			if(messenger != null)
				messenger.setTimeout(t);
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

	/** Get the message poller */
	public MessagePoller getPoller() {
		return poller;
	}

	/** Communication messenger */
	protected transient Messenger messenger;

	/** Parse a TCP port */
	protected int parseTcpPort(String p) throws IOException {
		try {
			int i = Integer.parseInt(p);
			if(i >= 0 && i <= 65535)
				return i;
		}
		catch(NumberFormatException e) {
			// Fall out
		}
		throw new IOException("INVALID TCP PORT: " + p);
	}

	/** Create a socket messenger */
	protected Messenger createSocketMessenger() throws IOException {
		String[] s = url.split(":");
		if(s.length != 2)
			throw new IOException("INVALID SOCKET ADDRESS");
		int p = parseTcpPort(s[1]);
		return new SocketMessenger(new InetSocketAddress(s[0], p));
	}

	/** Create an http file messenger */
	protected Messenger createHttpFileMessenger() throws IOException {
		return new HttpFileMessenger(new URL(url));
	}

	/** Create an NTCIP Class C poller */
	protected MessagePoller createNtcipCPoller() throws IOException {
		messenger = createSocketMessenger();
		return new NtcipPoller(name, messenger);
	}

	/** Create an NTCIP Class B poller */
	protected MessagePoller createNtcipBPoller() throws IOException {
		messenger = createSocketMessenger();
		HDLCMessenger hdlc = new HDLCMessenger(messenger);
		return new NtcipPoller(name, hdlc);
	}

	/** Create a Mn/DOT poller */
	protected MessagePoller createMndotPoller() throws IOException {
		messenger = createSocketMessenger();
		return new MndotPoller(name, messenger, protocol);
	}

	/** Create a SmartSensor poller */
	protected MessagePoller createSmartSensorPoller() throws IOException {
		messenger = createSocketMessenger();
		return new SmartSensorPoller(name, messenger);
	}

	/** Create a Canoga poller */
	protected MessagePoller createCanogaPoller() throws IOException {
		messenger = createSocketMessenger();
		return new CanogaPoller(name, messenger);
	}

	/** Create a Vicon poller */
	protected MessagePoller createViconPoller() throws IOException {
		messenger = createSocketMessenger();
		return new ViconPoller(name, messenger);
	}

	/** Create a Pelco poller */
	protected MessagePoller createPelcoPoller() throws IOException {
		messenger = createSocketMessenger();
		return new PelcoPoller(name, messenger);
	}

	/** Create a Manchester poller */
	protected MessagePoller createManchesterPoller() throws IOException {
		messenger = createSocketMessenger();
		return new ManchesterPoller(name, messenger);
	}

	/** Create a DMS Lite poller */
	protected MessagePoller createDmsLitePoller() throws IOException {
		messenger = createSocketMessenger();
		return new DmsLitePoller(name, messenger);
	}

	/** Create a CAWS poller */
	protected MessagePoller createCawsPoller() throws IOException {
		messenger = createHttpFileMessenger();
		return new CawsPoller(name, messenger);
	}

	/** Try to open the communication link */
	protected MessagePoller createPoller() throws IOException {
		switch(protocol) {
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
			case PROTO_PELCO:
				return createPelcoPoller();
			case PROTO_MANCHESTER:
				return createManchesterPoller();
			case PROTO_DMSLITE:
				return createDmsLitePoller();
			case PROTO_CAWS:
				return createCawsPoller();
			default:
				throw new ProtocolException("INVALID PROTOCOL");
		}
	}

	/** Open the communication link */
	protected synchronized void open() {
		try {
			poller = createPoller();
			poller.start();
			messenger.setTimeout(timeout);
			messenger.open();
			status = "";
		}
		catch(IOException e) {
			close();
			status = e.getMessage();
		}
		finally {
			if(MainServer.server == null)
				return;
			MainServer.server.setAttribute(this, "status",
				new String[] { status });
		}
	}

	/** Close the communication link */
	protected synchronized void close() {
		if(poller != null) {
			poller.addOperation(new KillThread());
			try {
				poller.join();
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}
			poller = null;
		}
		if(messenger != null) {
			messenger.close();
			messenger = null;
		}
	}

	/** Communication link status */
	protected transient String status = Constants.UNKNOWN;

	/** Get the communication status */
	public String getStatus() {
		return status;
	}

	/** Field device controllers */
	protected transient final TreeMap<Integer, ControllerImpl> controllers =
		new TreeMap<Integer, ControllerImpl>();

	/** Put a controller on the link */
	public synchronized void putController(int d, ControllerImpl c)
		throws ChangeVetoException
	{
		if(controllers.containsKey(d))
			throw new ChangeVetoException("Drop " + d + " exists");
		controllers.put(d, c);
	}

	/** Pull a controller from the link */
	public synchronized void pullController(ControllerImpl c) {
		Integer d = new Integer(c.getDrop());
		controllers.remove(d);
	}

	/** Get a controller by drop */
	public synchronized Controller getController(short drop) {
		Integer d = new Integer(drop);
		return controllers.get(d);
	}

	/** Get the controllers defined for this communication link */
	public synchronized Controller[] getControllers() {
		return (Controller [])controllers.values().toArray(
			new Controller[0]);
	}

	/** Find the controller */
	public synchronized ControllerImpl findController(Controller c) {
		for(ControllerImpl cont: controllers.values()) {
			if(cont.equals(c))
				return cont;
		}
		return null;
	}

	/** Add an operation to the communication link */
	void addOperation(Operation o) {
		MessagePoller p = poller;	// Avoid NPE races
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
	public synchronized void download() {
		MessagePoller p = poller;	// Avoid NPE races
		if(p != null) {
			for(ControllerImpl c: controllers.values())
				p.download(c, false);
		}
	}

	/** Poll this communication link for sign status data */
	public synchronized void pollSigns(Completer comp) {
		MessagePoller p = poller;	// Avoid NPE races
		if(p instanceof SignPoller) {
			SignPoller sp = (SignPoller)p;
			for(ControllerImpl c: controllers.values())
				sp.pollSigns(c, comp);
		}
	}

	/** Poll this communication link for 30-second data */
	public synchronized void poll30Second(Completer comp) {
		MessagePoller p = poller;	// Avoid NPE races
		if(p != null) {
			load = p.getLoad();
			for(ControllerImpl c: controllers.values())
				p.poll30Second(c, comp);
		} else
			load = 0;
	}

	/** Poll this communication link for 5-minute data */
	public synchronized void poll5Minute(Completer comp) {
		MessagePoller p = poller;	// Avoid NPE races
		if(p != null) {
			for(ControllerImpl c: controllers.values()) {
				c.resetPeriod(ErrorCounter.PERIOD_5_MIN);
				p.poll5Minute(c, comp);
			}
		}
	}

	/** Poll this communication link for 1-hour data */
	public synchronized void poll1Hour() {
		for(ControllerImpl c: controllers.values())
			c.resetPeriod(ErrorCounter.PERIOD_1_HOUR);
	}

	/** Poll this communication link for 1-day data */
	public synchronized void poll1Day() {
		for(ControllerImpl c: controllers.values())
			c.resetPeriod(ErrorCounter.PERIOD_1_DAY);
	}
}
