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
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import us.mn.state.dot.tms.comm.DiagnosticOperation;
import us.mn.state.dot.tms.comm.ControllerOperation;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.log.CommunicationLineEvent;
import us.mn.state.dot.vault.FieldMap;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * The ControllerImpl class represents a generic traffic controller.
 * Subclasses such as Controller170Impl are defined for particular types of
 * controllers.
 *
 * @author Douglas Lau
 */
public class ControllerImpl extends TMSObjectImpl implements Controller,
	ErrorCounter, Storable
{
	/** ObjectVault table name */
	static public final String tableName = "controller";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Communication failure retry threshold */
	static public final int RETRY_THRESHOLD = 3;

	/** Create a new Controller */
	public ControllerImpl(CircuitImpl c, short d) throws RemoteException {
		super();
		circuit = c;
		drop = d;
		version = UNKNOWN;
		counters = new int[ TYPES.length ][ PERIODS.length ];
		location = new LocationImpl();
		notes = "";
	}

	/** Create a controller from an ObjectVault field map */
	protected ControllerImpl( FieldMap fields ) throws RemoteException {
		super();
		location = (LocationImpl)fields.get("location");
	}

	/** Initialize the transient fields */
	public void initTransients() {
		version = UNKNOWN;
		counters = new int[TYPES.length][PERIODS.length];
		circuit.putController(this);
	}

	/** Create a string representation of the controller */
	public String toString() {
		CommunicationLineImpl line =
			(CommunicationLineImpl)circuit.getLine();
		return "(" + line.getIndex() + ":" + drop + ")";
	}

	/** Get controller label */
	public String getLabel() {
		CommunicationLineImpl line =
			(CommunicationLineImpl)circuit.getLine();
		StringBuffer buf = new StringBuffer();
		buf.append( "Line " );
		buf.append(line.getIndex());
		buf.append( " drop " );
		buf.append( drop );
		return buf.toString();
	}

	/** Circuit */
	protected CircuitImpl circuit;

	/** Get the circuit */
	public Circuit getCircuit() { return circuit; }

	/** Set the circuit for this controller */
	protected synchronized void setCircuit(CircuitImpl c)
		throws TMSException
	{
		if(c == circuit)
			return;
		CommunicationLineImpl line = (CommunicationLineImpl)c.getLine();
		if(line != getLine() && line.getController(drop) != null)
			throw new ChangeVetoException("Duplicate drop address");
		store.update(this, "circuit", c.getOID());
		circuit.pullController(this);
		circuit = c;
		circuit.putController(this);
	}

	/** Set the circuit for this controller */
	public void setCircuit(String id) throws TMSException {
		if(id.equals(circuit.getId()))
			return;
		CircuitImpl c = groupList.findCircuit(id);
		if(c == null)
			throw new ChangeVetoException("Invalid circuit ID");
		setCircuit(c);
	}

	/** Get the communication line */
	public final CommunicationLine getLine() {
		return circuit.getLine();
	}

	/** Drop address */
	protected short drop;

	/** Get the drop address */
	public short getDrop() { return drop; }

	/** Set the drop address */
	public synchronized void setDrop(short d) throws TMSException {
		if(d == drop)
			return;
		CommunicationLineImpl line = (CommunicationLineImpl)getLine();
		if(line.getController(d) != null)
			throw new ChangeVetoException("Duplicate drop address");
		store.update(this, "drop", d);
		circuit.pullController(this);
		drop = d;
		circuit.putController(this);
	}

	/** Active status flag */
	protected boolean active;

	/** Set the active status */
	public synchronized void setActive(boolean a) throws TMSException {
		if(a == active)
			return;
		store.update(this, "active", a);
		active = a;
		updateNowCounters();
	}

	/** Get the active status */
	public boolean isActive() { return active; }

	/** Controller location */
	protected final LocationImpl location;

	/** Get the controller location */
	public Location getLocation() {
		return location;
	}

	/** Administrator notes for this controller */
	protected String notes;

	/** Get the administrator notes */
	public String getNotes() { return notes; }

	/** Set the administrator notes */
	public synchronized void setNotes(String n) throws TMSException {
		if(n.equals(notes))
			return;
		store.update(this, "notes", n);
		notes = n;
	}

	/** Milepoint on freeway */
	protected float mile;

	/** Set the milepoint */
	public synchronized void setMile(float m) throws TMSException {
		if(m == mile)
			return;
		store.update(this, "mile", m);
		mile = m;
	}

	/** Get the milepoint */
	public float getMile() { return mile; }

	/** Mapping of all controller I/O pins */
	protected transient HashMap<Integer, ControllerIO> io_pins =
		new HashMap<Integer, ControllerIO>();

	/** I/O pin for first traffic device */
	static protected final int DEVICE_PIN = 1;

	/** Set the (first) traffic device */
	public void setDevice(String id) throws TMSException {
		DeviceImpl od = getDeviceImpl();
		if(od instanceof DetectorImpl)
			return;
		DeviceImpl d = (DeviceImpl)deviceList.getElement(id);
		if(d == null && id != null) {
			if(od == null || !id.equals(od.getId())) {
				throw new ChangeVetoException(
					"Device unavailable: " + id);
			}
			return;
		}
		setIO(DEVICE_PIN, d);
	}

	/** Assign an IO to the specified controller I/O pin */
	protected synchronized void setIO(int p, ControllerIO io)
		throws TMSException
	{
		if(p < 1)
			throw new ChangeVetoException("Invalid pin: " + p);
		Integer pin = new Integer(p);
		ControllerIO old_io = io_pins.get(pin);
		if(checkIO(io, old_io, p))
			return;
		if(old_io != null) {
			if(io != old_io)
				old_io.setController(null);
			io_pins.remove(pin);
		}
		if(io != null) {
			if(io != old_io)
				io.setController(this);
			io.setPin(p);
			io_pins.put(pin, io);
		}
	}

	/** Check if IO does not need to be changed */
	protected boolean checkIO(ControllerIO io, ControllerIO old_io, int p)
		throws TMSException
	{
		if(io != null) {
			if(io == old_io)
				return p == io.getPin();
			else if(old_io != null) {
				throw new ChangeVetoException("Pin " + p +
					" already assigned");
			}
		}
		return io == old_io;
	}

	/** Get the (first) traffic device */
	protected synchronized DeviceImpl getDeviceImpl() {
		ControllerIO io = io_pins.get(DEVICE_PIN);
		if(io instanceof DeviceImpl)
			return (DeviceImpl)io;
		else
			return null;
	}

	/** Get the (first) traffic device */
	public TrafficDevice getDevice() {
		Device d = getDeviceImpl();
		if(d instanceof TrafficDevice)
			return (TrafficDevice)d;
		else
			return null;
	}

	/** Set all controller devices to failed status */
	public synchronized void failDevices() {
		for(ControllerIO io: io_pins.values()) {
			if(io instanceof TrafficDeviceImpl) {
				TrafficDeviceImpl t = (TrafficDeviceImpl)io;
				t.setStatus(null);
				t.notifyStatus();
			}
		}
	}

	/** Determine whether this controller has an active ramp meter */
	public synchronized boolean hasActiveMeter() {
		if(isActive()) {
			for(ControllerIO io: io_pins.values()) {
				if(io instanceof RampMeterImpl)
					return true;
			}
		}
		return false;
	}

	/** Get the meter number on the controller */
	public int getMeterNumber(RampMeterImpl meter) {
		if(isActive()) {
			if(meter == getDevice())
				return 1;
		}
		return 0;
	}

	/** Get an active DMS for the controller */
	public DMSImpl getActiveSign() {
		if(isActive()) {
			DeviceImpl d = getDeviceImpl();
			if(d instanceof DMSImpl)
				return (DMSImpl)d;
		}
		return null;
	}

	/** Assign a detector to an input */
	protected void setDetector(int input, DetectorImpl d)
		throws TMSException
	{
		setIO(input, d);
	}

	/** Assign a detector input to a detector index */
	public void setDetector(int input, int index) throws TMSException {
		if(index > 0) {
			DetectorImpl d =
				(DetectorImpl)detList.getElement(index);
			if(!d.isAvailable()) {
				throw new ChangeVetoException(
					"Detector already assigned");
			}
			setDetector(input, d);
		} else
			setDetector(input, null);
	}

	/** Get a detector by its I/O pin number */
	protected synchronized Detector getDetectorAtPin(int pin) {
		ControllerIO io = io_pins.get(pin);
		if(io instanceof Detector)
			return (Detector)io;
		else
			return null;
	}

	/** Get a data detector (first is detector 1) */
	public Detector getDetector(int input) {
		return getDetectorAtPin(input);
	}

	/** Check whether this controller has any active detectors */
	public synchronized boolean hasActiveDetector() {
		if(isActive()) {
			for(ControllerIO io: io_pins.values()) {
				if(io instanceof DetectorImpl)
					return true;
			}
		}
		return false;
	}

	/** Find a matching detector for the specified velocity detector */
	protected synchronized int getSpeedPair(DetectorImpl v) {
		for(Map.Entry<Integer, ControllerIO> e: io_pins.entrySet()) {
			if(v.isSpeedPair(e.getValue()))
				return e.getKey();
		}
		return 0;
	}

	/** Find a matching detector for the specified input */
	public int getSpeedPair(int input) {
		Detector d = getDetector(input);
		if(d instanceof DetectorImpl) {
			DetectorImpl v = (DetectorImpl)d;
			if(v.isVelocity())
				return getSpeedPair(v);
		}
		return 0;
	}

	/** Add an alarm to the controller */
	public Alarm addAlarm(int pin) throws TMSException, RemoteException {
		AlarmImpl alarm = new AlarmImpl();
		try {
			vault.save(alarm, "IRIS");
		}
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		setIO(pin, alarm);
		return alarm;
	}

	/** Remove an alarm from the controller */
	public void removeAlarm(int pin) throws TMSException {
		AlarmImpl alarm = (AlarmImpl)getAlarm(pin);
		if(alarm != null) {
			setIO(pin, null);
			alarm.notifyDelete();
			try {
				vault.delete(alarm, "IRIS");
			}
			catch(ObjectVaultException e) {
				throw new TMSException(e);
			}
		}
	}

	/** Get an alarm from the controller */
	public synchronized Alarm getAlarm(int pin) {
		ControllerIO io = io_pins.get(pin);
		if(io instanceof Alarm)
			return (Alarm)io;
		else
			return null;
	}

	/** Check whether this controller has any alarms */
	public synchronized boolean hasAlarm() {
		for(ControllerIO io: io_pins.values()) {
			if(io instanceof AlarmImpl)
				return true;
		}
		return false;
	}

	/** Store 30-second detector data */
	public void storeData30Second(Calendar stamp, int[] volume,
		int[] scans, int[] speed)
	{
		for(int i = 0; i < volume.length; i++) {
			DetectorImpl det = (DetectorImpl)getDetector(i + 1);
			if(det != null) {
				det.storeData30Second(stamp, volume[i],
					scans[i]);
				if(speed != null)
					det.storeSpeed30Second(stamp, speed[i]);
			// warn if expected lane doesn't exist
			} else {
				int dn=i+1;
				System.err.println("Warning: expected detector missing (#"+dn+") from controller ("+this.getLabel()+").");
			}
		}
	}

	/** Store 5-minute detector data */
	public void storeData5Minute(Calendar stamp, int[] volume,
		int[] scan) throws IOException
	{
		for(int i = 0; i < volume.length; i++) {
			DetectorImpl det = (DetectorImpl)getDetector(i + 1);
			if(det != null)
				det.storeData5Minute(stamp, volume[i], scan[i]);
		}
	}

	/** Log a vehicle detection event */
	public void logEvent(Calendar stamp, int io_pin, int duration,
		int headway, int speed)
	{
		DetectorImpl det = (DetectorImpl)getDetector(io_pin);
		if(det != null)
			det.logEvent(stamp, duration, headway, speed);
	}

	/** Controller firmware version */
	protected transient String version;

	/** Set the controller firmware version */
	public void setVersion(String v) { version = v; }

	/** Get the controller firmware version */
	public String getVersion() { return version; }

	/** Add an operation to be performed */
	public void addOperation(ControllerOperation o) {
		CommunicationLineImpl line =
			(CommunicationLineImpl)circuit.getLine();
		line.addOperation(o);
	}

	/** Flag if counters have been initialized */
	protected transient boolean init_count;

	/** Poll/fail counter array */
	protected transient int[][] counters;

	/** Get the counter array */
	public int[][] getCounters() {
		synchronized(counters) {
			if(!init_count) {
				updateNowCounters();
				init_count = true;
			}
			return counters;
		}
	}

	/** Increment a counter */
	public final void incrementCounter(int counter) {
		synchronized(counters) {
			for(int p = 0; p < PERIODS.length; p++) {
				if(p != PERIOD_NOW)
					counters[counter][p]++;
			}
		}
	}

	/** Reset all counters for one time period */
	public final void resetPeriod(int period) {
		synchronized(counters) {
			for(int t = 0; t < TYPES.length; t++)
				counters[t][period] = 0;
		}
	}

	/** Update the counters for the 'now' period */
	protected final void updateNowCounters() {
		synchronized(counters) {
			if(isActive() && !isFailed())
				counters[TYPE_GOOD][PERIOD_NOW] = 1;
			else
				counters[TYPE_GOOD][PERIOD_NOW] = 0;
			if(isActive() && isFailed())
				counters[TYPE_FAIL][PERIOD_NOW] = 1;
			else
				counters[TYPE_FAIL][PERIOD_NOW] = 0;
		}
	}

	/** Controller communication status */
	protected transient String status = UNKNOWN;

	/** Get the controller communication status */
	public String getStatus() { return status; }

	/** Controller setup configuration state */
	protected transient String setup = UNKNOWN;

	/** Get the controller setup configuration state */
	public String getSetup() { return setup; }

	/** Set the controller setup configuration state */
	public void setSetup(String s) {
		if(s == null)
			setup = UNKNOWN;
		else
			setup = s;
	}

	/** Log an exception */
	public void logException(String id, String message) {
		CommunicationLineImpl line =
			(CommunicationLineImpl)circuit.getLine();
		status = message;
		if(!isFailed()) {
			CommunicationLineEvent e = new CommunicationLineEvent();
			e.setEventDescription(
				CommunicationLineEvent.COMM_ERROR);
			e.setEventCalendar(Calendar.getInstance());
			e.setEventRemarks(status);
			e.setDeviceId(id);
			e.setLine(line.getIndex());
			e.setDrop(drop);
			try{ eventLog.add(e); }
			catch(TMSException tmse) {
				tmse.printStackTrace();
			}
		}
	}

	/** Error counter for this controller */
	protected transient int errorCounter = RETRY_THRESHOLD;

	/** Get the failure status */
	public boolean isFailed() {
		return errorCounter >= RETRY_THRESHOLD;
	}

	/** Time stamp of most recent comm failure */
	protected transient Date failTime = new Date();

	/** Get the time stamp of the most recent comm failure */
	public Date getFailTime() { return failTime; }

	/** Get the number of milliseconds the controller has been failed */
	public long getFailMillis() {
		if(isFailed())
			return System.currentTimeMillis() - failTime.getTime();
		else
			return 0;
	}

	/** Log a FAILURE class message */
	protected final void logFailMessage( String message, String id ) {
		CommunicationLineImpl line =
			(CommunicationLineImpl)circuit.getLine();
		CommunicationLineEvent event = new CommunicationLineEvent();
//		event.setEventDescription( Log.DESCRIPTION_COMM_FAILED );
		event.setEventDescription( message );
		event.setEventCalendar( Calendar.getInstance() );
		event.setEventRemarks( "" );
		event.setDeviceId( id );
		event.setLine(line.getIndex());
		event.setDrop( drop );
		try{ eventLog.add( event ); }
		catch(TMSException tmse) {
			tmse.printStackTrace();
		};
		updateNowCounters();
	}

	/** Should a poll be tried again? This method is called after 
	  * a failure and bumps the error counter.
	  * @param id The ID of the device that failed.
	  * @return true if the operation should be retried. 
	  */
	public final boolean retry( String id ) {
		if(isFailed())
			return false;
		errorCounter++;
		if(isFailed()) {
			failTime = new Date();
			logFailMessage("Comm FAILED", id);
			return false;
		}
		return true;
	}

	/** Reset the error counter */
	public final void resetErrorCounter( String id ) {
		boolean failed = isFailed();
		status = "OK";
		errorCounter = 0;
		if(failed)
			logFailMessage("Comm RESTORED", id);
	}

	/** Get the message poller */
	public MessagePoller getPoller() {
		CommunicationLineImpl line =
			(CommunicationLineImpl)circuit.getLine();
		return line.getPoller();
	}

	/** Perform a controller download */
	public void download(boolean reset) {
		MessagePoller p = getPoller();
		if(p != null)
			p.download(this, reset);
	}

	/** Diagnostic operation for this controller */
	protected transient DiagnosticOperation test;

	/** Test the communications to this controller */
	public synchronized void testCommunications(boolean on_off) {
		MessagePoller p = getPoller();
		if(on_off) {
			if(test == null) {
				if(p != null)
					test = p.startTest(this);
			} else
				test.keepTesting();
		} else if(test != null) {
			test.stopTesting();
			test = null;
		}
	}
}
