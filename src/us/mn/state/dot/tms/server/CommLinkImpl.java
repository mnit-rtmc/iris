/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import java.io.Writer;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.TMSException;
import static us.mn.state.dot.tms.server.XmlWriter.createAttribute;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.DevicePollerFactory;
import us.mn.state.dot.tms.units.Interval;

/**
 * The CommLinkImpl class represents a single communication link which is
 * connected with one or more field device controllers.
 *
 * @see us.mn.state.dot.tms.CommProtocol
 * @author Douglas Lau
 */
public class CommLinkImpl extends BaseObjectImpl implements CommLink {

	/** Poller scheduler for repeating jobs */
	static private final Scheduler POLLER = new Scheduler("poller");

	/** Test if a comm protocol supports gate arm control */
	static private boolean isGateArm(CommProtocol cp) {
		return cp == CommProtocol.HYSECURITY_STC;
	}

	/** Load all the comm links */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, CommLinkImpl.class);
		store.query("SELECT name, description, uri, protocol, " +
			"poll_enabled, poll_period, timeout FROM iris." +
			SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new CommLinkImpl(
					row.getString(1),	// name
					row.getString(2),	// description
					row.getString(3),	// uri
					row.getShort(4),	// protocol
					row.getBoolean(5),	// poll_enabled
					row.getInt(6),		// poll_period
					row.getInt(7)		// timeout
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("description", description);
		map.put("uri", uri);
		map.put("protocol", (short)protocol.ordinal());
		map.put("poll_enabled", poll_enabled);
		map.put("poll_period", poll_period);
		map.put("timeout", timeout);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new comm link */
	public CommLinkImpl(String n) {
		super(n);
	}

	/** Create a new comm link */
	public CommLinkImpl(String n, String d, String u, short p, boolean pe,
		int pp, int t)
	{
		super(n);
		description = d;
		uri = u;
		CommProtocol cp = CommProtocol.fromOrdinal(p);
		if (cp != null)
			protocol = cp;
		poll_enabled = pe;
		poll_period = pp;
		timeout = t;
		poller = null;
		initTransients();
	}

	/** Initialize the transient fields */
	@Override
	protected void initTransients() {
		createPollJob(poll_period);
	}

	/** Polling job */
	private transient PollJob poll_job;

	/** Create a new polling job */
	private void createPollJob(int s) {
		PollJob pj = poll_job;
		if (pj != null)
			POLLER.removeJob(pj);
		poll_job = new PollJob(s);
		POLLER.addJob(poll_job);
	}

	/** Job for polling a comm link */
	private class PollJob extends Job {
		private PollJob(int s) {
			super(Calendar.SECOND, s);
		}
		public void perform() {
			pollControllers();
		}
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		closePoller();
		super.doDestroy();
	}

	/** Description of communication link */
	protected String description = "<New Link>";

	/** Set text description */
	@Override
	public void setDescription(String d) {
		description = d;
	}

	/** Set text description */
	public void doSetDescription(String d) throws TMSException {
		if (d.equals(description))
			return;
		store.update(this, "description", d);
		setDescription(d);
	}

	/** Get text description */
	@Override
	public String getDescription() {
		return description;
	}

	/** Test whether gate arm system should be disabled.
	 * @param name Object name.
	 * @param reason Reason for disabling. */
	public void testGateArmDisable(String name, String reason) {
		if (isGateArm(protocol))
			GateArmSystem.disable(name, reason);
	}

	/** Remote URI for link */
	protected String uri = "";

	/** Set remote URI for link */
	@Override
	public void setUri(String u) {
		testGateArmDisable(name, "URI");
		uri = u;
	}

	/** Set remote URI for link */
	public void doSetUri(String u) throws TMSException {
		if (u.equals(uri))
			return;
		store.update(this, "uri", u);
		setUri(u);
		closePoller();
		setStatusNotify(Constants.UNKNOWN);
	}

	/** Get remote URI for link */
	@Override
	public String getUri() {
		return uri;
	}

	/** Communication protocol */
	private CommProtocol protocol = CommProtocol.NTCIP_C;

	/** Set the communication protocol */
	@Override
	public void setProtocol(short p) {
		testGateArmDisable(name, "protocol 0");
		CommProtocol cp = CommProtocol.fromOrdinal(p);
		if (isGateArm(cp))
			GateArmSystem.disable(name, "protocol 1");
		if (cp != null)
			protocol = cp;
	}

	/** Set the communication protocol */
	public void doSetProtocol(short p) throws TMSException {
		CommProtocol cp = CommProtocol.fromOrdinal(p);
		if (cp == null)
			throw new ChangeVetoException("Invalid protocol: " + p);
		if (cp == protocol)
			return;
		store.update(this, "protocol", p);
		setProtocol(p);
		closePoller();
		setStatusNotify(Constants.UNKNOWN);
	}

	/** Get the communication protocol */
	@Override
	public short getProtocol() {
		return (short)protocol.ordinal();
	}

	/** Poll enabled/disabled flag */
	private boolean poll_enabled;

	/** Enable or disable polling */
	@Override
	public void setPollEnabled(boolean e) {
		testGateArmDisable(name, "poll_enabled");
		poll_enabled = e;
	}

	/** Set the poll enabled/disabled flag */
	public void doSetPollEnabled(boolean e) throws TMSException {
		if (e == poll_enabled)
			return;
		store.update(this, "poll_enabled", e);
		setPollEnabled(e);
		closePoller();
		setStatusNotify(Constants.UNKNOWN);
	}

	/** Get polling enabled/disabled flag */
	@Override
	public boolean getPollEnabled() {
		return poll_enabled;
	}

	/** Polling period (seconds) */
	private int poll_period = 30;

	/** Set poll period (seconds) */
	@Override
	public void setPollPeriod(int s) {
		testGateArmDisable(name, "poll_period");
		poll_period = s;
		createPollJob(s);
	}

	/** Check for valid polling period */
	private void checkPeriod(int s) throws TMSException {
		Interval p = new Interval(s);
		for (Interval per: VALID_PERIODS) {
			if (per.equals(p))
				return;
		}
		throw new ChangeVetoException("Invalid period: " + s);
	}

	/** Set the polling period (seconds) */
	public void doSetPollPeriod(int s) throws TMSException {
		if (s == poll_period)
			return;
		checkPeriod(s);
		store.update(this, "poll_period", s);
		setPollPeriod(s);
	}

	/** Get poll period (seconds) */
	@Override
	public int getPollPeriod() {
		return poll_period;
	}

	/** Polling timeout (milliseconds) */
	protected int timeout = 750;

	/** Set the polling timeout (milliseconds) */
	@Override
	public void setTimeout(int t) {
		testGateArmDisable(name, "timeout");
		timeout = t;
	}

	/** Set the polling timeout (milliseconds) */
	public void doSetTimeout(int t) throws TMSException {
		if (t == timeout)
			return;
		if (t < 0 || t > MAX_TIMEOUT_MS)
			throw new ChangeVetoException("Bad timeout: " + t);
		try {
			DevicePoller dp = poller;
			if (dp != null)
				dp.setTimeout(t);
		}
		catch (IOException e) {
			throw new TMSException(e);
		}
		store.update(this, "timeout", t);
		setTimeout(t);
	}

	/** Get the polling timeout (milliseconds) */
	@Override
	public int getTimeout() {
		return timeout;
	}

	/** Device poller for communication */
	private transient DevicePoller poller;

	/** Get the device poller.  This must be synchronized to protect
	 * access to the poller member variable. */
	public synchronized DevicePoller getPoller() {
		if (poller != null) {
			setStatusNotify(poller.getStatus());
			if (poller.isReady())
				return poller;
			else
				closePoller();
		}
		return poll_enabled ? openPoller() : null;
	}

	/** Open the device poller.  Poller must be null prior to calling. */
	private synchronized DevicePoller openPoller() {
		assert poller == null;
		try {
			poller = DevicePollerFactory.create(name, protocol,uri);
			poller.setTimeout(timeout);
		}
		catch (IOException e) {
			closePoller();
			setStatusNotify("I/O error: " + e.getMessage());
		}
		return poller;
	}

	/** Close the message poller */
	private synchronized void closePoller() {
		if (poller != null && !poller.wasHungUp()) {
			failControllers();
			poller.destroy();
		}
		poller = null;
	}

	/** Set all controllers to a failed status */
	private synchronized void failControllers() {
		for (ControllerImpl c: controllers.values())
			c.setFailed(true);
	}

	/** Poll all controllers */
	private synchronized void pollControllers() {
		for (ControllerImpl c: controllers.values()) {
			if (c.isActive())
				c.pollDevices();
		}
	}

	/** Communication link status */
	private transient String status = Constants.UNKNOWN;

	/** Set the communication status */
	private void setStatusNotify(String s) {
		if (s == null || s.equals(status))
			return;
		status = s;
		notifyAttribute("status");
	}

	/** Get the communication status */
	@Override
	public String getStatus() {
		return status;
	}

	/** Field device controllers */
	private transient final TreeMap<Integer, ControllerImpl> controllers =
		new TreeMap<Integer, ControllerImpl>();

	/** Put a controller on the link */
	public synchronized void putController(int d, ControllerImpl c)
		throws ChangeVetoException
	{
		if (controllers.containsKey(d))
			throw new ChangeVetoException("Drop " + d + " exists");
		controllers.put(d, c);
	}

	/** Pull a controller from the link */
	public synchronized void pullController(ControllerImpl c) {
		Integer d = new Integer(c.getDrop());
		controllers.remove(d);
	}

	/** Check if a modem is required for the link */
	public boolean isModemLink() {
		return uri.startsWith("modem:");
	}

	/** Check if the comm link is currently connected */
	public boolean isConnected() {
		DevicePoller dp = poller;
		return (dp != null) && dp.isConnected();
	}

	/** Write the comm link as an XML element */
	public void writeXml(Writer w) throws IOException {
		w.write("<commlink");
		w.write(createAttribute("name", getName()));
		w.write(createAttribute("description", getDescription()));
		w.write(createAttribute("protocol", protocol.toString()));
		w.write("/>\n");
	}
}
