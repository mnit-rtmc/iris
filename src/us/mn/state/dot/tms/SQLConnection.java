/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2007  Minnesota Department of Transportation
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

/**
 * Simple SQL database abstraction stuff
 *
 * @author Douglas Lau
 */
public class SQLConnection {

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
		close();
		connection = DriverManager.getConnection(location, user,
			password);
		connection.setAutoCommit(true);
	}

	/** Create a database statement */
	protected Statement createStatement() throws TMSException {
		System.err.println("SQLConnection.createStatement()");
		try {
			if(connection == null)
				open();
			return connection.createStatement();
		}
		catch(SQLException e) {
			e.printStackTrace();
			try {
				open();
				return connection.createStatement();
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
