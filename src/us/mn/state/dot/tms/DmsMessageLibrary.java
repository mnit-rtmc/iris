/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006  Minnesota Department of Transportation
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
import java.sql.SQLException;
import java.util.HashMap;

/**
 * This is a database abstraction for the DMS message library.
 *
 * @author Douglas Lau
 */
public class DmsMessageLibrary implements ResultFactory {

	/** Connection to SQL database */
	protected final SQLConnection store;

	/** Mapping of message IDs to DmsMessage objects */
	protected final HashMap messages = new HashMap();

	/** Get a mapping of the messages in the library */
	public HashMap getMessages() {
		return messages;
	}

	/** Create a new DMS message library */
	public DmsMessageLibrary(SQLConnection s) throws TMSException {
		store = s;
		store.query("SELECT * FROM dms_message;", this);
	}

	/** Create a DMS message from the current row of a result set */
	public void create(ResultSet row) throws SQLException {
		DmsMessage m = new DmsMessage(
			row.getInt(1),		// id
			row.getString(2),	// dms
			row.getShort(3),	// line
			row.getString(4),	// message
			row.getString(5),	// abbrev
			row.getShort(6)		// priority
		);
		synchronized(messages) {
			messages.put(m.id, m);
		}
	}

	/** Insert one entry into the message library for one DMS */
	protected void insertLocal(int id, String dms, short line)
		throws TMSException
	{
		store.update("INSERT INTO dms_message (id, dms, line) VALUES " +
			"('" + id + "', '" + dms + "', '" + line + "');");
	}

	/** Insert one entry into the global message library */
	protected void insertGlobal(int id, short line) throws TMSException {
		store.update("INSERT INTO dms_message (id, line) VALUES " +
			"('" + id + "', '" + line + "');");
	}

	/** Insert one entry into the message library */
	public DmsMessage insert(String dms_id, short line)
		throws TMSException
	{
		synchronized(messages) {
			int id = DmsMessage.nextId();
			if(dms_id != null)
				insertLocal(id, dms_id, line);
			else
				insertGlobal(id, line);
			store.query("SELECT * FROM dms_message WHERE id = '" +
				id + "';", this);
			return (DmsMessage)messages.get(new Integer(id));
		}
	}

	/** Update one entry in the message library */
	public void update(DmsMessage m) throws TMSException {
		synchronized(messages) {
			if(!messages.containsKey(m.id))
				throw new ChangeVetoException("Invalid ID");
			store.update("UPDATE dms_message SET message = '" +
				m.message + "', abbrev = '" + m.abbrev +
				"', priority = '" + m.priority +
				"' WHERE id = " + m.id + ";");
			messages.put(m.id, m);
		}
	}

	/** Remove one entry from the message library */
	public void remove(DmsMessage m) throws TMSException {
		synchronized(messages) {
			if(!messages.containsKey(m.id))
				throw new ChangeVetoException("Invalid ID");
			store.update("DELETE FROM dms_message WHERE id = " +
				m.id + ";");
			messages.remove(m.id);
		}
	}
}
