/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.server.Namespace;

/**
 * Sign text contains the properties of a single line message for display
 * on a dynamic message sign (DMS).
 *
 * @author Douglas Lau
 */
public class SignTextImpl extends BaseObjectImpl implements SignText {

	/** Sign message text validation regex pattern */
	static protected final Pattern MESS_PATTERN = Pattern.compile(
		"[0-9A-Z !#$%&()*+,-./:;<=>?'@]*");

	/** Validate a message string */
	static protected void validateMessage(String t)
		throws ChangeVetoException
	{
		Matcher m = MESS_PATTERN.matcher(t);
		if(!m.matches())
			throw new ChangeVetoException("Invalid message: " + t);
		if(t.length() > 24)
			throw new ChangeVetoException("Message too wide");
	}

	/** Load all the sign text */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading sign text...");
		namespace.registerType(SONAR_TYPE, SignTextImpl.class);
		store.query("SELECT name, sign_group, line, message, priority" +
			" FROM sign_text;", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.add(new SignTextImpl(namespace,
					row.getString(1),	// name
					row.getString(2),	// sign_group
					row.getShort(3),	// line
					row.getString(4),	// message
					row.getShort(5)		// priority
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("sign_group", sign_group);
		map.put("line", line);
		map.put("message", message);
		map.put("priority", priority);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new sign text message */
	public SignTextImpl(String n) {
		super(n);
	}

	/** Create a new sign text message */
	public SignTextImpl(String n, SignGroup g, short l, String m, short p) {
		super(n);
		sign_group = g;
		line = l;
		message = m;
		priority = p;
	}

	/** Create a new sign text message */
	protected SignTextImpl(Namespace ns, String n, String g, short l,
		String m, short p) throws NamespaceError
	{
		this(n, (SignGroupImpl)ns.lookupObject("sign_group", g), l,m,p);
	}

	/** Sign group */
	protected SignGroup sign_group;

	/** Get the sign group */
	public SignGroup getSignGroup() {
		return sign_group;
	}

	/** Line number on sign (usually 1-3) */
	protected short line;

	/** Set the line */
	public void setLine(short l) {
		line = l;
	}

	/** Set the line */
	public void doSetLine(short l) throws TMSException {
		if(l == line)
			return;
		store.update(this, "line", l);
		setLine(l);
	}

	/** Get the line */
	public short getLine() {
		return line;
	}

	/** Message text */
	protected String message;

	/** Set the message */
	public void setMessage(String m) {
		message = m;
	}

	/** Set the message */
	public void doSetMessage(String m) throws TMSException {
		if(m.equals(message))
			return;
		validateMessage(m);
		store.update(this, "message", m);
		setMessage(m);
	}

	/** Get the message */
	public String getMessage() {
		return message;
	}

	/** Message ordering priority */
	protected short priority;

	/** Set the priority */
	public void setPriority(short p) {
		priority = p;
	}

	/** Set the priority */
	public void doSetPriority(short p) throws TMSException {
		if(p == priority)
			return;
		store.update(this, "priority", p);
		setPriority(p);
	}

	/** Get the priority */
	public short getPriority() {
		return priority;
	}
}
