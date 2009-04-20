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
import java.util.TimeZone;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.sonar.server.Server;
import us.mn.state.dot.tms.event.BaseEvent;
import us.mn.state.dot.tms.utils.I18NMessages;
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

	/** SONAR server */
	static public Server server;

	/** Start the server and register it with the RMI registry */
	static public void main(String[] args) {
		try {
			redirectStdStreams();
			sanityChecks();
			Properties props = PropertyLoader.load(PROP_FILE);
			TMSImpl tms = new TMSImpl(props);
			I18NMessages.initialize(props);
			ServerNamespace ns = new ServerNamespace();
			// FIXME: static namespace hacks
			TMSObjectImpl.namespace = ns;
			SystemAttributeHelper.namespace = ns;
			IrisRoleImpl.lookup(TMSObjectImpl.store, ns);
			IrisUserImpl.lookup(TMSObjectImpl.store, ns);
			ns.registerType(Station.SONAR_TYPE, StationImpl.class);
			ns.registerType(SignMessage.SONAR_TYPE,
				SignMessageImpl.class);
			BaseObjectImpl.loadAll(TMSObjectImpl.store, ns);
			BaseEvent.store = TMSObjectImpl.store;
			RMISocketFactory.setSocketFactory(
				new TmsSocketFactory());
			tms.loadFromVault();
			tms.scheduleJobs();
			LoginImpl login = new LoginImpl(tms);
			LocateRegistry.createRegistry(
				Registry.REGISTRY_PORT);
			Naming.bind("//localhost/login", login);
			server = new Server(ns, props);
			System.err.println("IRIS Server active for " +
				SystemAttributeHelper.agencyId() + ".");
			server.join();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/** perform sanity and debug checks */
	static public void sanityChecks() {

		// does the default time zone support DST?
		if (!TimeZone.getDefault().useDaylightTime()) {
			System.err.println("Warning: the default time zone ("+
			TimeZone.getDefault().getDisplayName()+
			") doesn't support DST. Specify the time zone via the command line.");
		}
	}
}
