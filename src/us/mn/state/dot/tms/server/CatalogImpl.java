/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2024  Minnesota Department of Transportation
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
import java.util.ArrayList;
import us.mn.state.dot.tms.Catalog;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.TMSException;
import static us.mn.state.dot.tms.PlayList.NUM_MIN;
import static us.mn.state.dot.tms.PlayList.NUM_MAX;

/**
 * Catalog (camera play list sequence).
 *
 * @author Douglas lau
 */
public class CatalogImpl extends BaseObjectImpl implements Catalog {

	/** Catalog / PlayList table mapping */
	static private TableMappingList mapping;

	/** Load all the catalogs */
	static public void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, CatalogImpl.class);
		mapping = new TableMappingList(store, "iris", SONAR_TYPE,
			PlayList.SONAR_TYPE);
		store.query("SELECT name, seq_num, description FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new CatalogImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("seq_num", seq_num);
		map.put("description", description);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Create a new catalog */
	public CatalogImpl(String n) {
		super(n);
	}

	/** Create a catalog from database lookup */
	private CatalogImpl(ResultSet row) throws SQLException, TMSException {
		this(row.getString(1),  // name
		     row.getInt(2),     // seq_num
		     row.getString(3)   // description
		);
	}

	/** Create a catalog from database lookup */
	private CatalogImpl(String n, int sn, String d) throws TMSException {
		this(n);
		seq_num = sn;
		description = d;
		ArrayList<PlayListImpl> pls = new ArrayList<PlayListImpl>();
		for (String o: mapping.lookup(this)) {
			pls.add(lookupPlayList(o));
		}
		play_lists = pls.toArray(new PlayListImpl[0]);
	}

	/** Sequence number */
	private int seq_num;

	/** Set sequence number */
	@Override
	public void setSeqNum(int n) {
		seq_num = n;
	}

	/** Set sequence number */
	public void doSetSeqNum(int n) throws TMSException {
		if (n != seq_num) {
			if (n < NUM_MIN || n > NUM_MAX)
				throw new ChangeVetoException("Invalid seq #");
			store.update(this, "seq_num", n);
			setSeqNum(n);
		}
	}

	/** Get sequence number */
	@Override
	public int getSeqNum() {
		return seq_num;
	}

	/** Description of the catalog */
	private String description;

	/** Set the description */
	@Override
	public void setDescription(String d) {
		description = d;
	}

	/** Set the description */
	public void doSetDescription(String d) throws TMSException {
		if (!objectEquals(d, description)) {
			store.update(this, "description", d);
			setDescription(d);
		}
	}

	/** Get the description */
	@Override
	public String getDescription() {
		return description;
	}

	/** Play lists in the catalog */
	private PlayListImpl[] play_lists = new PlayListImpl[0];

	/** Set the play lists in the catalog */
	@Override
	public void setPlayLists(PlayList[] pl) {
		ArrayList<PlayListImpl> pls = new ArrayList<PlayListImpl>();
		for (PlayList p: pl) {
			if (p instanceof PlayListImpl)
				pls.add((PlayListImpl) p);
		}
		play_lists = pls.toArray(new PlayListImpl[0]);
	}

	/** Set the play lists in the catalog */
	public void doSetPlayLists(PlayList[] pl) throws TMSException {
		ArrayList<Storable> pls = new ArrayList<Storable>();
		for (PlayList p: pl) {
			if (p instanceof PlayListImpl)
				pls.add((PlayListImpl) p);
			else
				throw new ChangeVetoException("Invalid p-list");
		}
		mapping.update(this, pls);
		setPlayLists(pl);
	}

	/** Get the play lists in the catalog */
	@Override
	public PlayList[] getPlayLists() {
		return play_lists;
	}
}
