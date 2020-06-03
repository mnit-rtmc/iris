/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2020  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignConfigHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * A quick message is a sign message which consists of a MULTI string.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class QuickMessageImpl extends BaseObjectImpl implements QuickMessage {

	/** Load all the quick messages */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, QuickMessageImpl.class);
		store.query("SELECT name, sign_group, sign_config, " +
			"prefix_page, multi FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new QuickMessageImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("sign_group", sign_group);
		map.put("sign_config", sign_config);
		map.put("prefix_page", prefix_page);
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
	public QuickMessageImpl(String n) {
		super(n);
	}

	/** Create a quick message */
	private QuickMessageImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // sign_group
		     row.getString(3),  // sign_config
		     row.getBoolean(4), // prefix_page
		     row.getString(5)   // multi
		);
	}

	/** Create a quick message */
	private QuickMessageImpl(String n, String sg, String sc, boolean pp,
		String m)
	{
		super(n);
		sign_group = lookupSignGroup(sg);
		sign_config = SignConfigHelper.lookup(sc);
		prefix_page = pp;
		multi = m;
	}

	/** Sign group */
	private SignGroup sign_group;

	/** Get the sign group associated with the quick message.
	 * @return Sign group for quick message; null for no group. */
	@Override
	public SignGroup getSignGroup() {
		return sign_group;
	}

	/** Set the sign group associated with the quick message.
	 * @param sg Sign group to associate; null for no group. */
	@Override
	public void setSignGroup(SignGroup sg) {
		sign_group = sg;
	}

	/** Set the sign group associated with the quick message.
	 * @param sg Sign group to associate; null for no group. */
	public void doSetSignGroup(SignGroup sg) throws TMSException {
		if (sg != sign_group) {
			store.update(this, "sign_group", sg);
			setSignGroup(sg);
		}
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

	/** Prefix page flag */
	private boolean prefix_page;

	/** Set prefix page flag */
	@Override
	public void setPrefixPage(boolean pp) {
		prefix_page = pp;
	}

	/** Set prefix page flag */
	public void doSetPrefixPage(boolean pp) throws TMSException {
		if (pp != prefix_page) {
			store.update(this, "prefix_page", pp);
			setPrefixPage(pp);
		}
	}

	/** Get prefix page flag */
	@Override
	public boolean getPrefixPage() {
		return prefix_page;
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
