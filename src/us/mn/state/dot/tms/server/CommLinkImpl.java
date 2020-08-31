/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2020  Minnesota Department of Transportation
 * Copyright (C) 2015-2017  SRF Consulting Group
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
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.CommConfig;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.CommLinkHelper;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.TMSException;
import static us.mn.state.dot.tms.server.XmlWriter.createAttribute;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.DevicePollerFactory;

/**
 * The CommLinkImpl class represents a single communication link which is
 * connected with one or more field device controllers.
 *
 * @author Douglas Lau
 * @author John L. Stanley
 */
public class CommLinkImpl extends BaseObjectImpl implements CommLink {

	/** Seconds to offset each poll from start of interval */
	static private final int OFFSET_SECS = 2;

	/** Poller scheduler for repeating jobs */
	static private final Scheduler POLLER = new Scheduler("poller");

	/** Load all the comm links */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, CommLinkImpl.class);
		store.query("SELECT name, description, uri, poll_enabled, " +
			"comm_config FROM iris." + SONAR_TYPE  + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new CommLinkImpl(row));
			}
		});
	}

	/** Recreate pollers for all links with a comm config */
	static void recreatePollers(CommConfig cc) {
		Iterator<CommLink> it = CommLinkHelper.iterator();
		while (it.hasNext()) {
			CommLink cl = it.next();
			if (cl.getCommConfig() == cc &&
			    cl instanceof CommLinkImpl)
			{
				CommLinkImpl link = (CommLinkImpl) cl;
				link.recreatePoller();
			}
		}
	}

	/** Recreate poll jobs for all links with a comm config */
	static void recreatePollJobs(CommConfig cc) {
		int p = cc.getPollPeriodSec();
		int lp = cc.getLongPollPeriodSec();
		Iterator<CommLink> it = CommLinkHelper.iterator();
		while (it.hasNext()) {
			CommLink cl = it.next();
			if (cl.getCommConfig() == cc &&
			    cl instanceof CommLinkImpl)
			{
				CommLinkImpl link = (CommLinkImpl) cl;
				link.createPollJobs(p, lp);
			}
		}
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("description", description);
		map.put("uri", uri);
		map.put("poll_enabled", poll_enabled);
		map.put("comm_config", comm_config);
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

	/** Create a comm link */
	private CommLinkImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // description
		     row.getString(3),  // uri
		     row.getBoolean(4), // poll_enabled
		     row.getString(5)   // comm_config
		);
	}

	/** Create a comm link */
	private CommLinkImpl(String n, String d, String u, boolean pe,
		String cc)
	{
		super(n);
		description = d;
		uri = u;
		poll_enabled = pe;
		comm_config = lookupCommConfig(cc);
		recreatePoller();
		initTransients();
	}

	/** Initialize the transient fields */
	@Override
	protected void initTransients() {
		createPollJobs(
			comm_config.getPollPeriodSec(),
			comm_config.getLongPollPeriodSec()
		);
	}

	/** Polling job */
	private transient PollJob poll_job;

	/** Long polling job */
	private transient PollJob long_poll_job;

	/** Destroy existing poll jobs */
	private void destroyPollJobs() {
		POLLER.removeJob(poll_job);
		POLLER.removeJob(long_poll_job);
	}

	/** Create a new polling job */
	private synchronized void createPollJobs(int p, int lp) {
		destroyPollJobs();
		poll_job = new PollJob(p, OFFSET_SECS, false);
		POLLER.addJob(poll_job);
		long_poll_job = new PollJob(lp, OFFSET_SECS + 2, true);
		POLLER.addJob(long_poll_job);
	}

	/** Job for polling a comm link */
	private class PollJob extends Job {
		private final int period;
		private final boolean is_long;
		private PollJob(int p, int o, boolean lng) {
			super(Calendar.SECOND, p, Calendar.SECOND, o);
			period = p;
			is_long = lng;
		}
		@Override public void perform() {
			if (poll_enabled)
				pollControllers(period, is_long);
		}
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		destroyPollJobs();
		destroyPoller();
		super.doDestroy();
	}

	/** Description of communication link */
	private String description = "<New Link>";

	/** Set text description */
	@Override
	public void setDescription(String d) {
		description = d;
	}

	/** Set text description */
	public void doSetDescription(String d) throws TMSException {
		if (!objectEquals(d, description)) {
			store.update(this, "description", d);
			setDescription(d);
		}
	}

	/** Get text description */
	@Override
	public String getDescription() {
		return description;
	}

	/** Check if link is configured for a dial-up modem */
	private boolean isDialUpModem() {
		return uri.startsWith("modem:");
	}

	/** Check if dial-up is required to communicate */
	public boolean isDialUpRequired() {
		return isDialUpModem() && !isConnected();
	}

	/** Remote URI for link */
	private String uri = "";

	/** Set remote URI for link */
	@Override
	public void setUri(String u) {
		testGateArmDisable(name, "set URI");
		uri = u;
	}

	/** Set remote URI for link */
	public void doSetUri(String u) throws TMSException {
		if (!objectEquals(u, uri)) {
			store.update(this, "uri", u);
			setUri(u);
			recreatePoller();
		}
	}

	/** Get remote URI for link */
	@Override
	public String getUri() {
		return uri;
	}

	/** Poll enabled/disabled flag */
	private boolean poll_enabled;

	/** Enable or disable polling */
	@Override
	public void setPollEnabled(boolean e) {
		testGateArmDisable(name, "set poll_enabled");
		poll_enabled = e;
	}

	/** Set the poll enabled/disabled flag */
	public void doSetPollEnabled(boolean e) throws TMSException {
		if (e != poll_enabled) {
			store.update(this, "poll_enabled", e);
			setPollEnabled(e);
			recreatePoller();
		}
	}

	/** Get polling enabled/disabled flag */
	@Override
	public boolean getPollEnabled() {
		return poll_enabled;
	}

	/** Comm configuration */
	private CommConfigImpl comm_config;

	/** Set the comm configuration */
	@Override
	public void setCommConfig(CommConfig cc) {
		if (cc instanceof CommConfigImpl) {
			testGateArmDisable(name, "set comm_config");
			comm_config = (CommConfigImpl) cc;
		}
	}

	/** Set the comm configuration */
	public void doSetCommConfig(CommConfig cc) throws TMSException {
		if (cc != comm_config) {
			store.update(this, "comm_config", cc);
			setCommConfig(cc);
			recreatePoller();
		}
	}

	/** Get the comm configuration */
	@Override
	public CommConfig getCommConfig() {
		return comm_config;
	}

	/** Device poller */
	private transient DevicePoller poller;

	/** Get the device poller */
	public synchronized DevicePoller getPoller() {
		if (poll_enabled) {
			updateStatus();
			return poller;
		}
		return null;
	}

	/** Recreate the device poller */
	private synchronized void recreatePoller() {
		destroyPoller();
		if (poll_enabled)
			createPoller();
		updateStatus();
	}

	/** Destroy the device poller */
	private synchronized void destroyPoller() {
		if (poller != null) {
			poller.destroy();
			poller = null;
		}
	}

	/** Create the device poller */
	private synchronized void createPoller() {
		poller = DevicePollerFactory.create(this);
	}

	/** Set all controllers to a failed status */
	private synchronized void failControllers() {
		for (ControllerImpl c: controllers.values())
			c.setFailed(true);
	}

	/** Poll all controllers */
	private synchronized void pollControllers(int period, boolean is_long) {
		for (ControllerImpl c: controllers.values())
			c.pollDevices(period, is_long);
	}

	/** Communication link status */
	private transient String status = Constants.UNKNOWN;

	/** Update the comm link status */
	private void updateStatus() {
		DevicePoller dp = poller;
		setStatusNotify((dp != null)
			? dp.getStatus()
			: Constants.UNKNOWN);
	}

	/** Set the communication status */
	private void setStatusNotify(String s) {
		assert s != null;
		if (!s.equals(status)) {
			status = s;
			notifyAttribute("status");
			if (!s.isEmpty())
				failControllers();
		}
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
		w.write(createAttribute("protocol",
			getCommProtocol().toString()));
		w.write("/>\n");
	}

	/** Get the communication protocol */
	public CommProtocol getCommProtocol() {
		CommProtocol cp = CommProtocol.fromOrdinal(
			comm_config.getProtocol());
		return (cp != null) ? cp : CommProtocol.NTCIP_C;
	}

	/** Test whether gate arm system should be disabled.
	 * @param name Object name.
	 * @param reason Reason for disabling. */
	public void testGateArmDisable(String name, String reason) {
		comm_config.testGateArmDisable(name, reason);
	}

	/** Get poll period (seconds) */
	public int getPollPeriodSec() {
		return comm_config.getPollPeriodSec();
	}
}
