/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2024  Minnesota Department of Transportation
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.PlayListHelper;
import us.mn.state.dot.tms.TMSException;

/**
 * Play list (camera sequence).
 *
 * @author Douglas lau
 */
public class PlayListImpl extends BaseObjectImpl implements PlayList {

	/** PlayList / Entry table mapping */
	static private PlayListMapping ent_map;

	/** Load all the play lists */
	static public void loadAll() throws TMSException {
		ent_map = new PlayListMapping(store);
		store.query("SELECT name, meta, seq_num, notes " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new PlayListImpl(row));
			}
		});
		initAllTransients();
	}

	/** Initialize transients for all play lists.  This needs to happen after
	 * all play lists are loaded (for resolving sub lists). */
	static private void initAllTransients() throws TMSException {
		Iterator<PlayList> it = PlayListHelper.iterator();
		while (it.hasNext()) {
			PlayList pl = it.next();
			if (pl instanceof PlayListImpl)
				((PlayListImpl) pl).initTransients();
		}
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("meta", meta);
		map.put("seq_num", seq_num);
		map.put("notes", notes);
		return map;
	}

	/** Create a new play list */
	public PlayListImpl(String n) throws TMSException {
		super(n);
		initTransients();
	}

	/** Create a play list from database lookup */
	private PlayListImpl(ResultSet row) throws SQLException, TMSException {
		this(row.getString(1),           // name
		     row.getBoolean(2),          // meta
		     (Integer) row.getObject(3), // seq_num
		     row.getString(4)            // notes
		);
	}

	/** Create a play list from database lookup */
	private PlayListImpl(String n, boolean m, Integer sn, String nt)
		throws TMSException
	{
		this(n);
		meta = m;
		seq_num = sn;
		notes = nt;
	}

	/** Initialize the transient state */
	@Override
	public void initTransients() throws TMSException {
		super.initTransients();
		entries = buildEntries(
			ent_map.lookup(this).toArray(new String[0])
		);
	}

	/** Meta list flag */
	private boolean meta;

	/** Get meta list flag */
	@Override
	public boolean getMeta() {
		return meta;
	}

	/** Sequence number */
	private Integer seq_num;

	/** Set sequence number */
	@Override
	public void setSeqNum(Integer n) {
		seq_num = n;
	}

	/** Set sequence number */
	public void doSetSeqNum(Integer n) throws TMSException {
		if (n != seq_num) {
			if (n != null && (n < NUM_MIN || n > NUM_MAX))
				throw new ChangeVetoException("Invalid seq #");
			store.update(this, "seq_num", n);
			setSeqNum(n);
		}
	}

	/** Get sequence number */
	@Override
	public Integer getSeqNum() {
		return seq_num;
	}

	/** Notes (including hashtags) */
	private String notes;

	/** Set notes (including hashtags) */
	@Override
	public void setNotes(String n) {
		notes = n;
	}

	/** Set notes (including hashtags) */
	public void doSetNotes(String n) throws TMSException {
		if (!objectEquals(n, notes)) {
			store.update(this, "notes", n);
			setNotes(n);
		}
	}

	/** Get notes (including hashtags) */
	@Override
	public String getNotes() {
		return notes;
	}

	/** Entries in the play list */
	private String[] entries = new String[0];

	/** Build array of entries */
	private String[] buildEntries(String[] ents) {
		return (meta) ? buildSubLists(ents) : buildCameras(ents);
	}

	/** Build array of sub list entries */
	private String[] buildSubLists(String[] ents) {
		ArrayList<String> ls = new ArrayList<String>();
		for (String e: ents) {
			if (lookupPlayList(e) != null)
				ls.add(e);
		}
		return ls.toArray(new String[0]);
	}

	/** Build array of camera entries */
	private String[] buildCameras(String[] ents) {
		ArrayList<String> ls = new ArrayList<String>();
		for (String e: ents) {
			if (lookupCamera(e) != null)
				ls.add(e);
		}
		return ls.toArray(new String[0]);
	}

	/** Set the entries in the play list */
	@Override
	public void setEntries(String[] ents) {
		entries = buildEntries(ents);
	}

	/** Set the entries in the play list */
	public void doSetEntries(String[] ents) throws TMSException {
		if (!Arrays.equals(ents, buildEntries(ents)))
			throw new ChangeVetoException("Invalid entries");
		if (!Arrays.equals(entries, ents)) {
			ent_map.update(this, ents);
			setEntries(ents);
		}
	}

	/** Get the entries in the play list */
	@Override
	public String[] getEntries() {
		return entries;
	}
}
