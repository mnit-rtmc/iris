/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.SignTextHelper;
import us.mn.state.dot.tms.TMSException;

/**
 * Sign text contains the properties of a single line MULTI string for display
 * on a dynamic message sign (DMS).
 *
 * @author Douglas Lau
 */
public class SignTextImpl extends BaseObjectImpl implements SignText {

	/** Load all the sign text */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, SignTextImpl.class);
		store.query("SELECT name, sign_group, line, multi, rank " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new SignTextImpl(
					row.getString(1),	// name
					row.getString(2),	// sign_group
					row.getShort(3),	// line
					row.getString(4),	// multi
					row.getShort(5)		// rank
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("sign_group", sign_group);
		map.put("line", line);
		map.put("multi", multi);
		map.put("rank", rank);
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

	/** Create a new sign text message */
	public SignTextImpl(String n) {
		super(n);
	}

	/** Create a new sign text message */
	private SignTextImpl(String n, SignGroup g, short l, String m, short r) {
		super(n);
		sign_group = g;
		line = l;
		multi = m;
		rank = r;
	}

	/** Create a new sign text message */
	private SignTextImpl(String n, String g, short l, String m, short p) {
		this(n, lookupSignGroup(g), l, m, p);
	}

	/** Sign group */
	private SignGroup sign_group;

	/** Get the sign group */
	@Override
	public SignGroup getSignGroup() {
		return sign_group;
	}

	/** Line number on sign (usually 1-3) */
	private short line;

	/** Set the line */
	@Override
	public void setLine(short l) {
		line = l;
	}

	/** Set the line */
	public void doSetLine(short l) throws TMSException {
		if (l != line) {
			store.update(this, "line", l);
			setLine(l);
		}
	}

	/** Get the line */
	@Override
	public short getLine() {
		return line;
	}

	/** MULTI string */
	private String multi;

	/** Set the MULTI string */
	@Override
	public void setMulti(String m) {
		multi = m;
	}

	/** Set the MULTI string */
	public void doSetMulti(String m) throws TMSException {
		if (!m.equals(multi)) {
			if (!SignTextHelper.isMultiValid(m))
			    throw new ChangeVetoException("Invalid MULTI: " + m);
			store.update(this, "multi", m);
			setMulti(m);
		}
	}

	/** Get the MULTI string */
	@Override
	public String getMulti() {
		return multi;
	}

	/** Message ordering rank */
	private short rank;

	/** Set the rank */
	@Override
	public void setRank(short r) {
		rank = r;
	}

	/** Set the rank */
	public void doSetRank(short r) throws TMSException {
		if (r != rank) {
			store.update(this, "rank", r);
			setRank(r);
		}
	}

	/** Get the rank */
	@Override
	public short getRank() {
		return rank;
	}
}
