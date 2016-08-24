/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2016  Minnesota Department of Transportation
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.TMSException;

/**
 * Simple SQL database abstraction stuff
 *
 * @author Douglas Lau
 */
public class SQLConnection {

	/** SQL debug log */
	static private final DebugLog SQL_LOG = new DebugLog("sql");

	/** Pattern to match for a SQL identifier */
	static private final Pattern SQL_IDENTIFIER =
		Pattern.compile("[a-z_0-9.]*");

	/** Validate a SQL identifier */
	static private void validateIdentifier(String sql)
		throws ChangeVetoException
	{
		Matcher m = SQL_IDENTIFIER.matcher(sql);
		if(!m.matches()) {
			throw new ChangeVetoException("Invalid SQL identifier: "
				+ sql);
		}
	}

	/** Pattern to match for a SQL string constant value */
	static private final Pattern SQL_VALUE =
		Pattern.compile("[[\\p{Graph}\\p{Blank}\n]]*");

	/** Validate a SQL string constant value */
	static private void validateValue(String v) throws ChangeVetoException {
		Matcher m = SQL_VALUE.matcher(v);
		if(!m.matches())
			throw new ChangeVetoException("Invalid SQL value: " +v);
	}

	/** Escape a string constant value for SQL */
	static private String escapeValue(Object value) {
		return value.toString().replace("'", "''");
	}

	/** Location of database server */
	protected final String location;

	/** User to log into database server */
	protected final String user;

	/** Password to log into database server */
	protected final String password;

	/** Connection to the SQL database */
	protected Connection connection = null;

	/** Available SQL statements */
	protected final LinkedList<Statement> statements =
		new LinkedList<Statement>();

	/** Create a new SQL connection */
	public SQLConnection(String url, String usr, String pswd)
		throws TMSException
	{
		try {
			Class.forName("org.postgresql.Driver");
		}
		catch(ClassNotFoundException e) {
			throw new TMSException(e);
		}
		location = url;
		user = usr;
		password = pswd;
	}

	/** Close the current database connection */
	protected void close() throws SQLException {
		statements.clear();
		if(connection != null) {
			try {
				connection.close();
			}
			finally {
				connection = null;
			}
		}
	}

	/** Open a new database connection */
	protected void open() throws SQLException {
		connection = DriverManager.getConnection(location, user,
			password);
		connection.setAutoCommit(true);
	}

	/** Create a database statement */
	protected Statement _createStatement() throws SQLException {
		if(connection == null)
			open();
		return connection.createStatement();
	}

	/** Create a database statement */
	protected Statement createStatement() throws TMSException {
		try {
			return _createStatement();
		}
		catch(SQLException e) {
			SQL_LOG.log("createStatement -> " + e);
			try {
				close();
				return _createStatement();
			}
			catch(SQLException e2) {
				SQL_LOG.log("createStatement.2 -> " + e2);
				throw new TMSException(e2);
			}
		}
	}

	/** Get an available statement */
	protected synchronized Statement getStatement() throws TMSException {
		if(statements.isEmpty())
			return createStatement();
		else
			return statements.removeLast();
	}

	/** Put a statement back after using it */
	protected synchronized void putStatement(Statement s) {
		statements.add(s);
	}

	/** Query the database and call a factory for each result */
	public void query(String sql, ResultFactory factory)
		throws TMSException
	{
		Statement s = getStatement();
		try {
			ResultSet set = s.executeQuery(sql);
			try {
				while(set.next())
					factory.create(set);
			}
			finally {
				set.close();
			}
			putStatement(s);
		}
		catch(Exception e) {
			throw new TMSException(e);
		}
	}

	/** Update the database with the given SQL command */
	public void update(String sql) throws TMSException {
		Statement s = getStatement();
		try {
			s.executeUpdate(sql);
			putStatement(s);
		}
		catch(SQLException e) {
			SQL_LOG.log(sql + " -> " + e);
			throw new TMSException(e);
		}
	}

	/** Update one field in a storable database table */
	public void update(Storable s, String field, Object value)
		throws TMSException
	{
		validateIdentifier(field);
		String key = escapeValue(s.getKey());
		validateValue(key);
		if(value == null) {
			updateNull(s, field, key);
			return;
		}
		String v = escapeValue(value);
		validateValue(v);
		update("UPDATE " + s.getTable() + " SET " + field +
			" = '" + v + "' WHERE " + s.getKeyName() +
			" = '" + key + "';");
	}

	/** Update one field with a NULL value */
	private void updateNull(Storable s, String field, String key)
		throws TMSException
	{
		update("UPDATE " + s.getTable() + " SET " + field +
			" = NULL WHERE " + s.getKeyName() + " = '" +
			key + "';");
	}

	/** Create one storable record */
	public void create(Storable s) throws TMSException {
		Map<String, Object> columns = s.getColumns();
		StringBuilder keys = new StringBuilder();
		StringBuilder values = new StringBuilder();
		for(Map.Entry<String, Object> e: columns.entrySet()) {
			Object value = e.getValue();
			if(value != null) {
				String field = e.getKey();
				validateIdentifier(field);
				keys.append(field);
				keys.append(",");
				String val = escapeValue(value);
				validateValue(val);
				values.append("'");
				values.append(val);
				values.append("',");
			}
		}
		keys.setLength(keys.length() - 1);
		values.setLength(values.length() - 1);
		String sql = "INSERT INTO " + s.getTable() + " (" + keys +
			") VALUES (" + values + ");";
		update(sql);
	}

	/** Destroy one storable record */
	public void destroy(Storable s) throws TMSException {
		String val = escapeValue(s.getKey());
		validateValue(val);
		update("DELETE FROM " + s.getTable() + " WHERE " +
			s.getKeyName() + " = '" + val + "';");
	}

	/** Update the database with a batch of SQL commands */
	public void batch(BatchFactory f) throws TMSException {
		Statement s = getStatement();
		try {
			while(true) {
				String sql = f.next();
				if(sql == null)
					break;
				s.addBatch(sql);
			}
			s.executeBatch();
			s.clearBatch();
			putStatement(s);
		}
		catch(SQLException e) {
			SQL_LOG.log("batch -> " + e);
			throw new TMSException(e);
		}
	}
}
