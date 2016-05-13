/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ProxySelector;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.server.Server;
import us.mn.state.dot.tms.BaseHelper;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.event.BaseEvent;
import us.mn.state.dot.tms.utils.HTTPProxySelector;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.PropertyLoader;

/**
 * This is the main class to start the IRIS server.
 *
 * @author Douglas Lau
 */
public class MainServer {

	/** Location of IRIS property configuration file */
	static private final String PROP_FILE =
		"/etc/iris/iris-server.properties";

	/** Directory to store IRIS log files */
	static private final String LOG_FILE_DIR = "/var/log/iris/";

	/** File to log standard out stream */
	static private final String STD_OUT = LOG_FILE_DIR + "iris.stdout";

	/** File to log standard error stream */
	static private final String STD_ERR = LOG_FILE_DIR + "iris.stderr";

	/** Timer thread for repeating jobs */
	static private final Scheduler TIMER = new Scheduler("timer");

	/** Flush thread for disk writing jobs */
	static public final Scheduler FLUSH = new Scheduler("flush");

	/** Sample archive factory */
	static public final SampleArchiveFactoryImpl a_factory =
		new SampleArchiveFactoryImpl();

	/** SONAR server */
	static public Server server;

	/** Authentication provider */
	static public IrisProvider auth_provider;

	/** SQL connection */
	static private SQLConnection store;

	/** Agency district property */
	static private String district = "tms";

	/** Get the district ID */
	static public String districtId() {
		return district;
	}

	/** Main server entry point */
	static public void main(String[] args) {
		try {
			initialize();
			Properties props = PropertyLoader.load(PROP_FILE);
			district = props.getProperty("district", "tms");
			initProxySelector(props);
			store = createStore(props);
			BaseEvent.store = store;
			I18N.initialize(props);
			WhitelistNamespace ns = createNamespace(props);
			IrisCapabilityImpl.lookup(store, ns);
			IrisPrivilegeImpl.lookup(store, ns);
			IrisRoleImpl.lookup(store, ns);
			IrisUserImpl.lookup(store, ns);
			BaseObjectImpl.loadAll(store, ns);
			scheduleTimerJobs();
			scheduleFlushJobs();
			server = new Server(ns, props, new AccessLogger(FLUSH));
			auth_provider = new IrisProvider();
			server.addProvider(auth_provider);
			System.err.println("IRIS Server active");
			server.join();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/** Initialize the server process */
	static private void initialize() throws IOException {
		redirectStdStreams();
		DebugLog.init(new File(LOG_FILE_DIR),
			"IRIS @@VERSION@@ restarted");
		checkAssert();
	}

	/** Redirect the standard output and error streams to log files */
	static private void redirectStdStreams() throws IOException {
		System.setOut(createPrintStream(STD_OUT));
		System.setErr(createPrintStream(STD_ERR));
		String msg = "IRIS @@VERSION@@ restarted @ " +
			TimeSteward.getDateInstance();
		System.out.println(msg);
		System.err.println(msg);
	}

	/** Create a buffered print stream to a log file */
	static private PrintStream createPrintStream(String fname)
		throws IOException
	{
		FileOutputStream fos = new FileOutputStream(fname, true);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		return new PrintStream(bos, true);
	}

	/** Check assertion status */
	static private void checkAssert() {
		boolean assertsEnabled = false;
		// Intentional assignment side-effect
		assert assertsEnabled = true;
		System.err.println("Assertions are turned " +
			(assertsEnabled ? "on" : "off") + ".");
	}

	/** Initialize the proxy selector */
	static private void initProxySelector(Properties props) {
		HTTPProxySelector ps = new HTTPProxySelector(props);
		if(ps.hasProxies())
			ProxySelector.setDefault(ps);
	}

	/** Create the database connection */
	static private SQLConnection createStore(Properties props)
		throws IOException, TMSException
	{
		return new SQLConnection(
			props.getProperty("db.url"),
			props.getProperty("db.user"),
			props.getProperty("db.password")
		);
	}

	/** Create the server namespace */
	static private WhitelistNamespace createNamespace(Properties props)
		throws UnknownHostException, NumberFormatException
	{
		WhitelistNamespace ns = new WhitelistNamespace(props);
		// FIXME: static namespace hacks
		BaseHelper.namespace = ns;
		ns.registerType(Station.SONAR_TYPE, StationImpl.class);
		return ns;
	}

	/** Schedule jobs on TIMER thread */
	static private void scheduleTimerJobs() {
		TIMER.addJob(new DmsQueryStatusJob());
		TIMER.addJob(new DmsQueryDialupJob());
		TIMER.addJob(new MeteringJob(FLUSH));
		TIMER.addJob(new SampleQuery5MinJob());
		TIMER.addJob(new ActionPlanJob());
		TIMER.addJob(new CameraWiperJob());
		TIMER.addJob(new SendSettingsJob());
		TIMER.addJob(new SendSettingsJob(500));
		TIMER.addJob(new TollZoneJob());
		TIMER.addJob(new ReaperJob());
	}

	/** Schedule jobs on FLUSH thread */
	static private void scheduleFlushJobs() {
		FLUSH.addJob(new FlushSamplesJob(a_factory));
		FLUSH.addJob(new ArchiveSamplesJob(a_factory));
		FLUSH.addJob(new ProfilingJob());
		FLUSH.addJob(new XmlConfigJob());
		FLUSH.addJob(new XmlConfigJob(1000));
		FLUSH.addJob(new SignMessageXmlJob());
		FLUSH.addJob(new IncidentXmlJob());
		FLUSH.addJob(new EventPurgeJob());
	}
}
