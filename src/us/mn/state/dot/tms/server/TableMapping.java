/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2016  Minnesota Department of Transportation
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
 * A database mapping for a many-to-many table relation.
 *
 * @author Douglas Lau
 */
public class TableMapping {

	/** Connection to SQL database */
	private final SQLConnection store;

	/** Name of mapping table */
	private final String name;

	/** Name of first table */
	private final String table0;

	/** Name of second table */
	private final String table1;

	/** Create a new database table mapping */
	public TableMapping(SQLConnection s, String sch, String t0, String t1) {
		store = s;
		name = sch + '.' + t0 + '_' + t1;
		table0 = t0;
		table1 = t1;
	}

	/** Given one of the tables in the mapping, get the other one */
	private String getOtherTable(String table) throws TMSException {
		if (table.equals(table0))
			return table1;
		else if (table.equals(table1))
			return table0;
		else
			throw new TMSException("INVALID TABLE " + table);
	}

	/** Create an SQL lookup query */
	private String createLookup(String key) throws TMSException {
		return "SELECT " + table1 +
		      " FROM " + name +
		      " WHERE " + table0 + " = '" + key + "';";
	}

	/** Lookup related objects from the given table/key pair */
	public Set<String> lookup(Storable owner) throws TMSException {
		final String key = owner.getKey();
		final HashSet<String> set = new HashSet<String>();
		store.query(createLookup(key), new ResultFactory() {
			public void create(ResultSet row) throws Exception {
				set.add(row.getString(1));
			}
		});
		return set;
	}

	/** Create an SQL delete statement */
	private String createDelete(String table, String key) {
		return "DELETE FROM " + name + " WHERE " +
			table + " = '" + key + "';";
	}

	/** Create the start of an SQL insert statement */
	private String createInsertStart(String table, String key)
		throws TMSException
	{
		return "INSERT INTO " + name + "(" + table +
			"," + getOtherTable(table) + ") VALUES ('" + key +"','";
	}

	/** Update the relation from one table to a set in the other */
	public void update(final String table, Storable owner,
		Set<Storable> values) throws TMSException
	{
		final String key = owner.getKey();
		final String insert = createInsertStart(table, key);
		final Iterator<Storable> it = values.iterator();
		store.batch(new BatchFactory() {
			private boolean first = true;
			public String next() {
				if (first) {
					first = false;
					return createDelete(table, key);
				} else if (it.hasNext()) {
					Storable v = it.next();
					return insert + v.getKey() + "');";
				} else
					return null;
			}
		});
	}

	/** Update the relation from one table to an array in the other */
	public void update(String table, Storable owner, Storable[] values)
		throws TMSException
	{
		HashSet<Storable> set = new HashSet<Storable>(values.length);
		for (Storable s: values)
			set.add(s);
		update(table, owner, set);
	}
}
