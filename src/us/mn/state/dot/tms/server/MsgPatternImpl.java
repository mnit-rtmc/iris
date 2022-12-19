/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2022  Minnesota Department of Transportation
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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * A message pattern is a partially or fully composed message for a DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class MsgPatternImpl extends BaseObjectImpl implements MsgPattern {

	/** Create a unique MsgPattern record name */
	static public String createUniqueName(String template) {
		UniqueNameCreator unc = new UniqueNameCreator(template, 20,
			(n)->lookupMsgPattern(n));
		return unc.createUniqueName();
	}

	/** Load all the message patterns */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, MsgPatternImpl.class);
		store.query("SELECT name, sign_config, sign_group, " +
			"msg_combining, multi FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new MsgPatternImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("sign_config", sign_config);
		map.put("sign_group", sign_group);
		map.put("msg_combining", msg_combining);
		map.put("multi", multi);
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

	/** Create a new message (by SONAR clients) */
	public MsgPatternImpl(String n) {
		super(n);
	}

	/** Create a message pattern */
	private MsgPatternImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // sign_config
		     row.getString(3),  // sign_group
		     row.getInt(4),     // msg_combining
		     row.getString(5)   // multi
		);
	}

	/** Create a message pattern */
	private MsgPatternImpl(String n, String sc, String sg, int mc,
		String m)
	{
		this(n, lookupSignConfig(sc), lookupSignGroup(sg), mc, m);
	}

	/** Create a message pattern */
	public MsgPatternImpl(String n, SignConfig sc, SignGroup sg, int mc,
		String m)
	{
		super(n);
		sign_config = sc;
		sign_group = sg;
		msg_combining = mc;
		multi = m;
	}

	/** Sign config */
	private SignConfig sign_config;

	/** Get the sign configuration */
	@Override
	public SignConfig getSignConfig() {
		return sign_config;
	}

	/** Set the sign configuration */
	@Override
	public void setSignConfig(SignConfig sc) {
		sign_config = sc;
	}

	/** Set the sign configuration */
	public void doSetSignConfig(SignConfig sc) throws TMSException {
		if (sc != sign_config) {
			store.update(this, "sign_config", sc);
			setSignConfig(sc);
		}
	}

	/** Sign group */
	private SignGroup sign_group;

	/** Get the sign group associated with the message pattern.
	 * @return Sign group for message; null for no group. */
	@Override
	public SignGroup getSignGroup() {
		return sign_group;
	}

	/** Set the sign group associated with the message pattern.
	 * @param sg Sign group to associate; null for no group. */
	@Override
	public void setSignGroup(SignGroup sg) {
		sign_group = sg;
	}

	/** Set the sign group associated with the message pattern.
	 * @param sg Sign group to associate; null for no group. */
	public void doSetSignGroup(SignGroup sg) throws TMSException {
		if (sg != sign_group) {
			store.update(this, "sign_group", sg);
			setSignGroup(sg);
		}
	}

	/** Message combining value */
	private int msg_combining;

	/** Set message combining value.
	 * @see us.mn.state.dot.tms.MsgCombining */
	@Override
	public void setMsgCombining(int mc) {
		msg_combining = mc;
	}

	/** Set message combining value */
	public void doSetMsgCombining(int mc) throws TMSException {
		if (mc != msg_combining) {
			store.update(this, "msg_combining", mc);
			setMsgCombining(mc);
		}
	}

	/** Get message combining value.
	 * @see us.mn.state.dot.tms.MsgCombining */
	@Override
	public int getMsgCombining() {
		return msg_combining;
	}

	/** Message MULTI string, contains message text for all pages */
	private String multi = "";

	/** Get the message MULTI string.
	 * @return Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.utils.MultiString */
	@Override
	public String getMulti() {
		return multi;
	}

	/** Set the message MULTI string.
	 * @param m Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.utils.MultiString */
	@Override
	public void setMulti(String m) {
		multi = m;
	}

	/** Set the MULTI string */
	public void doSetMulti(String m) throws TMSException {
		if (!new MultiString(m).isValid())
			throw new ChangeVetoException("Invalid MULTI: " + m);
		if (!m.equals(multi)) {
			store.update(this, "multi", m);
			setMulti(m);
		}
	}
}
