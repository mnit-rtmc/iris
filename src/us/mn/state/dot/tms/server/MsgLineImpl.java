/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2023  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgLine;
import us.mn.state.dot.tms.MsgLineHelper;
import us.mn.state.dot.tms.TMSException;

/**
 * A message line contains the properties of a single line MULTI string for
 * filling in a message pattern.
 *
 * @author Douglas Lau
 */
public class MsgLineImpl extends BaseObjectImpl implements MsgLine {

	/** Load all the message lines */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, MsgLineImpl.class);
		store.query("SELECT name, msg_pattern, restrict_hashtag," +
			"line, multi, rank FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new MsgLineImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("msg_pattern", msg_pattern);
		map.put("restrict_hashtag", restrict_hashtag);
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

	/** Create a new message line */
	public MsgLineImpl(String n) {
		super(n);
	}

	/** Create a message line */
	private MsgLineImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // msg_pattern
		     row.getString(3),  // restrict_hashtag
		     row.getShort(4),   // line
		     row.getString(5),  // multi
		     row.getShort(6));  // rank
	}

	/** Create a message line */
	private MsgLineImpl(String n, String mp, String rht, short l,
		String m, short r)
	{
		super(n);
		msg_pattern = lookupMsgPattern(mp);
		restrict_hashtag = rht;
		line = l;
		multi = m;
		rank = r;
	}

	/** Message pattern */
	private MsgPattern msg_pattern;

	/** Get the message pattern */
	@Override
	public MsgPattern getMsgPattern() {
		return msg_pattern;
	}

	/** Restrict hashtag */
	private String restrict_hashtag;

	/** Get restrict hashtag, or null for none */
	@Override
	public String getRestrictHashtag() {
		return restrict_hashtag;
	}

	/** Set restrict hashtag, or null for none */
	@Override
	public void setRestrictHashtag(String rht) {
		restrict_hashtag = rht;
	}

	/** Set restrict hashtag, or null for none */
	public void doSetRestrictHashtag(String rht) throws TMSException {
		String ht = DMSHelper.normalizeHashtag(rht);
		if (!objectEquals(ht, rht))
			throw new ChangeVetoException("Bad hashtag");
		if (!objectEquals(rht, restrict_hashtag)) {
			store.update(this, "restrict_hashtag", rht);
			setRestrictHashtag(rht);
		}
	}

	/** Line number on sign (usually 1-3) */
	private short line;

	/** Set the line number */
	@Override
	public void setLine(short l) {
		line = l;
	}

	/** Set the line number */
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
			if (!MsgLineHelper.isMultiValid(m))
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
