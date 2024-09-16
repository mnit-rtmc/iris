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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.TMSException;

/**
 * A lane-use MULTI is an association between lane-use indication and a
 * MULTI string.
 *
 * @author Douglas Lau
 */
public class LaneUseMultiImpl extends BaseObjectImpl implements LaneUseMulti {

	/** Load all the lane-use MULTIs */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, LaneUseMultiImpl.class);
		store.query("SELECT name, indication, msg_num, msg_pattern, " +
			"dms_hashtag FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LaneUseMultiImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("indication", indication);
		map.put("msg_num", msg_num);
		map.put("msg_pattern", msg_pattern);
		map.put("dms_hashtag", dms_hashtag);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Create a new lane-use MULTI */
	public LaneUseMultiImpl(String n) {
		super(n);
	}

	/** Create a lane-use MULTI */
	private LaneUseMultiImpl(ResultSet row) throws SQLException {
		this(row.getString(1), // name
		     row.getInt(2),    // indication
		     row.getObject(3), // msg_num
		     row.getString(4), // msg_pattern
		     row.getString(5)  // dms_hashtag
		);
	}

	/** Create a new lane-use MULTI */
	private LaneUseMultiImpl(String n, int i, Object mn, String pat,
		String ht)
	{
		super(n);
		indication = i;
		msg_num = (Integer) mn;
		msg_pattern = lookupMsgPattern(pat);
		dms_hashtag = ht;
	}

	/** Ordinal of LaneUseIndication */
	protected int indication;

	/** Set the indication (ordinal of LaneUseIndication) */
	@Override
	public void setIndication(int i) {
		indication = i;
	}

	/** Set the indication (ordinal of LaneUseIndication) */
	public void doSetIndication(int i) throws TMSException {
		if (i == indication)
			return;
		LaneUseIndication ind = LaneUseIndication.fromOrdinal(i);
		if (ind == null)
			throw new ChangeVetoException("Invalid indication:" +i);
		store.update(this, "indication", i);
		setIndication(i);
	}

	/** Get the indication (ordinal of LaneUseIndication) */
	@Override
	public int getIndication() {
		return indication;
	}

	/** Message number */
	protected Integer msg_num;

	/** Set the message number */
	@Override
	public void setMsgNum(Integer n) {
		msg_num = n;
	}

	/** Set the message number */
	public void doSetMsgNum(Integer n) throws TMSException {
		if (!objectEquals(n, msg_num)) {
			store.update(this, "msg_num", n);
			setMsgNum(n);
		}
	}

	/** Get the message number */
	@Override
	public Integer getMsgNum() {
		return msg_num;
	}

	/** Message pattern to send for indication */
	protected MsgPattern msg_pattern;

	/** Set the message pattern */
	@Override
	public void setMsgPattern(MsgPattern pat) {
		msg_pattern = pat;
	}

	/** Set the message pattern */
	public void doSetMsgPattern(MsgPattern pat) throws TMSException {
		if (pat != msg_pattern) {
			store.update(this, "msg_pattern", pat);
			setMsgPattern(pat);
		}
	}

	/** Get the message pattern */
	@Override
	public MsgPattern getMsgPattern() {
		return msg_pattern;
	}

	/** DMS hashtag */
	private String dms_hashtag;

	/** Set the DMS hashtag */
	@Override
	public void setDmsHashtag(String ht) {
		dms_hashtag = ht;
	}

	/** Set the DMS hashtag */
	public void doSetDmsHashtag(String ht) throws TMSException {
		String t = Hashtags.normalize(ht);
		if (!objectEquals(t, ht))
			throw new ChangeVetoException("Bad hashtag");
		if (!objectEquals(ht, dms_hashtag)) {
			store.update(this, "dms_hashtag", ht);
			setDmsHashtag(ht);
		}
	}

	/** Get the DMS hashtag */
	@Override
	public String getDmsHashtag() {
		return dms_hashtag;
	}
}
