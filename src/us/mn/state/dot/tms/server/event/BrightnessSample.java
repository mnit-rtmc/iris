/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.event;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.ResultFactory;

/**
 * A brightness feedback sample point
 *
 * @author Douglas Lau
 */
public class BrightnessSample extends BaseEvent {

	/** Lookup all brightness samples for a DMS */
	static public void lookup(DMS dms, final DMSImpl.BrightnessHandler bh)
		throws TMSException
	{
		store.query("SELECT event_desc_id, photocell, output " +
			"FROM event.brightness_sample WHERE dms = '" +
			dms.getName() + "' ORDER BY event_date;",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				bh.feedback(
					EventType.fromId(row.getInt(1)),
					row.getInt(2),		// photocell
					row.getInt(3)		// output
				);
			}
		});
	}

	/** DMS being sampled */
	public final DMS dms;

	/** Photocell value (0-65535) */
	public final int photocell;

	/** Light output value (0-65535) */
	public final int output;

	/** Create a new brightness feedback sample point */
	public BrightnessSample(EventType et, DMS d, int p, int o) {
		super(et);
		assert et == EventType.DMS_BRIGHT_LOW ||
		       et == EventType.DMS_BRIGHT_GOOD ||
		       et == EventType.DMS_BRIGHT_HIGH;
		dms = d;
		photocell = p;
		output = o;
	}

	/** Get the database table name */
	public String getTable() {
		return "event.brightness_sample";
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("event_desc_id", event_type.id);
		map.put("event_date", event_date);
		map.put("dms", dms);
		map.put("photocell", photocell);
		map.put("output", output);
		return map;
	}

	/** Purge all conflicting samples */
	public void purgeConflicting() throws TMSException {
		String where = whereClause();
		if(where == null)
			return;
		store.update("DELETE FROM " + getTable() + " WHERE dms = '" +
			dms.getName() + "' AND " + where + ";");
	}

	/** Get SQL WHERE clause for conflicting samples.  This ensures that
	 * none of the samples will cause the brightness table to have a
	 * negative slope. */
	protected String whereClause() {
		switch(event_type) {
		case DMS_BRIGHT_LOW:
			return "(" + pgeOleClause() + " OR " +
			       lowPleOgeClause() + ")";
		case DMS_BRIGHT_GOOD:
			return "(" + lowPgeClause() + " OR " + highPleClause() +
			       " OR " + peqClause() + ") AND " + oeqClause();
		case DMS_BRIGHT_HIGH:
			return "(" + pleOgeClause() + " OR " +
			       highPgeOleClause() + ")";
		default:
			return null;
		}
	}

	/** Get SQL clause for photocell equal */
	protected String peqClause() {
		return "photocell = " + photocell;
	}

	/** Get SQL clause for photocell less than or equal */
	protected String pleClause() {
		return "photocell <= " + photocell;
	}

	/** Get SQL clause for photocell greater than or equal */
	protected String pgeClause() {
		return "photocell >= " + photocell;
	}

	/** Get SQL clause for output equal */
	protected String oeqClause() {
		return "output = " + output;
	}

	/** Get SQL clause for output less than or equal */
	protected String oleClause() {
		return "output <= " + output;
	}

	/** Get SQL clause for output greater than or equal */
	protected String ogeClause() {
		return "output >= " + output;
	}

	/** Get SQL clause for photocell greater and output less */
	protected String pgeOleClause() {
		return "(" + pgeClause() + " AND " + oleClause() + ")";
	}

	/** Get SQL clause for photocell less and output greater */
	protected String pleOgeClause() {
		return "(" + pleClause() + " AND " + ogeClause() + ")";
	}

	/** Get SQL clause for _LOW samples */
	protected String lowClause() {
		return "event_desc_id = " + EventType.DMS_BRIGHT_LOW.id;
	}

	/** Get SQL clause for _HIGH samples */
	protected String highClause() {
		return "event_desc_id = " + EventType.DMS_BRIGHT_HIGH.id;
	}

	/** Get SQL clause for _LOW photocell greater than or equal */
	protected String lowPgeClause() {
		return "(" + lowClause() + " AND " + pgeClause() + ")";
	}

	/** Get SQL clause for _HIGH photocell less than or equal */
	protected String highPleClause() {
		return "(" + highClause() + " AND " + pleClause() + ")";
	}

	/** Get SQL clause for _LOW photocell less and output greater */
	protected String lowPleOgeClause() {
		return "(" + lowClause() + " AND " + pleClause() + " AND " +
		       ogeClause() + ")";
	}

	/** Get SQL clause for _HIGH photocell greater and output less */
	protected String highPgeOleClause() {
		return "(" + highClause() + " AND " + pgeClause() + " AND " +
		       oleClause() + ")";
	}
}
