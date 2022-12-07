/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2022  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.AlertConfig;
import us.mn.state.dot.tms.AlertMessage;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.TMSException;

/**
 * Alert message from a configuration
 *
 * @author Douglas Lau
 */
public class AlertMessageImpl extends BaseObjectImpl implements AlertMessage {

	/** Load all the alert messages */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, AlertMessageImpl.class);
		store.query("SELECT name, alert_config, alert_period, " +
			"msg_pattern FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new AlertMessageImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("alert_config", alert_config);
		map.put("alert_period", alert_period);
		map.put("msg_pattern", msg_pattern);
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

	/** Create a new alert message */
	public AlertMessageImpl(String n) {
		super(n);
	}

	/** Create a new alert message */
	private AlertMessageImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // alert_config
		     row.getInt(3),     // alert_period
		     row.getString(4)   // msg_pattern
		);
	}

	/** Create an alert message */
	private AlertMessageImpl(String n, String ac, int ap, String pat) {
		this(n, lookupAlertConfig(ac), ap, lookupMsgPattern(pat));
	}

	/** Create an alert message */
	public AlertMessageImpl(String n, AlertConfig ac, int ap,
		MsgPattern pat)
	{
		this(n);
		alert_config = ac;
		alert_period = ap;
		msg_pattern = pat;
	}

	/** Alert config */
	private AlertConfig alert_config;

	/** Get the alert configuration */
	@Override
	public AlertConfig getAlertConfig() {
		return alert_config;
	}

	/** Alert period (ordinal of AlertPeriod enum) */
	private int alert_period;

	/** Set the alert period (ordinal of AlertPeriod enum) */
	@Override
	public void setAlertPeriod(int ap) {
		alert_period = ap;
	}

	/** Set the alert period (ordinal of AlertPeriod enum) */
	public void doSetAlertPeriod(int ap) throws TMSException {
		if (ap != alert_period) {
			store.update(this, "alert_period", ap);
			setAlertPeriod(ap);
		}
	}

	/** Get the alert period (ordinal of AlertPeriod enum) */
	@Override
	public int getAlertPeriod() {
		return alert_period;
	}

	/** Message to send when action happens */
	private MsgPattern msg_pattern;

	/** Set the message pattern */
	@Override
	public void setMsgPattern(MsgPattern pat) {
		msg_pattern = pat;
	}

	/** Set the message pattern */
	public void doSetMsgPattern(MsgPattern pat) throws TMSException {
		if (pat != msg_pattern) {
			store.update(this, "msg_pattern", pat);
			setMsgPattern(pat);
		}
	}

	/** Get the message pattern */
	@Override
	public MsgPattern getMsgPattern() {
		return msg_pattern;
	}
}
