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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsMsgPriority;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.TMSException;
import static us.mn.state.dot.tms.server.XmlWriter.createAttribute;

/**
 * A sign message represents a message which can be displayed on a dynamic
 * message sign (DMS). It contains the text associated with the message and a
 * bitmap for each page of the message.
 *
 * @author Douglas Lau
 */
public class SignMessageImpl extends BaseObjectImpl implements SignMessage {

	/** Sign msg debug log */
	static private final DebugLog MSG_LOG = new DebugLog("sign_msg");

	/** Last allocated system message ID */
	static private int last_id = 0;

	/** Create a unique sign message name */
	static private synchronized String createUniqueName() {
		String n = createNextName();
		while (namespace.lookupObject(SONAR_TYPE, n) != null)
			n = createNextName();
		return n;
	}

	/** Create the next system message name */
	static private String createNextName() {
		last_id++;
		// Check if the ID has rolled over to negative numbers
		if (last_id < 0)
			last_id = 0;
		return "system_" + last_id;
	}

	/** Load all the sign messages */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, SignMessageImpl.class);
		store.query("SELECT name, incident, multi, beacon_enabled, " +
			"bitmaps, a_priority, r_priority, source, owner, " +
			"duration FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new SignMessageImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("incident", incident);
		map.put("multi", multi);
		map.put("beacon_enabled", beacon_enabled);
		map.put("bitmaps", bitmaps);
		map.put("a_priority", activationPriority);
		map.put("r_priority", runTimePriority);
		map.put("source", source);
		map.put("owner", owner);
		map.put("duration", duration);
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

	/** Create a new sign message (by SONAR clients) */
	public SignMessageImpl(String n) {
		super(n);
		logMsg("created (client)");
	}

	/** Create a sign message */
	private SignMessageImpl(ResultSet row) throws SQLException {
		this(row.getString(1),		// name
		     row.getString(2),		// incident
		     row.getString(3),		// multi
		     row.getBoolean(4),		// beacon_enabled
		     row.getString(5),		// bitmaps
		     row.getInt(6),		// a_priority
		     row.getInt(7),		// r_priority
		     row.getInt(8),		// source
		     row.getString(9),		// owner
		     (Integer) row.getObject(10) // duration
		);
	}

	/** Create a sign message */
	private SignMessageImpl(String n, String inc, String m, boolean be,
		String b, int ap, int rp, int s, String o, Integer d)
	{
		super(n);
		incident = lookupIncident(inc);
		multi = m;
		beacon_enabled = be;
		bitmaps = b;
		activationPriority = ap;
		runTimePriority = rp;
		source = s;
		owner = o;
		duration = d;		
	}

	/** Create a new sign message (by IRIS) */
	public SignMessageImpl(String m, boolean be, String b,
		DmsMsgPriority ap, DmsMsgPriority rp, int s, String o,
		Integer d)
	{
		super(createUniqueName());
		multi = m;
		beacon_enabled = be;
		bitmaps = b;
		activationPriority = ap.ordinal();
		runTimePriority = rp.ordinal();
		source = s;
		owner = o;
		duration = d;
		logMsg("created (server)");
	}

	/** Log a message */
	void logMsg(String msg) {
		if (MSG_LOG.isOpen())
			MSG_LOG.log(getName() + ": " + msg);
	}

	/** Associated incident */
	private Incident incident;

	/** Get the associated incident */
	@Override
	public Incident getIncident() {
		return incident;
	}

	/** Message MULTI string, contains message text for all pages */
	private String multi;

	/** Get the message MULTI string.
	 * @return Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.utils.MultiString */
	@Override
	public String getMulti() {
		return multi;
	}

	/** Beacon enabled flag */
	private boolean beacon_enabled;

	/** Get beacon enabled flag */
	@Override
	public boolean getBeaconEnabled() {
		return beacon_enabled;
	}

	/** Bitmap data for each page (Base64-encoded) */
	private String bitmaps;

	/** Get the bitmaps for all pages of the message.
	 * @return Base64-encoded bitmap data.
	 * @see us.mn.state.dot.tms.utils.Base64 */
	@Override
	public String getBitmaps() {
		return bitmaps;
	}

	/** Message activation priority */
	private int activationPriority;

	/** Get the activation priority.
	 * @return Priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DmsMsgPriority */
	@Override
	public int getActivationPriority() {
		return activationPriority;
	}

	/** Run-time priority */
	private int runTimePriority;

	/** Get the run-time priority.
	 * @return Run-time priority ranging from 1 (low) to 255 (high).
	 * @see us.mn.state.dot.tms.DmsMsgPriority */
	@Override
	public int getRunTimePriority() {
		return runTimePriority;
	}

	/** Sign message source */
	private int source;

	/** Get the sign message source value.
	 * @return Sign message source.
	 * @see us.mn.state.dot.tms.SignMsgSource */
	@Override
	public int getSource() {
		return source;
	}

	/** Owner of current message */
	private String owner;

	/** Get the sign message owner.
	 * @return User who deployed the message. */
	@Override
	public String getOwner() {
		return owner;
	}

	/** Duration of this message (minutes) */
	private Integer duration;

	/** Get the message duration.
	 * @return Duration in minutes; null means indefinite. */
	@Override
	public Integer getDuration() {
		return duration;
	}

	/** Write the SignMessage object as xml */
	public void writeXml(Writer w, DMSImpl dms) throws IOException {
		w.write("<sign_message");
		w.write(createAttribute("dms", dms.getName()));
		w.write(createAttribute("status", DMSHelper.getAllStyles(dms)));
		w.write(createAttribute("run_priority", runTimePriority));
		w.write(createAttribute("act_priority", activationPriority));
		w.write(createAttribute("source", getSource()));
		w.write(createAttribute("duration", getDuration()));
		w.write(createAttribute("incident", getIncident()));
		w.write(createAttribute("multi", multi));
		w.write(createAttribute("bitmaps", getBitmaps()));
		w.write(createAttribute("deploy_time", dms.getDeployTime()));
		w.write("/>\n");
	}
}
