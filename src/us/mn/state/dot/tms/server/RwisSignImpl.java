/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  SRF Consulting Group
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

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.RwisSign;
import us.mn.state.dot.tms.RwisDmsHelper;
import us.mn.state.dot.tms.TMSException;

/**
 * This table contains an entry for each RWIS-enabled DMS 
 * and provides current RWIS status information for each
 * of those DMS.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class RwisSignImpl extends BaseObjectImpl implements RwisSign {

	/** Device debug log */
	static private final DebugLog RWIS_DMS_LOG = new DebugLog("rwis_sign");

//	/** Check if device log is open */
//	protected boolean isDeviceLogging() {
//		return RWIS_DMS_LOG.isOpen();
//	}

	/** Log a device message */
	protected void logError(String msg) {
		if (RWIS_DMS_LOG.isOpen())
			RWIS_DMS_LOG.log(getName() + ": " + msg);
	}

	/** Destroy a RwisSign object */
	public void doDestroy() throws TMSException {
		super.doDestroy();
		super.notifyRemove();
}

	/** Find existing or create a new RwisSign.
	 * @param dmsName.
	 */
	static public RwisSignImpl findOrCreate(String dmsName)
	{
		RwisSign sd = RwisDmsHelper.lookup(dmsName);
		if (sd instanceof RwisSignImpl)
			return (RwisSignImpl) sd;
		else {
			RwisSignImpl sdi = new RwisSignImpl(dmsName, "", null);
			return createNotify(sdi);
		}
	}

	/** Notify clients of the new RwisSign object */
	static private RwisSignImpl createNotify(RwisSignImpl rdi) {
		try {
			rdi.notifyCreate();
			return rdi;
		}
		catch (SonarException e) {
			System.err.println("createNotify: " + e.getMessage());
			return null;
		}
	}

	/** Load all the RwisSign(s) */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, RwisSignImpl.class);
		store.query("SELECT name, rwis_conditions, msg_pattern FROM iris." + SONAR_TYPE +
			";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new RwisSignImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("rwis_conditions", rwis_conditions);
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

	/** Create a new RwisSign */
	public RwisSignImpl(String n) {
		super(n);
	}

	/** Create an RwisSign */
	private RwisSignImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // rwis_conditions
		     row.getString(3)   // msg_pattern
		);
	}

	/** Create an RwisSign */
	public RwisSignImpl(String n, String rc, String mp)
	{
		this(n);
		rwis_conditions = rc;
		msg_pattern = mp;
	}

	/** RWIS conditions */
	private String rwis_conditions;

	/** Set the RWIS conditions */
	@Override
	public void setRwisConditionsNotify(String rc) {
		if (!objectEquals(rwis_conditions, rc)) {
			try {
				store.update(this, "rwis_conditions", rc);
				rwis_conditions = rc;
				notifyAttribute("rwisConditions");
			}
			catch (TMSException e) {
				logError("rwis_conditions: " + e.getMessage());
			}
		}
	}

	/** Get the RWIS conditions */
	@Override
	public String getRwisConditions() {
		return rwis_conditions;
	}

	/** Derived RWIS MsgPattern name */
	private String msg_pattern;

	/** Set the derived RWIS MsgPattern name */
	@Override
	public void setMsgPatternNotify(String pat) {
		if (!objectEquals(msg_pattern, pat)) {
			try {
				store.update(this, "msg_pattern", pat);
				msg_pattern = pat;
				notifyAttribute("msgPattern");
			}
			catch (TMSException e) {
				logError("msg_pattern: " + e.getMessage());
			}
		}
		msg_pattern = pat;
	}

	/** Get the derived RWIS MsgPattern name */
	@Override
	public String getMsgPattern() {
		return msg_pattern;
	}
}
