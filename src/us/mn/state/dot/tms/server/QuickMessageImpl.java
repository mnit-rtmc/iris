/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.TMSException;

/**
 * A quick message is a sign message which consists of a MULTI string.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class QuickMessageImpl extends BaseObjectImpl implements QuickMessage {

	/** Load all the quick messages */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading quick messages...");
		namespace.registerType(SONAR_TYPE, QuickMessageImpl.class);
		store.query("SELECT name, multi FROM iris.quick_message;", 
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new QuickMessageImpl(
					row.getString(1),	// name
					row.getString(2)	// multi
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("multi", multi);
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

	/** Create a new message (by SONAR clients) */
	public QuickMessageImpl(String n) {
		super(n);
	}

	/** Create a message */
	protected QuickMessageImpl(String n, String m) {
		super(n);
		multi = m;
	}

	/** Message MULTI string, contains message text for all pages */
	protected String multi = "";

	/** Get the message MULTI string.
	 * @return Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.MultiString */
	public String getMulti() {
		return multi;
	}

	/** Set the message MULTI string.
	 * @return Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.MultiString */
	public void setMulti(String m) {
		multi = m;
	}

	/** Set the MULTI string */
	public void doSetMulti(String m) throws TMSException {
		if(m.equals(multi))
			return;
		if(!new MultiString(m).isValid())
			throw new ChangeVetoException("Invalid MULTI: " + m);
		store.update(this, "multi", m);
		setMulti(m);
	}
}
