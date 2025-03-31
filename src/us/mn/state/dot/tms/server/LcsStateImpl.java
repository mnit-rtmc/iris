/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsIndication;
import us.mn.state.dot.tms.LcsState;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.TMSException;

/**
 * An LCS state represents a lane/indication combination for an LCS array.
 *
 * @author Douglas Lau
 */
public class LcsStateImpl extends ControllerIoImpl implements LcsState,
	Comparable<LcsStateImpl>
{
	/** Load all the LCS states */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, controller, pin, lcs, lane, " +
			"indication, msg_pattern, msg_num FROM iris." +
			SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LcsStateImpl(
					row.getString(1),  // name
					row.getString(2),  // controller
					row.getInt(3),     // pin
					row.getString(4),  // lcs
					row.getInt(5),     // lane
					row.getInt(6),     // indication
					row.getString(7),  // msg_pattern
					row.getObject(8)   // msg_num
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("lcs", lcs);
		map.put("lane", lane);
		map.put("indication", indication);
		map.put("msg_pattern", msg_pattern);
		map.put("msg_num", msg_num);
		return map;
	}

	/** Create a new LCS state */
	public LcsStateImpl(String n) {
		super(n, null, 0);
	}

	/** Create an LCS state */
	private LcsStateImpl(String n, String c, int p, String l, int ln,
		int i, String pat, Object mn)
	{
		super(n, lookupController(c), p);
		lcs = lookupLcs(l);
		lane = ln;
		indication = i;
		msg_pattern = lookupMsgPattern(pat);
		msg_num = (Integer) mn;
	}

	/** Compare to another LCS state */
	@Override
	public int compareTo(LcsStateImpl o) {
		return name.compareTo(o.name);
	}

	/** Test if this state equals another state */
	@Override
	public boolean equals(Object o) {
		if (o instanceof LcsStateImpl)
			return name.equals(((LcsStateImpl) o).name);
		else
			return false;
	}

	/** LCS array */
	private Lcs lcs;

	/** Get the LCS array */
	@Override
	public Lcs getLcs() {
		return lcs;
	}

	/** Lane number */
	private int lane;

	/** Set the lane number */
	@Override
	public void setLane(int ln) {
		lane = ln;
	}

	/** Set the lane number */
	public void doSetLane(int ln) throws TMSException {
		if (ln != lane) {
			store.update(this, "lane", ln);
			setLane(ln);
		}
	}

	/** Get the lane number (starting from right lane as 1) */
	@Override
	public int getLane() {
		return lane;
	}

	/** Ordinal of LcsIndication */
	private int indication;

	/** Set the indication (ordinal of LcsIndication) */
	@Override
	public void setIndication(int i) {
		indication = i;
	}

	/** Set the indication (ordinal of LcsIndication) */
	public void doSetIndication(int i) throws TMSException {
		if (i != indication) {
			LcsIndication ind = LcsIndication.fromOrdinal(i);
			if (ind == null) {
				throw new ChangeVetoException(
					"Bad indication:" + i);
			}
			store.update(this, "indication", i);
			setIndication(i);
		}
	}

	/** Get the indication (ordinal of LcsIndication) */
	@Override
	public int getIndication() {
		return indication;
	}

	/** Message pattern to send for indication */
	private MsgPattern msg_pattern;

	/** Set the message pattern */
	@Override
	public void setMsgPattern(MsgPattern pat) {
		msg_pattern = pat;
	}

	/** Set the message pattern */
	public void doSetMsgPattern(MsgPattern pat) throws TMSException {
		if (!objectEquals(pat, msg_pattern)) {
			store.update(this, "msg_pattern", pat);
			setMsgPattern(pat);
		}
	}

	/** Get the message pattern */
	@Override
	public MsgPattern getMsgPattern() {
		return msg_pattern;
	}

	/** Message number */
	private Integer msg_num;

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
}
