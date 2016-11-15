/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.QuickMessage;
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
		store.query("SELECT name, indication, msg_num, width, height, "+
			"quick_message FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LaneUseMultiImpl(
					namespace,
					row.getString(1),	// name
					row.getInt(2),		// indication
					(Integer)row.getObject(3), // msg_num
					row.getInt(4),		// width
					row.getInt(5),		// height
					row.getString(6)	// quick_message
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("indication", indication);
		map.put("msg_num", msg_num);
		map.put("width", width);
		map.put("height", height);
		map.put("quick_message", quick_message);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new lane-use MULTI */
	public LaneUseMultiImpl(String n) {
		super(n);
	}

	/** Create a new lane-use MULTI */
	protected LaneUseMultiImpl(Namespace ns, String n, int i, Integer mn,
		int w, int h, String qm)
	{
		this(n, i, mn, w, h, (QuickMessage)ns.lookupObject(
		     QuickMessage.SONAR_TYPE, qm));
	}

	/** Create a new lane-use MULTI */
	protected LaneUseMultiImpl(String n, int i, Integer mn, int w, int h,
		QuickMessage qm)
	{
		this(n);
		indication = i;
		msg_num = mn;
		width = w;
		height = h;
		quick_message = qm;
	}

	/** Ordinal of LaneUseIndication */
	protected int indication;

	/** Set the indication (ordinal of LaneUseIndication) */
	public void setIndication(int i) {
		indication = i;
	}

	/** Set the indication (ordinal of LaneUseIndication) */
	public void doSetIndication(int i) throws TMSException {
		if(i == indication)
			return;
		LaneUseIndication ind = LaneUseIndication.fromOrdinal(i);
		if(ind == null)
			throw new ChangeVetoException("Invalid indication:" +i);
		store.update(this, "indication", i);
		setIndication(i);
	}

	/** Get the indication (ordinal of LaneUseIndication) */
	public int getIndication() {
		return indication;
	}

	/** Message number */
	protected Integer msg_num;

	/** Set the message number */
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
	public Integer getMsgNum() {
		return msg_num;
	}

	/** Indication sign width */
	protected int width;

	/** Set the indication sign width */
	public void setWidth(int w) {
		width = w;
	}

	/** Set the indication sign width */
	public void doSetWidth(int w) throws TMSException {
		if(w == width)
			return;
		store.update(this, "width", w);
		setWidth(w);
	}

	/** Get the indication sign width */
	public int getWidth() {
		return width;
	}

	/** Indication sign height */
	protected int height;

	/** Set the indication sign height */
	public void setHeight(int h) {
		height = h;
	}

	/** Set the indication sign height */
	public void doSetHeight(int h) throws TMSException {
		if(h == height)
			return;
		store.update(this, "height", h);
		setHeight(h);
	}

	/** Get the indication sign height */
	public int getHeight() {
		return height;
	}

	/** Quick message to send for indication */
	protected QuickMessage quick_message;

	/** Set the quick message */
	public void setQuickMessage(QuickMessage qm) {
		quick_message = qm;
	}

	/** Set the quick message */
	public void doSetQuickMessage(QuickMessage qm) throws TMSException {
		if(qm == quick_message)
			return;
		store.update(this, "quick_message", qm);
		setQuickMessage(qm);
	}

	/** Get the quick message */
	public QuickMessage getQuickMessage() {
		return quick_message;
	}
}
