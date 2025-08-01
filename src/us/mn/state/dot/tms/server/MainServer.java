/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
 * Copyright (C) 2017  Iteris Inc.
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
import java.net.Authenticator;
import java.net.ProxySelector;
import java.util.Properties;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.server.Server;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.tms.BaseHelper;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.event.BaseEvent;
import us.mn.state.dot.tms.server.comm.cux50.CUx50;
import us.mn.state.dot.tms.server.comm.cux50.PrServer;
import us.mn.state.dot.tms.utils.DevelCfg;
import us.mn.state.dot.tms.utils.HttpProxySelector;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.PropertyLoader;

/**
 * This is the main class to start the IRIS server.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class MainServer {

	/** Location of IRIS property configuration file */
	static private final String PROP_FILE =
		DevelCfg.get("server.prop.file",
			"/etc/iris/iris-server.properties");

	/** Directory to store IRIS log files */
	static private final String LOG_FILE_DIR =
		DevelCfg.get("log.output.dir", "/var/log/iris/");

	/** File to log standard out stream */
	static private final String STD_OUT = LOG_FILE_DIR + "iris.stdout";

	/** File to log standard error stream */
	static private final String STD_ERR = LOG_FILE_DIR + "iris.stderr";

	/** Timer thread for repeating jobs */
	static public final Scheduler TIMER = new Scheduler("timer");

	/** Flush thread for disk writing jobs */
	static public final Scheduler FLUSH = new Scheduler("flush");

	/** Sample archive factory */
	static public final SampleArchiveFactoryImpl a_factory =
		new SampleArchiveFactoryImpl();

	/** SONAR server */
	static public Server server;

	/** Hash password authentication provider */
	static public HashProvider hash_provider;

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
			GateArmArrayImpl.initAllowList(props);
			ServerNamespace ns = createNamespace();
			BaseObjectImpl.loadAll(store, ns);
			scheduleTimerJobs();
			scheduleFlushJobs();
			startProtocolServer();
			hash_provider = new HashProvider();
			server = new Server(ns, props, new AccessLogger(FLUSH),
				hash_provider);
			System.err.println("IRIS Server active");
			server.join();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Initialize the server process */
	static private void initialize() throws IOException {
		redirectStdStreams();
		DebugLog.init(new File(LOG_FILE_DIR), DevelCfg.get(
			"log.start.msg", "IRIS @@VERSION@@ restarted"));
		checkAssert();
	}

	/** Redirect the standard output and error streams to log files */
	static private void redirectStdStreams() throws IOException {

		// Developer option to NOT redirect console out/err
		// to a logfile when running in Eclipse...:
		// To use, in Eclipse, go to Run > Run Configurations
		// > (MainServer or MainClient) > Arguments, and add
		// "-DrunInEclipse=true" (without the quotes) to the
		// VM arguments box.
		
		String inEclipseStr = System.getProperty("runInEclipse");
		if (inEclipseStr == null)
			inEclipseStr = DevelCfg.get("runInEclipse");
		if ((inEclipseStr == null) || !inEclipseStr.equalsIgnoreCase("true")) {
			System.setOut(createPrintStream(STD_OUT));
			System.setErr(createPrintStream(STD_ERR));
			String msg = DevelCfg.get("log.start.msg",
				"IRIS @@VERSION@@ restarted") + " @ "
				+ TimeSteward.getDateInstance();
			System.out.println(msg);
			System.err.println(msg);
		}
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
		HttpProxySelector ps = new HttpProxySelector(props);
		if (ps.hasProxies()) {
			ProxySelector.setDefault(ps);
			Authenticator.setDefault(ps.getAuthenticator());
		}
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
	static private ServerNamespace createNamespace() throws SonarException {
		ServerNamespace ns = new ServerNamespace();
		// FIXME: static namespace hacks
		BaseHelper.namespace = ns;
		ns.registerType(StationImpl.class);
		ns.registerType(DomainImpl.class);
		ns.registerType(RoleImpl.class);
		ns.registerType(PermissionImpl.class);
		ns.registerType(UserImpl.class);
		ns.registerType(SystemAttributeImpl.class);
		ns.registerType(GraphicImpl.class);
		ns.registerType(FontImpl.class);
		ns.registerType(GlyphImpl.class);
		ns.registerType(RoadImpl.class);
		ns.registerType(RoadAffixImpl.class);
		ns.registerType(GeoLocImpl.class);
		ns.registerType(MapExtentImpl.class);
		ns.registerType(IncidentDetailImpl.class);
		ns.registerType(CommConfigImpl.class);
		ns.registerType(CommLinkImpl.class);
		ns.registerType(ModemImpl.class);
		ns.registerType(CabinetStyleImpl.class);
		ns.registerType(ControllerImpl.class);
		ns.registerType(SignConfigImpl.class);
		ns.registerType(SignDetailImpl.class);
		ns.registerType(DayPlanImpl.class);
		ns.registerType(DayMatcherImpl.class);
		ns.registerType(PlanPhaseImpl.class);
		ns.registerType(ActionPlanImpl.class);
		ns.registerType(R_NodeImpl.class);
		ns.registerType(AlarmImpl.class);
		ns.registerType(GpsImpl.class);
		ns.registerType(CameraTemplateImpl.class);
		ns.registerType(VidSourceTemplateImpl.class);
		ns.registerType(CameraVidSourceOrderImpl.class);
		ns.registerType(DetectorImpl.class);
		ns.registerType(TollZoneImpl.class);
		ns.registerType(EncoderTypeImpl.class);
		ns.registerType(EncoderStreamImpl.class);
		ns.registerType(CameraImpl.class);
		ns.registerType(CameraPresetImpl.class);
		ns.registerType(PlayListImpl.class);
		ns.registerType(MonitorStyleImpl.class);
		ns.registerType(VideoMonitorImpl.class);
		ns.registerType(FlowStreamImpl.class);
		ns.registerType(BeaconImpl.class);
		ns.registerType(WeatherSensorImpl.class);
		ns.registerType(RampMeterImpl.class);
		ns.registerType(SignMessageImpl.class);
		ns.registerType(DMSImpl.class);
		ns.registerType(MsgPatternImpl.class);
		ns.registerType(MsgLineImpl.class);
		ns.registerType(GateArmArrayImpl.class);
		ns.registerType(GateArmImpl.class);
		ns.registerType(TagReaderImpl.class);
		ns.registerType(LcsImpl.class);
		ns.registerType(LcsStateImpl.class);
		ns.registerType(ParkingAreaImpl.class);
		ns.registerType(IncidentImpl.class);
		ns.registerType(IncDescriptorImpl.class);
		ns.registerType(IncLocatorImpl.class);
		ns.registerType(IncAdviceImpl.class);
		ns.registerType(TimeActionImpl.class);
		ns.registerType(DeviceActionImpl.class);
		ns.registerType(WordImpl.class);
		ns.registerType(RptConduitImpl.class);
		ns.registerType(AlertConfigImpl.class);
		ns.registerType(AlertMessageImpl.class);
		ns.registerType(AlertInfoImpl.class);
		return ns;
	}

	/** Schedule jobs on TIMER thread */
	static private void scheduleTimerJobs() {
		TIMER.addJob(new MeteringJob(FLUSH));
		TIMER.addJob(new LockExpireJob());
		TIMER.addJob(new SendSettingsJob());
		TIMER.addJob(new SendSettingsJob(500));
		TIMER.addJob(new TollZoneJob());
		TIMER.addJob(new ParkingAreaJob());
		TIMER.addJob(new ReaperJob());
		TIMER.addJob(new ActionPlanJob(TIMER));
	}

	/** Schedule jobs on FLUSH thread */
	static private void scheduleFlushJobs() {
		FLUSH.addJob(new FlushSamplesJob(a_factory));
		FLUSH.addJob(new ArchiveSamplesJob(a_factory));
		FLUSH.addJob(new ProfilingJob());
		FLUSH.addJob(new CreateCorridorsJob(FLUSH));
		FLUSH.addJob(new XmlConfigJob(1000));
		FLUSH.addJob(new SignMessageXmlJob());
		FLUSH.addJob(new IncidentXmlJob());
		FLUSH.addJob(new WeatherSensorXmlJob());
		FLUSH.addJob(new EventPurgeJob());
	}

	/** Start the protocol server */
	static private void startProtocolServer() {
		// FIXME: need to restart server on change
		if (isPanasonicKeyboardEnabled()) {
			try {
				PrServer ps = new PrServer();
				ps.listen(7001, new CUx50());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/** Is panasonic keyboard enabled? */
	static private boolean isPanasonicKeyboardEnabled() {
		return SystemAttrEnum.CAMERA_KBD_PANASONIC_ENABLE.getBoolean();
	}
}
