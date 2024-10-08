/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.TMSException;

/**
 * A Lane-Use Control Signal is a special DMS which is designed to display
 * lane-use indications.
 *
 * @author Douglas Lau
 */
public class LCSImpl extends BaseObjectImpl implements LCS {

	/** Load all the LCS */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, lcs_array, lane FROM iris." +
			SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LCSImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// lcs_array
					row.getInt(3)		// lane
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("lcs_array", lcsArray);
		map.put("lane", lane);
		return map;
	}

	/** Create a new LCS */
	public LCSImpl(String n) {
		super(n);
	}

	/** Create an LCS */
	private LCSImpl(Namespace ns, String n, String a, int l) {
		this(n, (LCSArrayImpl) ns.lookupObject(LCSArray.SONAR_TYPE, a),
		     l);
	}

	/** Create an LCS */
	private LCSImpl(String n, LCSArray a, int l) {
		this(n);
		lcsArray = a;
		lane = l;
		initTransients();
	}

	/** Initialize the LCS array */
	@Override
	public void initTransients() {
		try {
			if (lcsArray instanceof LCSArrayImpl) {
				LCSArrayImpl la = (LCSArrayImpl) lcsArray;
				la.setLane(lane, this);
			}
		}
		catch (TMSException e) {
			System.err.println("LCS " + getName() +
				" initialization error");
			e.printStackTrace();
		}
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		if (lcsArray instanceof LCSArrayImpl) {
			LCSArrayImpl la = (LCSArrayImpl) lcsArray;
			la.setLane(lane, null);
		}
		super.doDestroy();
	}

	/** LCS array containing this LCS */
	private LCSArray lcsArray;

	/** Get the LCS array */
	@Override
	public LCSArray getArray() {
		return lcsArray;
	}

	/** Lane number */
	private int lane;

	/** Get the lane number (starting from right lane as 1) */
	@Override
	public int getLane() {
		return lane;
	}
}
