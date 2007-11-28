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
package us.mn.state.dot.tms;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import us.mn.state.dot.sonar.PropertyLoader;
import us.mn.state.dot.sonar.server.Namespace;
import us.mn.state.dot.sonar.server.Server;
import us.mn.state.dot.tms.log.TMSEvent;

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
	static protected final String LOG_FILE_DIR = "/var/log/tms/";

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

	/** Start the server and register it with the RMI registry */
	static public void main(String[] args) {
		try {
			redirectStdStreams();
			Properties props = PropertyLoader.load(PROP_FILE);
			RMISocketFactory.setSocketFactory(
				new TmsSocketFactory());
			TMSImpl tms = new TMSImpl();
			tms.loadFromVault(props);
			tms.scheduleJobs();
			LoginImpl login = new LoginImpl(tms);
			LocateRegistry.createRegistry(
				Registry.REGISTRY_PORT);
			Naming.bind("//localhost/login", login);
			Namespace ns = new Namespace();
			IrisRoleImpl.lookup(TMSObjectImpl.store, ns);
			IrisUserImpl.lookup(TMSObjectImpl.store, ns);
			BaseObjectImpl.loadAll(TMSObjectImpl.store, ns);
			Server server = new Server(ns, props);
			TMSObjectImpl.eventLog.add(new TMSEvent("System",
				TMSEvent.SYSTEM_RESTARTED,
				Calendar.getInstance()));
			System.err.println("IRIS Server active");
			server.join();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
