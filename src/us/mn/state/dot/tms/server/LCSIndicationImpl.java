/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2020  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSIndication;
import us.mn.state.dot.tms.TMSException;

/**
 * A lane-use control sign indication is a mapping of a controller I/O pin
 * with a specific lane-use indication.
 *
 * @author Douglas Lau
 */
public class LCSIndicationImpl extends ControllerIoImpl
	implements LCSIndication
{
	/** Load all the LCS indications */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, LCSIndicationImpl.class);
		store.query("SELECT name, controller, pin, lcs, indication " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LCSIndicationImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("lcs", lcs);
		map.put("indication", indication);
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

	/** Create a new LCS indication */
	private LCSIndicationImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // controller
		     row.getInt(3),     // pin
		     row.getString(4),  // lcs
		     row.getInt(5)      // indication
		);
	}

	/** Create a new LCS indication */
	private LCSIndicationImpl(String n, String c, int p, String l, int i) {
		super(n, lookupController(c), p);
		lcs = lookupLCS(l);
		indication = i;
		initTransients();
	}

	/** Create a new LCS indication */
	public LCSIndicationImpl(String n) {
		super(n, null, 0);
	}

	/** LCS associated with this indication */
	private LCSImpl lcs;

	/** Get the LCS */
	@Override
	public LCS getLcs() {
		return lcs;
	}

	/** Ordinal of LaneUseIndication */
	private int indication;

	/** Get the indication (ordinal of LaneUseIndication) */
	@Override
	public int getIndication() {
		return indication;
	}
}
