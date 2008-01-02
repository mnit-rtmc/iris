/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

/**
 * This class handles all database interaction for the log object within IRIS.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class EventVault {

	/** The IP address of the machine hosting the database */
	private String hostIP;

	/** Port to connect on */
	private String port;

	/** The name of the database containing event data */
	private String dbName;

	/** The account name used to gain access to the database */
	private String userName;

	/** The password associated with the user name */
	private String password;

	/** The database connection object */
	private Connection connection = null;

	/** Statement used in database transactions */
	private Statement statement;

	/** Database connection driver */
	private String driver = "jdbc:postgresql";

	/** Misc constants */
	private final int NOT_FOUND = -1;

	/**
	 * Constructor for the EventVault object
	 *
	 * @param host	IP address of the db host machine.
	 * @param user	User account name to access db.
	 * @param prt	Port to connect to the database on
	 * @param db	Name of the database to connect
	 * @param pwd	User's password on the database host system
	 * @exception EventVaultException  Throws an EventVaultException if
	 * there is a problem accessing the data from the database.
	 */
	public EventVault(String host, String prt, String db, String user,
		String pwd) throws EventVaultException
	{
		hostIP = host;
		port = prt;
		dbName = db;
		userName = user;
		password = pwd;
		openConnection();
		try {
			statement = connection.createStatement();
		} catch(SQLException sqle) {
			throw new EventVaultException(sqle);
		}
	}

	/**
	 * Set the state of a record.
	 * Rather than deleting a record, it can be made inactive and
	 * will not be available for new records that reference it's table
	 *
	 * @param description	Record description to set the active state of
	 * @param table		The table which contains the record
	 * @param active	The state to set the active field to
	 * @exception EventVaultException  If there is a problem accessing
	 * the database
	 */
	public synchronized void setActive(String description, String table,
		boolean active) throws EventVaultException
	{
		description = parseString(description);
		String a = null;
		if(active)
			a = "'t'";
		else
			a = "'f'";
		update("UPDATE " + table + " SET active = " + a + " WHERE " +
			table + ".description = '" + description + "'");
	}

	/**
	 * Purge old records from the database.
	 *
	 * @param purgeClass	The object class to purge
	 * @param daysOld	All records older than daysOld will be deleted.
	 * @exception EventVaultException  If there is a problem accessing
	 * the database.
	 */
	public synchronized void purgeCommunicationLineEvents(int daysOld)
		 throws EventVaultException
	{
		purgeDeviceEvents("comm_line_event", daysOld);
	}

	/**
	 * Writes an object to the database.
	 *
	 * @param object                   The object to be added
	 * @exception EventVaultException  Throws an EventVaultException
	 * if the object is not writable (does not have proper field data or is
	 * missing critical field data) or if the object is not an instance of
	 * the types of objects that exist in this vault (TMSEvents and
	 * DeviceFailures).
	 */
	public synchronized void add(Object object) throws EventVaultException {
		if(object instanceof TMSEvent) {
			if(object instanceof CommunicationLineEvent) {
				addCommunicationLineEvent(
					(CommunicationLineEvent)object);
			} else if(object instanceof SignStatusEvent) {
				addSignStatusEvent((SignStatusEvent)object);
			} else if(object instanceof DetectorMalfunctionEvent) {
				addDetectorMalfunctionEvent(
					(DetectorMalfunctionEvent)object);
			} else {
				addTMSEvent((TMSEvent)object);
			}
		}
	}

	/**
	 * Remove a TMSEvent from the database.
	 *
	 * @param event                    The event to be removed
	 * @exception EventVaultException  If there is a problem accessing
	 * the database.
	 */
	public synchronized void removeEvent(TMSEvent event)
		 throws EventVaultException
	{
		if(event != null) {
			int eventId = event.getEventId();
			if(event instanceof CommunicationLineEvent)
				deleteEvent(eventId, "comm_line_event");
			else if(event instanceof SignStatusEvent)
				deleteEvent(eventId, "sign_status_event");
			else
				deleteEvent(eventId, "system_event");
		}
	}

	/**
	 * Get a set of ids
	 *
	 * @param field                    The fieldname containing the ids
	 * @param set                      The resultset to get the ids from
	 * @return                         The set of ids from the resultset
	 * @exception EventVaultException  If there is a problem accessing
	 * the database.
	 */
	private int[] getIds(String field, ResultSet set)
		throws EventVaultException
	{
		try {
			set.beforeFirst();
			Vector<Integer> ids = new Vector<Integer>();
			while(set.next())
				ids.addElement(set.getInt(field));
			if(ids.size() == 0)
				return null;
			else {
				int[] values = new int[ids.size()];
				for(int i = 0; i < values.length; i++)
					values[i] = ids.get(i).intValue();
				return values;
			}
		}
		catch(SQLException sqle) {
			throw new EventVaultException(sqle);
		}
	}

	/**
	 * Get an id
	 *
	 * @param field		The name of the field containing the id
	 * @param set		The resultset containing the record
	 * @return		An id.  If no id is found, returns NOT_FOUND
	 * @exception EventVaultException  If there is a problem accessing
	 * the database.  Also will throw an EventVaultException if
	 * more than one id is found in the resultset.
	 */
	private int getId(String field, ResultSet set)
		throws EventVaultException
	{
		int[] ids = getIds(field, set);
		if(ids == null)
			return NOT_FOUND;
		else if(ids.length > 1) {
			throw new EventVaultException(field +
				" has multiple entries in database.");
		}
		return ids[0];
	}

	/**
	 * Purge all device events from table that are older than daysOld.
	 *
	 * @param daysOld	All records older than daysOld will be deleted.
	 * @exception EventVaultException  If there is a problem accessing
	 * the database.
	 */
	private void purgeDeviceEvents(String table, int daysOld)
		throws EventVaultException
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_YEAR,
			c.get(Calendar.DAY_OF_YEAR) - daysOld);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
		String dateString = dateFormat.format(c.getTime());
		String sql = "DELETE FROM " + table + " WHERE event_date < '"
			+ dateString + "'";
		update(sql);
	}

	/**
	 * Writes a TMSEvent to the database.
	 *
	 * @param e	The feature to be added to the TMSEvent attribute
	 * @exception EventVaultException  If there is a problem accessing
	 * the database.
	 */
	private void addTMSEvent(TMSEvent e) throws EventVaultException {
		String sql = "SELECT device_type_id FROM " +
			"device_type WHERE description = 'System'";
		int deviceTypeId = getId("device_type_id", query(sql));
		if(deviceTypeId == NOT_FOUND)
			throw new EventVaultException(
				"System is not a valid device type.");
		sql = "SELECT event_desc_id FROM event_description " +
			"WHERE description = '" + e.getEventDescription() +
			"' AND device_type_id = " + deviceTypeId;
		int eventDescId = getId("event_desc_id", query(sql));
		if(eventDescId == NOT_FOUND)
			throw new EventVaultException(e.getEventDescription() +
				" is not a valid event description.");
		sql = "INSERT INTO system_event (event_date, event_desc_id) " +
			"VALUES ('" + e.getEventCalendar().getTime() + "', " +
			eventDescId + ")";
		update(sql);
	}

	/**
	 * Writes a SignStatusEvent to the database.
	 *
	 * @param e                        The SignStatusEvent to be added
	 * @exception EventVaultException  If there is a problem accessing
	 * the database.
	 */
	private void addSignStatusEvent(SignStatusEvent e)
		throws EventVaultException
	{
		String sql = "SELECT device_type_id FROM " +
			" device_type WHERE description = '" +
			e.getDeviceType() + "'";
		int deviceTypeId = getId("device_type_id", query(sql));
		if(deviceTypeId == NOT_FOUND)
			throw new EventVaultException(e.getDeviceType() +
				" is not a valid device type.");
		sql = "SELECT event_desc_id FROM event_description " +
			"WHERE device_type_id = " + deviceTypeId +
			" AND description = '" + e.getEventDescription() + "'";
		int eventDescId = getId("event_desc_id", query(sql));
		if(eventDescId == NOT_FOUND)
			throw new EventVaultException(e.getEventDescription() +
				" is not a valid event description.");
		int userId = getUserId(e.getLoggedBy());
		e.setMessage(parseString(e.getMessage()));
		e.setEventRemarks(parseString(e.getEventRemarks()));
		sql = "INSERT INTO sign_status_event " +
			"(event_date, event_desc_id, device_id, message, " +
			"user_id ) " + "values ('" +
			e.getEventCalendar().getTime() + "', " + eventDescId +
			", '" + e.getDeviceId() + "', '" + e.getMessage() +
			"', " + userId + ")";
		update(sql);
	}

	/**
	 * Writes a DetectorMalfunctionEvent to the database.
	 *
	 * @param e	The DetectorMalfunctionEvent to be added
	 * @exception EventVaultException  If there is a problem accessing
	 * the database.
	 */
	private void addDetectorMalfunctionEvent(DetectorMalfunctionEvent e)
		 throws EventVaultException
	{
		String sql = "SELECT event_description_id " +
			"FROM active_event_descriptions " +
			"WHERE device_type = '" + e.getDeviceType() + "' " +
			"AND event_description = '" + e.getEventDescription() + "'";
		int eventDescId = getId("event_description_id", query(sql));
		if(eventDescId == NOT_FOUND)
			throw new EventVaultException(e.getEventDescription() +
				" is not a valid event description.");
		int userId = getUserId(e.getLoggedBy());
		e.setEventDescription( parseString(e.getEventDescription()));
		e.setEventRemarks(parseString( e.getEventRemarks()));
		sql = "INSERT INTO detector_malfunction_event " +
			"(event_date, event_desc_id, device_id, logged_by) " +
			"values ('" + e.getEventCalendar().getTime() + "', " +
			eventDescId + ", '" + e.getDeviceId() + "', " + userId + ")";
		update(sql);
	}

	/** Get the id of the given user */
	private int getUserId(String userName) throws EventVaultException {
		String sql = "SELECT id from tms_user where description = '" +
			userName + "'";
		int userId = getId("id", query(sql));
		if(userId == NOT_FOUND) {
			String insert = "INSERT INTO tms_user (description) " +
				"VALUES ( '" + userName + "')";
			update(insert);
			userId = getId("id", query(sql));
			if(userId == NOT_FOUND) {
				throw new EventVaultException( "Error: " +
					"unable to add user (" + userName +
					") to database." );
			}
		}
		return userId;
	}

	/**
	 * Writes a CommunicationLineEvent to the database.
	 *
	 * @param e	The CommunicationLineEvent to be added
	 * @exception EventVaultException  If there is a problem accessing
	 * the database.
	 */
	private void addCommunicationLineEvent(CommunicationLineEvent e)
		throws EventVaultException
	{
		String sql = "SELECT event_description_id " +
			"FROM active_event_descriptions " +
			"WHERE device_type = 'Communication Line' " +
			"AND event_description = '" + e.getEventDescription() +
			"'";
		int eventDescId = getId("event_description_id", query(sql));
		if(eventDescId == NOT_FOUND)
			throw new EventVaultException(e.getEventDescription() +
				" is not a valid event description.");
		e.setEventRemarks(parseString(e.getEventRemarks()));
		sql = "INSERT INTO comm_line_event " +
			"(event_date, event_desc_id, line, drop, remarks, device_id) VALUES ('" +
			e.getEventCalendar().getTime() + "', " + eventDescId + ", " +
			e.getLine() + ", " + e.getDrop() + ", '" + e.getEventRemarks() + "', '" +
			e.getDeviceId() + "')";
		update(sql);
	}

	/**
	 */
	private synchronized void deleteEvent(int eventId, String table)
		throws EventVaultException
	{
		update("DELETE FROM " + table + " WHERE event_id = " + eventId);
	}

	/**
	 * Creates a string that can be used directly in a PostgreSQL statement given a
	 * string with a single quote in it.
	 *
	 * @param string  The string to be parsed
	 * @return        The parsed string
	 */
	private String parseString(String string) {
		boolean done;
		int index;
		if(string != null) {
			StringBuffer b = new StringBuffer(string);
			done = false;
			index = 0;
			while(!done) {
				if((b.toString().indexOf("'", index)) > -1) {
					index = b.toString().indexOf("'", index);
					b.insert(index, "\\");
					index = index + 2;
				} else
					done = true;
				string = b.toString();
			}
		}
		return string;
	}

	/**
	 * Open the connection to the database.
	 *
	 * @exception EventVaultException  If there is a problem accessing
	 * the database.
	 */
	private void openConnection() throws EventVaultException {
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection(
				driver + "://" + hostIP + ":" + port + "/" +
				dbName, userName, password);
			System.out.println("Opened  connection to " + dbName + " database.");
		} catch(ClassNotFoundException cnfe) {
			throw new EventVaultException(cnfe);
		} catch(SQLException sqle) {
			throw new EventVaultException(sqle);
		}
	}

	/**
	 * Add, Update or Delete a record
	 *
	 * @param updateSQL                The SQL statement used in the database
	 *      update statement.
	 * @exception EventVaultException  If there is a problem accessing
	 * the database.
	 */
	private void update(String updateSQL) throws EventVaultException {
		try {
			if(connection.isClosed())
				openConnection();
			statement.executeUpdate(updateSQL);
		} catch(SQLException sqle) {
			throw new EventVaultException(sqle);
		}
	}

	/**
	 * Query the database
	 *
	 * @param sql                      Description of Parameter
	 * @return                         Description of the Returned Value
	 * @exception EventVaultException  If there is a problem accessing
	 * the database.
	 */
	private ResultSet query(String sql) throws EventVaultException {
		try {
			if(connection.isClosed())
				openConnection();
			return statement.executeQuery(sql);
		} catch(SQLException sqle) {
			throw new EventVaultException(sqle);
		}
	}
}
