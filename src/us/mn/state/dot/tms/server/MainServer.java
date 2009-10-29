/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.sonar.server.Server;
import us.mn.state.dot.tms.BaseHelper;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.event.BaseEvent;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.PropertyLoader;

/**
 * This is the main class to start the IRIS server.
 *
 * @author Douglas Lau
 */
public class MainServer {

	/** Location of IRIS property configuration file */
	static protected final String PROP_FILE =
		"/etc/iris/iris-server.properties";

	/** Directory to store IRIS log files */
	static protected final String LOG_FILE_DIR = "/var/log/iris/";

	/** File to log standard out stream */
	static protected final String STD_OUT = LOG_FILE_DIR + "iris.stdout";

	/** File to log standard error stream */
	static protected final String STD_ERR = LOG_FILE_DIR + "iris.stderr";

	/** Redirect the standard output and error streams to log files */
	static protected void redirectStdStreams() throws IOException {
		FileOutputStream fos = new FileOutputStream(STD_OUT, true);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		System.setOut(new PrintStream(bos, true));
		fos = new FileOutputStream(STD_ERR, true);
		bos = new BufferedOutputStream(fos);
		System.setErr(new PrintStream(bos, true));
		String msg = "IRIS @@VERSION@@ restarted @ " + new Date();
		System.out.println(msg);
		System.err.println(msg);
	}

	/** Timer thread for repeating jobs */
	static protected final Scheduler TIMER =
		new Scheduler("Scheduler: TIMER");

	/** Flush thread for disk writing jobs */
	static public final Scheduler FLUSH =
		new Scheduler("Scheduler: FLUSH");

	/** SONAR server */
	static public Server server;

	/** SQL connection */
	static protected SQLConnection store;

	/** Main server entry point */
	static public void main(String[] args) {
		try {
			redirectStdStreams();
			sanityChecks();
			Properties props = PropertyLoader.load(PROP_FILE);
			store = createStore(props);
			I18N.initialize(props);
			ServerNamespace ns = new ServerNamespace();
			// FIXME: static namespace hacks
			DMSList.namespace = ns;
			BaseHelper.namespace = ns;
			IrisRoleImpl.lookup(store, ns);
			IrisPrivilegeImpl.lookup(store, ns);
			IrisUserImpl.lookup(store, ns);
			ns.registerType(Station.SONAR_TYPE, StationImpl.class);
			ns.registerType(SignMessage.SONAR_TYPE,
				SignMessageImpl.class);
			BaseObjectImpl.loadAll(store, ns);
			BaseEvent.store = store;
			scheduleTimerJobs();
			scheduleFlushJobs();
			server = new Server(ns, props);
			System.err.println("IRIS Server active");
			server.join();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/** Perform sanity and debug checks */
	static protected void sanityChecks() {
		if(!TimeZone.getDefault().useDaylightTime()) {
			System.err.println("Warning: the default time zone ("+
			TimeZone.getDefault().getDisplayName() +
			") doesn't support DST. Specify the time zone via the command line.");
		}
	}

	/** Create the database connection */
	static protected SQLConnection createStore(Properties props)
		throws IOException, TMSException
	{
		return new SQLConnection(
			props.getProperty("db.url"),
			props.getProperty("db.user"),
			props.getProperty("db.password")
		);
	}

	/** Schedule jobs on TIMER thread */
	static protected void scheduleTimerJobs() {
		int secs = SystemAttrEnum.DMS_POLL_FREQ_SECS.getInt();
		if(secs > 5) {
			TIMER.addJob(new DmsQueryMsgJob(secs));
			TIMER.addJob(new LcsQueryMsgJob(secs));
			TIMER.addJob(new WarnQueryStatusJob(secs));
		}
		TIMER.addJob(new DmsQueryStatusJob());
		TIMER.addJob(new AlarmQueryStatusJob());
		TIMER.addJob(new SampleQuery30SecJob(FLUSH));
		TIMER.addJob(new SampleQuery5MinJob(FLUSH));
		TIMER.addJob(new ActionPlanJob(TIMER));
		TIMER.addJob(new CameraNoFailJob());
		TIMER.addJob(new SendSettingsJob());
		TIMER.addJob(new SendSettingsJob(500));
	}

	/** Schedule jobs on FLUSH thread */
	static protected void scheduleFlushJobs() {
		FLUSH.addJob(new ProfilingJob());
		FLUSH.addJob(new KmlWriterJob());
		FLUSH.addJob(new XmlConfigJob());
		FLUSH.addJob(new XmlConfigJob(1000));
		FLUSH.addJob(new DmsXmlJob());
		FLUSH.addJob(new IncidentXmlJob());
	}
}
