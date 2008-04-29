/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2008  Minnesota Department of Transportation
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Simple SQL database abstraction stuff
 *
 * @author Douglas Lau
 */
public class SQLConnection {

	/** Pattern to match for a SQL update value */
	static protected final Pattern SQL_VALUE =
		Pattern.compile("[[\\p{Graph}\\p{Blank}\n]&&[^'\\\\]]*");

	/** Validate a SQL update value */
	static protected void validateSql(String sql)
		throws ChangeVetoException
	{
		Matcher m = SQL_VALUE.matcher(sql);
		if(!m.matches()) throw
			new ChangeVetoException("Invalid SQL value: " + sql);
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
	public SQLConnection(String host, String port, String database,
		String userName, String password) throws TMSException
	{
		try {
			Class.forName("org.postgresql.Driver");
		}
		catch(ClassNotFoundException e) {
			throw new TMSException(e);
		}
		location = "jdbc:postgresql://" + host + ":" + port + "/" +
			database;
		user = userName;
		this.password = password;
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
			e.printStackTrace();
			try {
				close();
				return _createStatement();
			}
			catch(SQLException e2) {
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
			throw new TMSException(e);
		}
	}

	/** Get the string representation of an object value */
	static protected String valueAsString(Object value) {
		if(value != null)
			return value.toString();
		else
			// this "0" is needed for ObjectVault compatibility
			return "0";
	}

	/** Update one field in a storable database table */
	public void update(Storable s, String field, Object value)
		throws TMSException
	{
		// FIXME: throw an exception if field is mixed case or contains
		// spaces, etc. Then we can get rid of the quotes around the
		// field name.
		if(value == null) {
			updateNull(s, field);
			return;
		}
		String v = valueAsString(value);
		validateSql(v);
		update("UPDATE " + s.getTable() + " SET \"" + field +
			"\" = '" + v + "' WHERE " + s.getKeyName() +
			" = '" + s.getKey() + "';");
	}

	/** Update one field with a NULL value */
	protected void updateNull(Storable s, String field) throws TMSException
	{
		update("UPDATE " + s.getTable() + " SET \"" + field +
			"\" = NULL WHERE " + s.getKeyName() + " = '" +
			s.getKey() + "';");
	}

	/** Create one storable record */
	public void create(Storable s) throws TMSException {
		validateSql(s.getKey());
		update("INSERT INTO " + s.getTable() + " (" + s.getKeyName() +
			") VALUES ('" + s.getKey() + "');");
	}

	/** Destroy one storable record */
	public void destroy(Storable s) throws TMSException {
		validateSql(s.getKey());
		update("DELETE FROM " + s.getTable() + " WHERE " +
			s.getKeyName() + " = '" + s.getKey() + "';");
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
			throw new TMSException(e);
		}
	}
}
