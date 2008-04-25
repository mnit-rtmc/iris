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
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.TreeMap;
import javax.comm.SerialPort;
import java.lang.NoClassDefFoundError;

import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.comm.KillThread;
import us.mn.state.dot.tms.comm.Messenger;
import us.mn.state.dot.tms.comm.Operation;
import us.mn.state.dot.tms.comm.ProtocolException;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.SerialMessenger;
import us.mn.state.dot.tms.comm.SignPoller;
import us.mn.state.dot.tms.comm.SocketMessenger;
import us.mn.state.dot.tms.comm.HttpFileMessenger;
import us.mn.state.dot.tms.comm.canoga.CanogaPoller;
import us.mn.state.dot.tms.comm.manchester.ManchesterPoller;
import us.mn.state.dot.tms.comm.mndot.MndotPoller;
import us.mn.state.dot.tms.comm.ntcip.HDLCMessenger;
import us.mn.state.dot.tms.comm.ntcip.NtcipPoller;
import us.mn.state.dot.tms.comm.pelco.PelcoPoller;
import us.mn.state.dot.tms.comm.smartsensor.SmartSensorPoller;
import us.mn.state.dot.tms.comm.vicon.ViconPoller;
import us.mn.state.dot.tms.comm.dmslite.DmsLitePoller;
import us.mn.state.dot.tms.comm.caws.CawsPoller;
import us.mn.state.dot.vault.FieldMap;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * The CommunicationLineImpl class represents a single serial line which
 * is connected with multiple field device controllers. Five different
 * protocols are supported: Mn/DOT's 4- and 5-bit 170 protocols, NTCIP class B,
 * Wavetronix SmartSensor, and 3M Canoga.
 *
 * @author Douglas Lau
 */
final class CommunicationLineImpl extends TMSObjectImpl
	implements CommunicationLine, ErrorCounter, Storable
{
	/** ObjectVault table name */
	static public final String tableName = "communication_line";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Create a new communication line */
	public CommunicationLineImpl(int i) throws RemoteException {
		super();
		index = i;
		port = "/dev/ttyD" + (index - 1);
		circuits = new LinkedList<CircuitImpl>();
		open();
	}

	/** Create a communication line from an ObjectVault field map */
	protected CommunicationLineImpl(FieldMap fields)
		throws RemoteException
	{
		super();
		index = fields.getInt("index");
		port = (String)fields.get("port");
		bitRate = fields.getInt("bitRate");
		protocol = fields.getShort("protocol");
		timeout = fields.getInt("timeout");
		circuits = new LinkedList<CircuitImpl>();
		open();
	}

	/** Get a string representation */
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(index);
		while(b.length() < 2)
			b.insert(0, " ");
		b.append("> ");
		b.append(description);
		return b.toString();
	}

	/** Print the current status of the line */
	public void print(PrintStream ps) {
		MessagePoller p = poller;	// Avoid NPE
		if(p != null)
			p.print(ps);
	}

	/** Line number index */
	protected final int index;

	/** Get line number index */
	public int getIndex() {
		return index;
	}

	/** Description of communication line */
	protected String description = "<New Line>";

	/** Set text description */
	public synchronized void setDescription(String d) throws TMSException {
		if(d.equals(description))
			return;
		store.update(this, "description", d);
		description = d;
	}

	/** Get text description */
	public String getDescription() {
		return description;
	}

	/** Serial port device name */
	protected String port;

	/** Set serial port device name */
	public synchronized void setPort(String p) throws TMSException {
		if(p.equals(port))
			return;
		store.update(this, "port", p);
		close();
		port = p;
		open();
	}

	/** Get serial port device name */
	public String getPort() {
		return port;
	}

	/** Bit rate (bits per second) */
	protected int bitRate = 9600;

	/** Set the baud rate */
	public synchronized void setBitRate(int b) throws TMSException {
		if(b == bitRate)
			return;
		try {
			if(messenger instanceof SerialMessenger) {
				SerialMessenger sm = (SerialMessenger)messenger;
				sm.setBaudRate(b);
			}
		}
		catch(IOException e) {
			status = e.getMessage();
			return;
		}
		store.update(this, "bitRate", b);
		bitRate = b;
	}

	/** Get the bit rate */
	public int getBitRate() {
		return bitRate;
	}

	/** Communication protocol */
	protected short protocol = PROTO_NTCIP_C;   // TCP based

	/** Check if a Mn/DOT protocol change should be vetoed */
	protected void checkMndotProtocolChange(short p) throws TMSException {
		if(p == PROTO_MNDOT_4) {
			for(Integer d: controllers.keySet()) {
				if(d.intValue() > 15) {
					throw new ChangeVetoException(
						"Existing drop: " + d);
				}
			}
		}
	}

	/** Check if a Camera protocol change should be vetoed */
	protected void checkCameraProtocolChange(short p) throws TMSException {
		if(p == PROTO_PELCO) {
			for(Integer d: controllers.keySet()) {
				if(d.intValue() > PelcoPoller.ADDRESS_MAX) {
					throw new ChangeVetoException(
						"Existing drop: " + d);
				}
			}
		}
	}

	/** Check if a protocol change should be vetoed */
	protected void checkProtocolChange(short p) throws TMSException {
		if((p == PROTO_MNDOT_4 || p == PROTO_MNDOT_5) &&
		   (protocol == PROTO_MNDOT_4 || protocol == PROTO_MNDOT_5))
		{
			checkMndotProtocolChange(p);
			return;
		}
		if((p == PROTO_PELCO || p == PROTO_MANCHESTER) &&
		   (protocol == PROTO_PELCO || protocol == PROTO_MANCHESTER))
		{
			checkCameraProtocolChange(p);
			return;
		}
		throw new ChangeVetoException("Controllers exist");
	}

	/** Set the communication protocol */
	public synchronized void setProtocol(short p) throws TMSException {
		if(p == protocol)
			return;
		if(!controllers.isEmpty())
			checkProtocolChange(p);
		store.update(this, "protocol", p);
		close();
		protocol = p;
		open();
	}

	/** Get the communication protocol */
	public short getProtocol() {
		return protocol;
	}

	/** Polling timeout (milliseconds) */
	protected int timeout = 750;

	/** Set the polling timeout (milliseconds) */
	public synchronized void setTimeout(int t) throws TMSException {
		if(t == timeout)
			return;
		try {
			if(messenger != null)
				messenger.setTimeout(t);
		}
		catch(IOException e) {
			status = e.getMessage();
			return;
		}
		store.update(this, "timeout", t);
		timeout = t;
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

	/** Check if the port device is a serial port */
	protected boolean isSerialPort() {
		// Note: this is linux-specific
		return port.startsWith("/");
	}

	/** Parse a TCP port */
	protected int parseTcpPort(String p) throws IOException {
		try {
			return Integer.parseInt(p);
		}
		catch(NumberFormatException e) {
			throw new IOException("INVALID TCP PORT: " + p);
		}
	}

	/** Create a socket messenger */
	protected Messenger createSocketMessenger() throws IOException {
		String[] s = port.split(":");
		if(s.length != 2)
			throw new IOException("INVALID SOCKET ADDRESS");
		InetAddress a = InetAddress.getByName(s[0]);
		int p = parseTcpPort(s[1]);
		return new SocketMessenger(a, p);
	}

	/** Create an http file messenger */
	protected Messenger createHttpFileMessenger() throws IOException {
		return new HttpFileMessenger(new URL(port));
	}

	/** Create a serial or socket messenger */
	protected Messenger createMessenger(int threshold, int parity)
		throws IOException
	{
		if(isSerialPort())
			return new SerialMessenger(port, threshold, parity);
		else
			return createSocketMessenger();
	}

	/** Create an NTCIP Class C poller */
	protected MessagePoller createNtcipCPoller() throws IOException {
		messenger = createSocketMessenger();
		return new NtcipPoller(String.valueOf(index), messenger);
	}

	/** Create an NTCIP Class B poller */
	protected MessagePoller createNtcipBPoller() throws IOException {
		messenger = createMessenger(1024, SerialPort.PARITY_NONE);
		HDLCMessenger hdlc = new HDLCMessenger(messenger);
		return new NtcipPoller(String.valueOf(index), hdlc);
	}

	/** Create a Mn/DOT poller */
	protected MessagePoller createMndotPoller() throws IOException {
		messenger = createMessenger(127, SerialPort.PARITY_EVEN);
		return new MndotPoller(String.valueOf(index), messenger,
			protocol);
	}

	/** Create a SmartSensor poller */
	protected MessagePoller createSmartSensorPoller() throws IOException {
		messenger = createMessenger(0, SerialPort.PARITY_NONE);
		return new SmartSensorPoller(String.valueOf(index), messenger);
	}

	/** Create a Canoga poller */
	protected MessagePoller createCanogaPoller() throws IOException {
		messenger = createMessenger(0, SerialPort.PARITY_NONE);
		return new CanogaPoller(String.valueOf(index), messenger);
	}

	/** Create a Vicon poller */
	protected MessagePoller createViconPoller() throws IOException {
		messenger = createMessenger(0, SerialPort.PARITY_NONE);
		return new ViconPoller(String.valueOf(index), messenger);
	}

	/** Create a Pelco poller */
	protected MessagePoller createPelcoPoller() throws IOException {
		messenger = createMessenger(0, SerialPort.PARITY_NONE);
		return new PelcoPoller(String.valueOf(index), messenger);
	}

	/** Create a Manchester poller */
	protected MessagePoller createManchesterPoller() throws IOException {
		messenger = createMessenger(0, SerialPort.PARITY_NONE);
		return new ManchesterPoller(String.valueOf(index), messenger);
	}

	/** Create a DMS Lite poller */
	protected MessagePoller createDmsLitePoller() throws IOException {
		messenger = createSocketMessenger();
		return new DmsLitePoller(String.valueOf(index), messenger);
	}

	/** Create a CAWS poller */
	protected MessagePoller createCawsPoller() throws IOException {
		messenger = createHttpFileMessenger();
		return new CawsPoller(String.valueOf(index), messenger);
	}

	/** Try to open the communication line */
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

	/** Open the communication port */
	protected synchronized void open() {
		try {
			poller = createPoller();
			poller.start();
			if(messenger instanceof SerialMessenger) {
				SerialMessenger sm = (SerialMessenger)messenger;
				sm.setBaudRate(bitRate);
			}
			messenger.setTimeout(timeout);
			messenger.open();
			status = "OK";
		}
		catch(IOException e) {
			close();
			status = e.getMessage();
			return;
		}
		// the iris installation might not have the java serial port
		// jar installed
		catch(NoClassDefFoundError e) {
			status = "RS232 not supported.";
			return;
		}
		notifyStatus();
	}

	/** Close the communication port */
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

	/** Communication line status */
	protected transient String status = UNKNOWN;

	/** Get the communication status */
	public String getStatus() {
		return status;
	}

	/** Field device controllers */
	protected transient final TreeMap<Integer, ControllerImpl> controllers =
		new TreeMap<Integer, ControllerImpl>();

	/** Put a controller on the line */
	public synchronized void putController(ControllerImpl c) {
		Integer d = new Integer(c.getDrop());
		controllers.put(d, c);
	}

	/** Pull a controller from the line */
	public synchronized void pullController(ControllerImpl c) {
		Integer d = new Integer(c.getDrop());
		controllers.remove(d);
	}

	/** Add a controller at the specified drop address */
	public synchronized Controller addController(CircuitImpl circuit,
		short drop) throws TMSException, RemoteException
	{
		MessagePoller p = poller;	// Avoid NPE
		if(p == null)
			throw new ChangeVetoException("Inactive line");
		if(!p.isAddressValid(drop))
			throw new ChangeVetoException("Invalid drop address");
		Integer d = new Integer(drop);
		if(controllers.containsKey(d))
			throw new ChangeVetoException("Controller exists");
		ControllerImpl c;
		switch(protocol) {
			case PROTO_MNDOT_4:
			case PROTO_MNDOT_5:
				c = new Controller170Impl(circuit, drop);
				break;
			case PROTO_NTCIP_B:
			case PROTO_NTCIP_C:
			case PROTO_SMART_SENSOR:
			case PROTO_CANOGA:
			case PROTO_VICON:
			case PROTO_PELCO:
			case PROTO_MANCHESTER:
			case PROTO_DMSLITE:
			case PROTO_CAWS:
				c = new ControllerImpl(circuit, drop);
				break;
			default:
				throw new ChangeVetoException(
					"Invalid protocol" );
		}
		try {
			vault.save(c, getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		controllers.put(d, c);
		return c;
	}

	/** Get a controller by drop */
	public synchronized Controller getController(short drop) {
		Integer d = new Integer(drop);
		return controllers.get(d);
	}

	/** Remove a controller at the specified drop address */
	public synchronized ControllerImpl removeController(short drop)
		throws TMSException
	{
		Integer d = new Integer(drop);
		ControllerImpl c = controllers.get(d);
		if(c == null)
			throw new ChangeVetoException("No controller: " + drop);
		if(!c.isDeletable())
			throw new ChangeVetoException("Cannot delete object");
		try {
			vault.delete(c, getUserName());
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		controllers.remove(d);
		c.notifyDelete();
		return c;
	}

	/** Get the controllers defined for this communication line */
	public synchronized Controller[] getControllers() {
		return (Controller [])controllers.values().toArray(
			new Controller[0]);
	}

	/** Circuits assiciated with this communicaton line */
	protected transient LinkedList<CircuitImpl> circuits;

	/** Add a circuit to this communication line */
	public synchronized void addCircuit(CircuitImpl c) {
		if(!circuits.contains(c))
			circuits.add(c);
	}

	/** Remove a circuit from this communication line */
	public synchronized void removeCircuit(Circuit c) {
		circuits.remove(c);
	}

	/** Get the circuits assigned to this communication line */
	public synchronized Circuit[] getCircuits() {
		return (Circuit [])circuits.toArray(new Circuit[0]);
	}

	/** Find the controller */
	public synchronized ControllerImpl findController(Controller c) {
		for(ControllerImpl cont: controllers.values()) {
			if(cont.equals(c))
				return cont;
		}
		return null;
	}

	/** Add an operation to the communication line */
	void addOperation(Operation o) {
		MessagePoller p = poller;	// Avoid NPE races
		if(p != null)
			p.addOperation(o);
	}

	/** Perform downloads on all controllers on this line */
	public synchronized void download() {
		MessagePoller p = poller;	// Avoid NPE races
		if(p != null) {
			for(ControllerImpl c: controllers.values())
				p.download(c, false);
		}
	}

	/** Line load */
	protected transient float load;

	/** Get the current line load */
	public float getLoad() {
		return load;
	}

	/** Poll this communication line for sign status data */
	public synchronized void pollSigns(Completer comp) {
		MessagePoller p = poller;	// Avoid NPE races
		if(p instanceof SignPoller) {
			SignPoller sp = (SignPoller)p;
			for(ControllerImpl c: controllers.values())
				sp.pollSigns(c, comp);
		}
	}

	/** Poll this communication line for 30-second data */
	public synchronized void poll30Second(Completer comp) {
		MessagePoller p = poller;	// Avoid NPE races
		if(p != null) {
			load = p.getLoad();
			for(ControllerImpl c: controllers.values())
				p.poll30Second(c, comp);
		} else
			load = 0;
	}

	/** Poll this communication line for 5-minute data */
	public synchronized void poll5Minute(Completer comp) {
		MessagePoller p = poller;	// Avoid NPE races
		if(p != null) {
			for(ControllerImpl c: controllers.values()) {
				c.resetPeriod(PERIOD_5_MIN);
				p.poll5Minute(c, comp);
			}
		}
	}

	/** Poll this communication line for 1-hour data */
	public synchronized void poll1Hour() {
		for(ControllerImpl c: controllers.values())
			c.resetPeriod(PERIOD_1_HOUR);
	}

	/** Poll this communication line for 1-day data */
	public synchronized void poll1Day() {
		for(ControllerImpl c: controllers.values())
			c.resetPeriod(PERIOD_1_DAY);
	}

	/** Get summed counters for all controllers on the line */
	public synchronized int[][] getCounters() {
		int[][] counters = new int[TYPES.length][PERIODS.length];
		for(ControllerImpl cont: controllers.values()) {
			if(!cont.isActive())
				continue;
			int[][] count = cont.getCounters();
			for(int c = 0; c < TYPES.length; c++)
				for(int p = 0; p < PERIODS.length; p++)
					counters[c][p] += count[c][p];
		}
		return counters;
	}
}
