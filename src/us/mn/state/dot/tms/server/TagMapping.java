/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  Minnesota Department of Transportation
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import us.mn.state.dot.tms.TMSException;

/**
 * A database mapping for an associative (one-to-many) table relation.
 *
 * @author Douglas Lau
 */
public class TagMapping {

	/** Connection to SQL database */
	private final SQLConnection store;

	/** Name of mapping table */
	private final String name;

	/** Name of first table */
	private final String table;

	/** Name of tag */
	private final String tag;

	/** Create a new database tag mapping */
	public TagMapping(SQLConnection s, String sch, String tb, String tg) {
		store = s;
		name = sch + '.' + tb + '_' + tg;
		table = tb;
		tag = tg;
	}

	/** Create an SQL lookup query */
	private String createLookup(String key) {
		if (key.contains("'"))
			key = SQLConnection.escapeValue(key);
		return "SELECT " + tag  +
		      " FROM " + name +
		      " WHERE " + table + " = '" + key + "';";
	}

	/** Lookup related objects from the given table/key pair */
	public Set<String> lookup(Storable owner) throws TMSException {
		final String key = owner.getPKey();
		final HashSet<String> set = new HashSet<String>();
		store.query(createLookup(key), new ResultFactory() {
			public void create(ResultSet row) throws Exception {
				set.add(row.getString(1));
			}
		});
		return set;
	}

	/** Create an SQL delete statement */
	private String createDelete(String key) {
		return "DELETE FROM " + name +
		      " WHERE " + table + " = '" + key + "';";
	}

	/** Create the start of an SQL insert statement */
	private String createInsertStart(String key) {
		return "INSERT INTO " + name + "(" + table + "," + tag + ")" +
		      " VALUES ('" + key + "','";
	}

	/** Create an SQL insert statement */
	private String createInsert(String start, String t) {
		return start + t + "');";
	}

	/** Update the relation from one table to a set */
	public void update(Storable owner, Set<String> tags)
		throws TMSException
	{
		final String key = owner.getPKey();
		final String insert = createInsertStart(key);
		final Iterator<String> it = tags.iterator();
		store.batch(new BatchFactory() {
			private boolean first = true;
			public String next() {
				if (first) {
					first = false;
					return createDelete(key);
				} else if (it.hasNext())
					return createInsert(insert, it.next());
				else
					return null;
			}
		});
	}
}
