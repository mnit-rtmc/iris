/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  Minnesota Department of Transportation
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.CommConfig;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.units.Interval;

/**
 * A CommConfig represents a configuration which can be used for one or more
 * CommLinks.
 *
 * @see us.mn.state.dot.tms.CommProtocol
 * @author Douglas Lau
 */
public class CommConfigImpl extends BaseObjectImpl implements CommConfig {

	/** Load all the comm configs */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, CommConfigImpl.class);
		store.query("SELECT name, description, protocol, modem, " +
			"timeout_ms, poll_period_sec, long_poll_period_sec, " +
			"idle_disconnect_sec, no_response_disconnect_sec " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new CommConfigImpl(row));
			}
		});
	}

	/** Check for valid polling period */
	static private void checkPeriod(int s) throws TMSException {
		Interval p = new Interval(s);
		for (Interval per: VALID_PERIODS) {
			if (per.equals(p))
				return;
		}
		throw new ChangeVetoException("Invalid period: " + s);
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("description", description);
		map.put("protocol", (short) protocol.ordinal());
		map.put("modem", modem);
		map.put("timeout_ms", timeout_ms);
		map.put("poll_period_sec", poll_period_sec);
		map.put("long_poll_period_sec", long_poll_period_sec);
		map.put("idle_disconnect_sec", idle_disconnect_sec);
		map.put("no_response_disconnect_sec",
			no_response_disconnect_sec);
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

	/** Create a new comm config */
	public CommConfigImpl(String n) {
		super(n);
	}

	/** Create a comm config */
	private CommConfigImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // description
		     row.getShort(3),   // protocol
		     row.getBoolean(4), // modem
		     row.getInt(5),     // timeout_ms
		     row.getInt(6),     // poll_period_sec
		     row.getInt(7),     // long_poll_period_sec
		     row.getInt(8),     // idle_disconnect_sec
		     row.getInt(9)      // no_response_disconnect_sec
		);
	}

	/** Create a comm config */
	private CommConfigImpl(String n, String d, short p, boolean m, int t,
		int pp, int lpp, int idsc, int nrdsc)
	{
		super(n);
		description = d;
		CommProtocol cp = CommProtocol.fromOrdinal(p);
		protocol = (cp != null) ? cp : CommProtocol.NTCIP_C;
		modem = m;
		timeout_ms = t;
		poll_period_sec = pp;
		long_poll_period_sec = lpp;
		idle_disconnect_sec = idsc;
		no_response_disconnect_sec = nrdsc;
	}

	/** Test whether gate arm system should be disabled.
	 * @param name Object name.
	 * @param reason Reason for disabling. */
	public void testGateArmDisable(String name, String reason) {
		if (protocol.isGateArm())
			GateArmSystem.disable(name, reason);
	}

	/** Description of communication config */
	private String description = "<new>";

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

	/** Communication protocol */
	private CommProtocol protocol = CommProtocol.NTCIP_C;

	/** Set the communication protocol */
	@Override
	public void setProtocol(short p) {
		testGateArmDisable(name, "set protocol 0");
		CommProtocol cp = CommProtocol.fromOrdinal(p);
		if (cp != null) {
			protocol = cp;
			testGateArmDisable(name, "set protocol 1");
		}
		CommLinkImpl.recreatePollers(this);
	}

	/** Set the communication protocol */
	public void doSetProtocol(short p) throws TMSException {
		CommProtocol cp = CommProtocol.fromOrdinal(p);
		if (cp == null)
			throw new ChangeVetoException("Invalid protocol: " + p);
		if (cp != protocol) {
			store.update(this, "protocol", p);
			setProtocol(p);
		}
	}

	/** Get the communication protocol */
	@Override
	public short getProtocol() {
		return (short) protocol.ordinal();
	}

	/** Modem flag */
	private boolean modem;

	/** Set modem flag */
	@Override
	public void setModem(boolean m) {
		testGateArmDisable(name, "set modem");
		modem = m;
		CommLinkImpl.recreatePollers(this);
	}

	/** Set the modem flag */
	public void doSetModem(boolean m) throws TMSException {
		if (m != modem) {
			store.update(this, "modem", m);
			setModem(m);
		}
	}

	/** Get modem flag */
	@Override
	public boolean getModem() {
		return modem;
	}

	/** Polling timeout (milliseconds) */
	private int timeout_ms = 750;

	/** Set the polling timeout (milliseconds) */
	@Override
	public void setTimeoutMs(int t) {
		testGateArmDisable(name, "set timeout_ms");
		timeout_ms = t;
		CommLinkImpl.recreatePollers(this);
	}

	/** Set the polling timeout (milliseconds) */
	public void doSetTimeoutMs(int t) throws TMSException {
		if (t < 0 || t > MAX_TIMEOUT_MS)
			throw new ChangeVetoException("Bad timeout: " + t);
		if (t != timeout_ms) {
			store.update(this, "timeout_ms", t);
			setTimeoutMs(t);
		}
	}

	/** Get the polling timeout (milliseconds) */
	@Override
	public int getTimeoutMs() {
		return timeout_ms;
	}

	/** Polling period (seconds) */
	private int poll_period_sec = 30;

	/** Set poll period (seconds) */
	@Override
	public void setPollPeriodSec(int s) {
		testGateArmDisable(name, "set poll_period_sec");
		poll_period_sec = s;
		CommLinkImpl.recreatePollJobs(this);
	}

	/** Set the polling period (seconds) */
	public void doSetPollPeriodSec(int s) throws TMSException {
		if (s != poll_period_sec) {
			checkPeriod(s);
			store.update(this, "poll_period_sec", s);
			setPollPeriodSec(s);
		}
	}

	/** Get poll period (seconds) */
	@Override
	public int getPollPeriodSec() {
		return poll_period_sec;
	}

	/** Long polling period (seconds) */
	private int long_poll_period_sec = 30;

	/** Set long poll period (seconds) */
	@Override
	public void setLongPollPeriodSec(int s) {
		testGateArmDisable(name, "set long_poll_period_sec");
		long_poll_period_sec = s;
		CommLinkImpl.recreatePollJobs(this);
	}

	/** Set the long polling period (seconds) */
	public void doSetLongPollPeriodSec(int s) throws TMSException {
		if (s != long_poll_period_sec) {
			checkPeriod(s);
			store.update(this, "long_poll_period_sec", s);
			setLongPollPeriodSec(s);
		}
	}

	/** Get long poll period (seconds) */
	@Override
	public int getLongPollPeriodSec() {
		return long_poll_period_sec;
	}

	/** Idle disconnect (seconds) */
	private int idle_disconnect_sec = 0;

	/** Set idle disconnect (seconds) */
	@Override
	public void setIdleDisconnectSec(int s) {
		testGateArmDisable(name, "set idle_disconnect_sec");
		idle_disconnect_sec = s;
		CommLinkImpl.recreatePollers(this);
	}

	/** Set the idle disconnect (seconds) */
	public void doSetIdleDisconnectSec(int s) throws TMSException {
		if (s != idle_disconnect_sec) {
			store.update(this, "idle_disconnect_sec", s);
			setIdleDisconnectSec(s);
		}
	}

	/** Get idle disconnect (seconds) */
	@Override
	public int getIdleDisconnectSec() {
		return idle_disconnect_sec;
	}

	/** No response disconnect (seconds) */
	private int no_response_disconnect_sec = 0;

	/** Set no responsed disconnect (seconds) */
	@Override
	public void setNoResponseDisconnectSec(int s) {
		testGateArmDisable(name, "set no_response_disconnect_sec");
		no_response_disconnect_sec = s;
		CommLinkImpl.recreatePollers(this);
	}

	/** Set the no responsed disconnect (seconds) */
	public void doSetNoResponseDisconnectSec(int s) throws TMSException {
		if (s != no_response_disconnect_sec) {
			store.update(this, "no_response_disconnect_sec", s);
			setNoResponseDisconnectSec(s);
		}
	}

	/** Get no response disconnect (seconds) */
	@Override
	public int getNoResponseDisconnectSec() {
		return no_response_disconnect_sec;
	}
}
