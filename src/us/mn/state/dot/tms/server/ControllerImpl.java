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
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.SamplePoller;
import us.mn.state.dot.tms.server.event.CommEvent;
import us.mn.state.dot.tms.server.event.EventType;
import us.mn.state.dot.tms.Cabinet;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Constants;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.TMSException;

/**
 * A controller represents a field device controller.
 *
 * @author Douglas Lau
 */
public class ControllerImpl extends BaseObjectImpl implements Controller {

	/** Communication failure retry threshold */
	static public final int RETRY_THRESHOLD = 3;

	/** Load all the controllers */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading controllers...");
		namespace.registerType(SONAR_TYPE, ControllerImpl.class);
		store.query("SELECT name, cabinet, comm_link, drop_id, " +
			"active, notes FROM " + SONAR_TYPE  + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new ControllerImpl(
					namespace,
					row.getString(1),	// name
					row.getString(2),	// cabinet
					row.getString(3),	// comm_link
					row.getShort(4),	// drop_id
					row.getBoolean(5),	// active
					row.getString(6)	// notes
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("cabinet", cabinet);
		map.put("comm_link", comm_link);
		map.put("drop_id", drop_id);
		map.put("active", active);
		map.put("notes", notes);
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

	/** Create a new controller */
	public ControllerImpl(String n) throws TMSException, SonarException {
		super(n);
		CabinetImpl c = new CabinetImpl(n);
		MainServer.server.createObject(c);
		cabinet = c;
	}

	/** Create a new controller */
	protected ControllerImpl(String n, Cabinet c, CommLink l, short d,
		boolean a, String nt) throws TMSException
	{
		super(n);
		cabinet = c;
		comm_link = l;
		drop_id = d;
		active = a;
		notes = nt;
		initTransients();
	}

	/** Create a new controller */
	protected ControllerImpl(Namespace ns, String n, String c, String l,
		short d, boolean a, String nt) throws TMSException
	{
		this(n, (Cabinet)ns.lookupObject(Cabinet.SONAR_TYPE, c),
			(CommLink)ns.lookupObject(CommLink.SONAR_TYPE, l),
			d, a, nt);
	}

	/** Initialize the transient fields */
	protected void initTransients() throws TMSException {
		version = "";
		failed = true;
		errorCounter = RETRY_THRESHOLD;
		if(comm_link instanceof CommLinkImpl) {
			CommLinkImpl link = (CommLinkImpl)comm_link;
			link.putController(drop_id, this);
		}
	}

	/** Create a string representation of the controller */
	public String toString() {
		CommLink l = comm_link;
		return "(" + l.getName() + ":" + drop_id + ")";
	}

	/** Get controller label */
	public String getLabel() {
		CommLink l = comm_link;
		StringBuilder b = new StringBuilder();
		b.append("Link ");
		b.append(l.getName());
		b.append(" drop ");
		b.append(drop_id);
		return b.toString();
	}

	/** Controller cabinet */
	protected Cabinet cabinet;

	/** Set the controller cabinet */
	public void setCabinet(Cabinet c) {
		cabinet = c;
	}

	/** Set the controller cabinet */
	public void doSetCabinet(Cabinet c) throws TMSException {
		if(c == cabinet)
			return;
		if(c != null)
			store.update(this, "cabinet", c.getName());
		else
			store.update(this, "cabinet", null);
		setCabinet(c);
	}

	/** Get the controller cabinet */
	public Cabinet getCabinet() {
		return cabinet;
	}

	/** Put this controller into a comm link */
	protected void putCommLink(int d, CommLink l) throws TMSException {
		CommLinkImpl link = (CommLinkImpl)l;
		if(link != null)
			link.putController(d, this);
	}

	/** Pull this controller from a comm link */
	protected void pullCommLink(CommLink l) {
		CommLinkImpl link = (CommLinkImpl)l;
		if(link != null)
			link.pullController(this);
	}

	/** Comm link */
	protected CommLink comm_link;

	/** Set the comm link for this controller */
	public void setCommLink(CommLink c) {
		comm_link = c;
	}

	/** Set the comm link for this controller */
	public void doSetCommLink(CommLink c) throws TMSException {
		if(c == comm_link)
			return;
		putCommLink(drop_id, c);
		if(c != null)
			store.update(this, "comm_link", c.getName());
		else
			store.update(this, "comm_link", null);
		pullCommLink(comm_link);
		setCommLink(c);
	}

	/** Get the comm link */
	public CommLink getCommLink() {
		return comm_link;
	}

	/** Drop address */
	protected short drop_id;

	/** Set the drop address */
	public void setDrop(short d) {
		drop_id = d;
	}

	/** Set the drop address */
	public void doSetDrop(short d) throws TMSException {
		if(d == drop_id)
			return;
		putCommLink(d, comm_link);
		store.update(this, "drop_id", d);
		pullCommLink(comm_link);
		setDrop(d);
	}

	/** Get the drop address */
	public short getDrop() {
		return drop_id;
	}

	/** Active status flag */
	protected boolean active;

	/** Set the active status */
	public void setActive(boolean a) {
		active = a;
	}

	/** Set the active status */
	public void doSetActive(boolean a) throws TMSException {
		if(a == active)
			return;
		store.update(this, "active", a);
		setActive(a);
		updateNowCounters();
	}

	/** Get the active status */
	public boolean getActive() {
		return active;
	}

	/** Administrator notes for this controller */
	protected String notes = "";

	/** Set the administrator notes */
	public void setNotes(String n) {
		notes = n;
	}

	/** Set the administrator notes */
	public void doSetNotes(String n) throws TMSException {
		if(n.equals(notes))
			return;
		store.update(this, "notes", n);
		setNotes(n);
	}

	/** Get the administrator notes */
	public String getNotes() {
		return notes;
	}

	/** Mapping of all controller I/O pins */
	protected transient HashMap<Integer, ControllerIO> io_pins =
		new HashMap<Integer, ControllerIO>();

	/** Get all controller I/O pins */
	public synchronized ControllerIO[] getIO() {
		ControllerIO[] io = new ControllerIO[ALL_PINS];
		for(int i: io_pins.keySet())
			io[i] = io_pins.get(i);
		return io;
	}

	/** Assign an IO to the specified controller I/O pin */
	public synchronized void setIO(int pin, ControllerIO io)
		throws TMSException
	{
		if(pin < 1 || pin > ALL_PINS)
			throw new ChangeVetoException("Invalid pin: " + pin);
		ControllerIO old_io = io_pins.get(pin);
		if(old_io != null) {
			if(io != null)
				throw new ChangeVetoException(
					"Pin " + pin + " already assigned");
			else
				io_pins.remove(pin);
		}
		if(io != null)
			io_pins.put(pin, io);
	}

	/** Determine whether this controller has an active ramp meter */
	public synchronized boolean hasActiveMeter() {
		if(getActive()) {
			for(ControllerIO io: io_pins.values()) {
				if(io instanceof RampMeterImpl)
					return true;
			}
		}
		return false;
	}

	/** Get an active DMS for the controller */
	public synchronized DMSImpl getActiveSign() {
		if(getActive()) {
			for(ControllerIO io: io_pins.values()) {
				if(io instanceof DMSImpl)
					return (DMSImpl)io;
			}
		}
		return null;
	}

	/** Get an active LCS for the controller */
	public synchronized LCSImpl getActiveLcs() {
		if(getActive()) {
			for(ControllerIO io: io_pins.values()) {
				if(io instanceof LCSImpl)
					return (LCSImpl)io;
			}
		}
		return null;
	}

	/** Get an active warning sign for the controller */
	public synchronized WarningSignImpl getActiveWarningSign() {
		if(getActive()) {
			for(ControllerIO io: io_pins.values()) {
				if(io instanceof WarningSignImpl)
					return (WarningSignImpl)io;
			}
		}
		return null;
	}

	/** Get a detector by its I/O pin number */
	public synchronized DetectorImpl getDetectorAtPin(int pin) {
		ControllerIO io = io_pins.get(pin);
		if(io instanceof DetectorImpl)
			return (DetectorImpl)io;
		else
			return null;
	}

	/** Check whether this controller has any active detectors */
	public synchronized boolean hasActiveDetector() {
		if(getActive()) {
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
	public int getSpeedPair(int pin) {
		DetectorImpl d = getDetectorAtPin(pin);
		if(d != null && d.isVelocity())
			return getSpeedPair(d);
		return 0;
	}

	/** Get an alarm from the controller */
	public synchronized AlarmImpl getAlarm(int pin) {
		ControllerIO io = io_pins.get(pin);
		if(io instanceof AlarmImpl)
			return (AlarmImpl)io;
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
	public void storeData30Second(Calendar stamp, int start_pin,
		int[] volume, int[] scans, int[] speed)
	{
		for(int i = 0; i < volume.length; i++) {
			DetectorImpl det = getDetectorAtPin(start_pin + i);
			if(det != null) {
				det.storeData30Second(stamp, volume[i],
					scans[i]);
				if(speed != null)
					det.storeSpeed30Second(stamp, speed[i]);
			}
		}
	}

	/** Store 5-minute detector data */
	public void storeData5Minute(Calendar stamp, int start_pin,
		int[] volume, int[] scan) throws IOException
	{
		for(int i = 0; i < volume.length; i++) {
			DetectorImpl det = getDetectorAtPin(start_pin + i);
			if(det != null)
				det.storeData5Minute(stamp, volume[i], scan[i]);
		}
	}

	/** Log a vehicle detection event */
	public void logEvent(Calendar stamp, int pin, int duration,
		int headway, int speed)
	{
		DetectorImpl det = getDetectorAtPin(pin);
		if(det != null)
			det.logEvent(stamp, duration, headway, speed);
	}

	/** Controller firmware version */
	protected transient String version;

	/** Set the controller firmware version */
	public void setVersion(String v) {
		version = v;
		notifyAttribute("version");
	}

	/** Get the controller firmware version */
	public String getVersion() {
		return version;
	}

	/** Add an operation to be performed */
	public void addOperation(OpController o) {
		CommLinkImpl link = (CommLinkImpl)comm_link;
		if(link != null)
			link.addOperation(o);
	}

	/** Increment a counter */
	public void incrementCounter(int counter) {
		// FIXME
	}

	/** Reset all counters for one time period */
	public void resetPeriod(int period) {
		// FIXME
	}

	/** Update the counters for the 'now' period */
	protected final void updateNowCounters() {
		// FIXME
	}

	/** Controller communication status */
	protected transient String status = Constants.UNKNOWN;

	/** Get the controller communication status */
	public String getStatus() {
		if(isFailed())
			return status;
		else
			return "";
	}

	/** Set the controller communication status */
	protected void setStatus(String s) {
		// NOTE: the status attribute is set here, but don't notify
		// clients until communication fails. That happens in the
		// setFailed method.
		status = s;
	}

	/** Log an exception */
	public void logException(String id, String message) {
		setStatus(message);
		if(!isFailed()) {
			CommEvent ev = new CommEvent(EventType.COMM_ERROR,
				getName(), id);
			try {
				ev.doStore();
			}
			catch(TMSException e) {
				e.printStackTrace();
			}
		}
	}

	/** Failed status of controller */
	protected transient boolean failed = true;

	/** Set the failed status of the controller */
	protected void setFailed(boolean f, String id) {
		if(f == failed)
			return;
		if(f) {
			failTime = new Date();
			logFailMessage(EventType.COMM_FAILED, id);
		} else
			logFailMessage(EventType.COMM_RESTORED, id);
		failed = f;
		notifyAttribute("status");
		notifyAttribute("error");
	}

	/** Set the failed status of the controller */
	public void setFailed(boolean f) {
		setFailed(f, toString());
	}

	/** Get the failure status */
	public boolean isFailed() {
		return failed;
	}

	/** Time stamp of most recent comm failure */
	protected transient Date failTime = new Date();

	/** Controller error detail */
	protected transient String error = "";

	/** Set the controller error detail */
	public void setError(String s) {
		if(s == null)
			error = "";
		else
			error = s;
		notifyAttribute("error");
	}

	/** Set the controller error detail */
	public void doSetError(String s) {
		if(s == null)
			error = "";
		else
			error = s;
	}

	/** Get the controller error detail */
	public String getError() {
		if(isFailed())
			return "FAIL @ " + failTime.toString();
		else
			return error;
	}

	/** Get the number of milliseconds the controller has been failed */
	public long getFailMillis() {
		if(isFailed())
			return System.currentTimeMillis() - failTime.getTime();
		else
			return 0;
	}

	/** Log a FAILURE class message */
	protected final void logFailMessage(EventType event, String id) {
		CommEvent ev = new CommEvent(event, getName(), id);
		try {
			ev.doStore();
		}
		catch(TMSException e) {
			e.printStackTrace();
		};
		updateNowCounters();
	}

	/** Error counter for this controller */
	protected transient int errorCounter = RETRY_THRESHOLD;

	/** Should a poll be tried again? This method is called after 
	  * a failure and bumps the error counter.
	  * @param id The ID of the device that failed.
	  * @return true if the operation should be retried. 
	  */
	public final boolean retry( String id ) {
		if(isFailed())
			return false;
		errorCounter++;
		if(errorCounter >= RETRY_THRESHOLD)
			setFailed(true, id);
		return !isFailed();
	}

	/** Reset the error counter */
	public void resetErrorCounter(String id) {
		errorCounter = 0;
		setFailed(false, id);
	}

	/** Reset the error counter */
	public void resetErrorCounter() {
		resetErrorCounter(toString());
	}

	/** Complete a controller operation */
	public void completeOperation(String id, boolean success) {
		if(success) {
			resetErrorCounter(id);
			incrementCounter(ErrorCounter.TYPE_GOOD);
		} else
			incrementCounter(ErrorCounter.TYPE_FAIL);
	}

	/** Get the message poller */
	public MessagePoller getPoller() {
		CommLinkImpl link = (CommLinkImpl)comm_link;
		if(link != null)
			return link.getPoller();
		else
			return null;
	}

	/** Perform a controller download (reset) */
	public void setDownload(boolean reset) {
		MessagePoller p = getPoller();
		if(p instanceof SamplePoller) {
			SamplePoller sp = (SamplePoller)p;
			if(reset)
				sp.resetController(this);
			else
				sp.sendSettings(this);
		}
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		if(comm_link instanceof CommLinkImpl) {
			CommLinkImpl link = (CommLinkImpl)comm_link;
			link.pullController(this);
		}
		super.doDestroy();
		MainServer.server.removeObject(cabinet);
	}
}
