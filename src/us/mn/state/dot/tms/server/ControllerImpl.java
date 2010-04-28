/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Cabinet;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Constants;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.SamplePoller;
import us.mn.state.dot.tms.server.event.CommEvent;

/**
 * A controller represents a field device controller.
 *
 * @author Douglas Lau
 */
public class ControllerImpl extends BaseObjectImpl implements Controller {

	/** Load all the controllers */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading controllers...");
		namespace.registerType(SONAR_TYPE, ControllerImpl.class);
		store.query("SELECT name, cabinet, comm_link, drop_id, " +
			"active, password, notes FROM iris." + SONAR_TYPE  +";",
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
					row.getString(6),	// password
					row.getString(7)	// notes
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
		map.put("password", password);
		map.put("notes", notes);
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

	/** Create a new controller */
	public ControllerImpl(String n) throws TMSException, SonarException {
		super(n);
		CabinetImpl c = new CabinetImpl(n);
		MainServer.server.createObject(c);
		cabinet = c;
	}

	/** Create a new controller */
	protected ControllerImpl(String n, Cabinet c, CommLink l, short d,
		boolean a, String p, String nt) throws TMSException
	{
		super(n);
		cabinet = c;
		comm_link = l;
		drop_id = d;
		active = a;
		password = p;
		notes = nt;
		initTransients();
	}

	/** Create a new controller */
	protected ControllerImpl(Namespace ns, String n, String c, String l,
		short d, boolean a, String p, String nt) throws TMSException
	{
		this(n, (Cabinet)ns.lookupObject(Cabinet.SONAR_TYPE, c),
			(CommLink)ns.lookupObject(CommLink.SONAR_TYPE, l),
			d, a, p, nt);
	}

	/** Initialize the transient fields */
	protected void initTransients() throws TMSException {
		version = "";
		failed = true;
		if(comm_link instanceof CommLinkImpl) {
			CommLinkImpl link = (CommLinkImpl)comm_link;
			link.putController(drop_id, this);
		}
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
		store.update(this, "cabinet", c);
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
		store.update(this, "comm_link", c);
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
	}

	/** Get the active status */
	public boolean getActive() {
		return active;
	}

	/** Access password */
	protected String password;

	/** Set the access password */
	public void setPassword(String pwd) {
		password = pwd;
	}

	/** Set the access password */
	public void doSetPassword(String pwd) throws TMSException {
		if(pwd.length() == 0)
			pwd =  null;
		if(pwd == null) {
			if(password == null)
				return;
		} else {
			if(pwd.equals(password))
				return;
		}
		store.update(this, "password", pwd);
		setPassword(pwd);
	}

	/** Get the access password */
	public String getPassword() {
		return password;
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

	/** Get controller I/O for one pin */
	public synchronized ControllerIO getIO(int pin) {
		return io_pins.get(pin);
	}

	/** Assign an IO to the specified controller I/O pin */
	public synchronized void setIO(int pin, ControllerIO io) {
		if(io != null)
			io_pins.put(pin, io);
		else
			io_pins.remove(pin);
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
	public DetectorImpl getDetectorAtPin(int pin) {
		ControllerIO io = getIO(pin);
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
	public AlarmImpl getAlarm(int pin) {
		ControllerIO io = getIO(pin);
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

	/** Bin 30-second sample data */
	public synchronized void binEventSamples() {
		for(ControllerIO io: io_pins.values()) {
			if(io instanceof DetectorImpl)
				((DetectorImpl)io).binEventSamples();
		}
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

	/** Log a comm event */
	public void logCommEvent(EventType et, String id, String message) {
		incrementCommCounter(et);
		setStatus(message);
		if(!isFailed())
			logCommEvent(et, id);
	}

	/** Failed status of controller */
	protected transient boolean failed = true;

	/** Set the failed status of the controller */
	protected void setFailed(boolean f, String id) {
		if(f == failed)
			return;
		if(f) {
			failTime = new Date();
			logCommEvent(EventType.COMM_FAILED, id);
		} else
			logCommEvent(EventType.COMM_RESTORED, id);
		failed = f;
		notifyAttribute("status");
		notifyAttribute("error");
	}

	/** Set the failed status of the controller */
	public void setFailed(boolean f) {
		setFailed(f, null);
	}

	/** Get the failure status */
	public boolean isFailed() {
		return failed;
	}

	/** Controller maint status */
	protected transient String maint = "";

	/** Set the controller maint status */
	public void setMaint(String s) {
		if(s == null)
			maint = "";
		else
			maint = s;
		notifyAttribute("maint");
	}

	/** Get the controller maint status */
	public String getMaint() {
		return maint;
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

	/** Timeout error count */
	protected int timeoutErr;

	/** Get the timeout error count */
	public int getTimeoutErr() {
		return timeoutErr;
	}

	/** Increment the timeout error count */
	protected void incrementTimeoutErr() {
		timeoutErr++;
		notifyAttribute("timeoutErr");
	}

	/** Checksum error count */
	protected int checksumErr;

	/** Get the checksum error count */
	public int getChecksumErr() {
		return checksumErr;
	}

	/** Increment the checksum error count */
	protected void incrementChecksumErr() {
		checksumErr++;
		notifyAttribute("checksumErr");
	}

	/** Parsing error count */
	protected int parsingErr;

	/** Get the parsing error count */
	public int getParsingErr() {
		return parsingErr;
	}

	/** Increment the parsing error count */
	protected void incrementParsingErr() {
		parsingErr++;
		notifyAttribute("parsingErr");
	}

	/** Controller error count */
	protected int controllerErr;

	/** Get the controller error count */
	public int getControllerErr() {
		return controllerErr;
	}

	/** Increment the controller error count */
	protected void incrementControllerErr() {
		controllerErr++;
		notifyAttribute("controllerErr");
	}

	/** Increment a comm error counter */
	protected void incrementCommCounter(EventType et) {
		switch(et) {
		case POLL_TIMEOUT_ERROR:
			incrementTimeoutErr();
			break;
		case CHECKSUM_ERROR:
			incrementChecksumErr();
			break;
		case PARSING_ERROR:
			incrementParsingErr();
			break;
		case CONTROLLER_ERROR:
			incrementControllerErr();
			break;
		}
	}

	/** Successful operations count */
	protected int successOps;

	/** Get the successful operation count */
	public int getSuccessOps() {
		return successOps;
	}

	/** Increment the successful operation count */
	protected void incrementSuccessOps() {
		successOps++;
		notifyAttribute("successOps");
	}

	/** Failed operations count */
	protected int failedOps;

	/** Get the failed operation count */
	public int getFailedOps() {
		return failedOps;
	}

	/** Increment the failed operation count */
	protected void incrementFailedOps() {
		failedOps++;
		notifyAttribute("failedOps");
	}

	/** Clear the counters and error status */
	public void setCounters(boolean clear) {
		setMaint("");
		setError("");
		timeoutErr = 0;
		notifyAttribute("timeoutErr");
		checksumErr = 0;
		notifyAttribute("checksumErr");
		parsingErr = 0;
		notifyAttribute("parsingErr");
		controllerErr = 0;
		notifyAttribute("controllerErr");
		successOps = 0;
		notifyAttribute("successOps");
		failedOps = 0;
		notifyAttribute("failedOps");
	}

	/** Log a comm event */
	protected final void logCommEvent(EventType event, String id) {
		CommEvent ev = new CommEvent(event, getName(), id);
		try {
			ev.doStore();
		}
		catch(TMSException e) {
			e.printStackTrace();
		};
	}

	/** Complete a controller operation */
	public void completeOperation(String id, boolean success) {
		if(success)
			incrementSuccessOps();
		else
			incrementFailedOps();
		setFailed(!success, id);
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
