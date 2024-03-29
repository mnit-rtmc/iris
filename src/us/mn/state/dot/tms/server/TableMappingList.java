/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2021  Minnesota Department of Transportation
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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import us.mn.state.dot.tms.TMSException;

/**
 * A database mapping for an associative (many-to-many) table relation.
 *
 * @author Douglas Lau
 */
public class TableMappingList {

	/** Connection to SQL database */
	private final SQLConnection store;

	/** Name of mapping table */
	private final String name;

	/** Name of first table */
	private final String table0;

	/** Name of second table */
	private final String table1;

	/** Create a new database table mapping list */
	public TableMappingList(SQLConnection s, String sch, String t0,
		String t1)
	{
		store = s;
		name = sch + '.' + t0 + '_' + t1;
		table0 = t0;
		table1 = t1;
	}

	/** Create an SQL lookup query */
	private String createLookup(String key) {
		return "SELECT " + table1 +
		       " FROM " + name +
		       " WHERE " + table0 + " = '" + key + "'" +
		       " ORDER BY ordinal;";
	}

	/** Lookup related objects from the given table/key pair */
	public List<String> lookup(Storable owner) throws TMSException {
		final String key = owner.getPKey();
		final ArrayList<String> res = new ArrayList<String>();
		store.query(createLookup(key), new ResultFactory() {
			public void create(ResultSet row) throws Exception {
				res.add(row.getString(1));
			}
		});
		return res;
	}

	/** Create an SQL delete statement */
	private String createDelete(String key) {
		return "DELETE FROM " + name +
		      " WHERE " + table0 + " = '" + key + "';";
	}

	/** Create the start of an SQL insert statement */
	private String createInsertStart(String key) {
		return "INSERT INTO " + name + "(" + table0 + "," + table1 +
		       ",ordinal) VALUES ('" + key + "','";
	}

	/** Create an SQL insert statement */
	private String createInsert(String start, Storable v, int o) {
		return start + v.getPKey() + "'," + o + ");";
	}

	/** Update the relation from one table to a list in the other */
	public void update(Storable owner, List<Storable> values)
		throws TMSException
	{
		final String key = owner.getPKey();
		final String insert = createInsertStart(key);
		final ListIterator<Storable> it = values.listIterator();
		store.batch(new BatchFactory() {
			private boolean first = true;
			public String next() {
				if (first) {
					first = false;
					return createDelete(key);
				} else if (it.hasNext()) {
					int o = it.nextIndex();
					return createInsert(insert,it.next(),o);
				} else
					return null;
			}
		});
	}
}
