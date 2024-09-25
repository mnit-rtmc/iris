/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.TMSException;

/**
 * A database mapping for play list entries.
 *
 * @author Douglas Lau
 */
public class PlayListMapping {

	/** Create an SQL lookup query */
	static private String createLookup(String key) {
		return "SELECT COALESCE(camera, sub_list)" +
		       " FROM iris.play_list_entry " +
		       " WHERE play_list = '" + key + "'" +
		       " ORDER BY ordinal;";
	}

	/** Create an SQL delete statement */
	static private String createDelete(String key) {
		return "DELETE FROM iris.play_list_entry " +
		      " WHERE play_list = '" + key + "';";
	}

	/** Create the start of an SQL insert statement */
	static private String createInsertStart(PlayListImpl pl) {
		String key = pl.getPKey();
		String ent = pl.getMeta() ? "sub_list" : "camera";
		return "INSERT INTO iris.play_list_entry" +
		       " (play_list, ordinal, " + ent + ")" +
		       " VALUES ('" + key + "',";
	}

	/** Create an SQL insert statement for an entry */
	private String createInsert(String start, int o, String entry) {
		return start + o + ",'" + entry + "');";
	}

	/** Connection to SQL database */
	private final SQLConnection store;

	/** Create a new play list mapping */
	public PlayListMapping(SQLConnection s) {
		store = s;
	}

	/** Lookup entries from the given play list */
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

	/** Update the entries in the play list */
	public void update(PlayListImpl pl, String[] entries)
		throws TMSException
	{
		final String key = pl.getPKey();
		final String st = createInsertStart(pl);
		store.batch(new BatchFactory() {
			private boolean first = true;
			private int i = 0;
			public String next() {
				if (i < entries.length) {
					if (first) {
						first = false;
						return createDelete(key);
					}
					int o = i;
					String entry = entries[o];
					i++;
					return createInsert(st, o, entry);
				} else
					return null;
			}
		});
	}
}
