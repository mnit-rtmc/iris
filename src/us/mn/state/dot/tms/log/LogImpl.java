/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2006  Minnesota Department of Transportation
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

import java.util.Calendar;

import us.mn.state.dot.tms.TMSException;

/**
 * This is the implementation of the log interface
 *
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class LogImpl implements Log {

	/** Number of days to keep records when purging comm line events */
	static protected final int COMM_LINE_PURGE_THRESHOLD = 7;

	/** EventVault object used to retrieve events */
	private final EventVault vault;

	/** Number of milliseconds for the purgeThread to wait before attempting
	 * another purge
	 */
	private int sleepDuration = 60*60*24*1000; // number of milliseconds in a day

	/** Thread that controls purging the database of old records. */
	Thread purgeThread = new Thread() {
		public void run() {
			try {
				while ( true ) {
					purge();
					sleep( sleepDuration );
				}
			} catch ( Exception e ) {
				// do nothing
			}
		}
	};


	/** Constructor for the LogImpl class */
	public LogImpl( String host, String prt, String db, String user, String pwd )
		throws TMSException
	{
		try {
			String hostIP = host;
			String port = prt;
			String dbName = db;
			String userName = user;
			String password = pwd;
			vault = new EventVault( hostIP, port, dbName, userName, password );
			purgeThread.start();
		} catch ( Exception e ) {
			throw ( new TMSException( e ) );
		}
	}


	/** Add an object to the log */
	public void add( Object object )
		throws TMSException
	{
		try {
			vault.add( object );
		} catch ( EventVaultException eve ) {
			throw ( new TMSException( eve ) );
		}
	}

	
	/** Purge old records from the database */
	public void purge() throws TMSException {
		try {
			vault.purgeCommunicationLineEvents(
				COMM_LINE_PURGE_THRESHOLD);
			System.out.println("Communication Line Events purged " +
				"on " + Calendar.getInstance().getTime() +
				" at " + COMM_LINE_PURGE_THRESHOLD +
				" day cutoff.");
		} catch(EventVaultException e) {
			throw new TMSException(e);
		}
	}
}
