/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Iterator;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.tms.utils.Agency;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.SignPoller;
import us.mn.state.dot.vault.ObjectVault;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * The TMSImpl class is an RMI object which contains all the global traffic
 * management system object lists
 *
 * @author Douglas Lau
 */
final class TMSImpl extends TMSObjectImpl implements TMS {

	/** Worker thread */
	static protected final Scheduler TIMER =
		new Scheduler("Scheduler: TIMER");

	/** Detector data flush thread */
	static public final Scheduler FLUSH =
		new Scheduler("Scheduler: FLUSH");

	/** Open the ObjectVault */
	static protected void openVault(Properties props) throws IOException,
		TMSException, ObjectVaultException
	{
		vault = new ObjectVault(
			props.getProperty( "HostName" ),
			props.getProperty( "Port" ),
			props.getProperty( "Database" ),
			props.getProperty( "UserName" ),
			props.getProperty( "Password" )
		);
		vault.addRootTable( UnicastRemoteObject.class );
		vault.enableLogging( false );
		store = new SQLConnection(
			props.getProperty("HostName"),
			props.getProperty("Port"),
			props.getProperty("Database"),
			props.getProperty("UserName"),
			props.getProperty("Password")
		);
		R_NodeImpl.mapping = new TableMapping(store, "r_node",
			"detector");
		TrafficDeviceImpl.plan_mapping = new TableMapping(store,
			"traffic_device", "timing_plan");
	}

	/** Load the TMS root object from the ObjectVault */
	void loadFromVault() throws ObjectVaultException, TMSException,
		RemoteException
	{
		System.err.println("Loading detectors...");
		detectors.load(DetectorImpl.class, "index");
		System.err.println("Loading r_nodes...");
		// NOTE: must be called after detList is populated
		r_nodes.load(R_NodeImpl.class, "vault_oid");
		System.err.println( "Loading meters..." );
		meters.load( RampMeterImpl.class, "id" );
		System.err.println( "Loading dms list..." );
		dmss.load( DMSImpl.class, "id" );
		System.err.println("Loading warning signs...");
		warn_signs.load(WarningSignImpl.class, "id");
		System.err.println( "Loading cameras..." );
		cameras.load( CameraImpl.class, "id" );
		System.err.println( "Loading lane control signals..." );
		lcss.load( LaneControlSignalImpl.class, "id" );
	}

	/** determine agency specific polling time */
	public static int getAgencyPollTimerJobSigns() {
		if (Agency.isId(Agency.CALTRANS_D10))
			return(60);
		return(30);
	}

	/** Schedule all repeating jobs */
	public void scheduleJobs() {
		TIMER.addJob(new TimerJobSigns(TMSImpl.getAgencyPollTimerJobSigns()));
		TIMER.addJob(new TimerJob30Sec());
		TIMER.addJob(new TimerJob5Min());
		TIMER.addJob(new Job(Calendar.HOUR, 1) {
			public void perform() throws Exception {
				poll1Hour();
				System.out.println(new Date());
				Profile.printMemory(System.out);
				Profile.printThreads(System.out);
			}
		} );
		TIMER.addJob(new Job(Calendar.DATE, 1) {
			public void perform() throws Exception {
				poll1Day();
			}
		} );
		TIMER.addJob(new Job(Calendar.DATE, 1,
			Calendar.HOUR, 20)
		{
			public void perform() throws IOException {
				writeXmlConfiguration();
			}
		});
		TIMER.addJob(new Job(Calendar.DATE, 1,
			Calendar.HOUR, 4)
		{
			public void perform() throws Exception {
				System.err.println( "Performing download to"
					+ " all controllers @ " + new Date() );
				download();
			}
		} );
		TIMER.addJob(new Job(500) {
			public void perform() throws Exception {
				System.err.println( "Performing download to"
					+ " all controllers @ " + new Date() );
				download();
			}
		} );
		TIMER.addJob(new Job(1000) {
			public void perform() throws IOException {
				writeXmlConfiguration();
			}
		});
	}

	/** Write the TMS xml configuration files */
	protected void writeXmlConfiguration() throws IOException {
		System.err.println("Writing TMS XML files @ " + new Date());
		detectors.writeXml();
		r_nodes.writeXml();
		meters.writeXml();
		System.err.println("Completed TMS XML dump @ " + new Date());
	}

	/** Calculate the 30-second interval for the given time stamp */
	static protected int calculateInterval(Calendar stamp) {
		return stamp.get(Calendar.HOUR_OF_DAY) * 120 +
			stamp.get(Calendar.MINUTE) * 2 +
			stamp.get(Calendar.SECOND) / 30;
	}

	/** Sign polling timer job */
	protected class TimerJobSigns extends Job {

		/** Job completer */
		protected final Completer comp;

		/** Job to be performed on each completion */
		protected final Job job = new Job() {
			public void perform() {
				dmss.notifyStatus();
				warn_signs.notifyStatus();
				lcss.notifyStatus();
			}
		};

		/** Create a new sign polling timer job */
		protected TimerJobSigns(int intervalSecs) {
			super(Calendar.SECOND, intervalSecs, Calendar.SECOND, 4);
			comp = new Completer("Sign Poll", TIMER, job);
		}

		/** Perform the sign poll job */
		public void perform() throws Exception {
			if(!comp.checkComplete())
				return;
			comp.reset(Calendar.getInstance());
			try {
				pollSigns(comp);
			}
			finally {
				comp.makeReady();
			}
		}
	}

	/** 30-second timer job */
	protected class TimerJob30Sec extends Job {

		/** Job completer */
		protected final Completer comp;

		/** Current time stamp */
		protected Calendar stamp;

		/** Job to be performed on completion */
		protected final Job job = new Job() {
			public void perform() {
				detectors.calculateFakeFlows();
				detectors.writeSampleXml();
				station_map.calculateData(stamp);
				station_map.writeSampleXml();
				station_map.writeStationXml();
				int interval = calculateInterval(stamp);
				meters.computeDemand(interval);
				if(!isHoliday(stamp)) {
					meters.validateTimingPlans(interval);
					dmss.updateTravelTimes(interval);
				}
				meters.notifyStatus();
			}
		};

		/** Create a new 30-second timer job */
		protected TimerJob30Sec() {
			super(Calendar.SECOND, 30, Calendar.SECOND, 8);
			comp = new Completer("30-Second", TIMER, job);
		}

		/** Perform the 30-second timer job */
		public void perform() throws Exception {
			if(!comp.checkComplete())
				return;
			Calendar s = Calendar.getInstance();
			s.add(Calendar.SECOND, -30);
			stamp = s;
			comp.reset(stamp);
			try {
				poll30Second(comp);
			}
			finally {
				comp.makeReady();
			}
		}
	}

	/** 5-minute timer job */
	protected class TimerJob5Min extends Job {

		/** Job completer */
		protected final Completer comp;

		/** Current time stamp */
		protected Calendar stamp;

		/** Job to be performed on completion */
		protected final Job job = new Job(500) {
			public void perform() {
				detectors.flush(stamp);
			}
		};

		/** Create a new 5-minute timer job */
		protected TimerJob5Min() {
			super(Calendar.MINUTE, 5, Calendar.SECOND, 12);
			comp = new Completer("5-Minute", FLUSH, job);
		}

		/** Perform the 5-minute timer job */
		public void perform() throws Exception {
			if(!comp.checkComplete()) {
				// FIXME: print some error msg
				return;
			}
			stamp = Calendar.getInstance();
			Calendar s = (Calendar)stamp.clone();
			s.add(Calendar.MINUTE, -5);
			comp.reset(s);
			try {
				poll5Minute(comp);
			}
			finally {
				comp.makeReady();
			}
		}
	}

	/** Master detector list */
	protected final DetectorListImpl detectors;

	/** Master station list */
	protected final StationMapImpl station_map;

	/** Master r_node map */
	protected final R_NodeMapImpl r_nodes;

	/** Master timing plan list */
	protected final TimingPlanListImpl plans;

	/** Master ramp meter list */
	protected final RampMeterListImpl meters;

	/** Dynamic message sign list */
	protected final DMSListImpl dmss;

	/** Warning sign list */
	protected final WarningSignList warn_signs;

	/** Camera list */
	protected final CameraList cameras;

	/** Lane Control Signals list */
	protected final LCSListImpl lcss;

	/** Initialize the subset list */
	protected void initialize() throws RemoteException {
		// This is an ugly hack, but it works
		detList = detectors;
		statMap = station_map;
		nodeMap = r_nodes;
		planList = plans;
		meterList = meters;
		dmsList = dmss;
		warnList = warn_signs;
		availableMeters = (SubsetList)meters.getAvailableList();
		cameraList = cameras;
		lcsList = lcss;
	}

	/** Create a new TMS object */
	TMSImpl(Properties props) throws IOException, TMSException,
		ObjectVaultException
	{
		super();
		detectors = new DetectorListImpl();
		station_map = new StationMapImpl();
		r_nodes = new R_NodeMapImpl();
		plans = new TimingPlanListImpl();
		meters = new RampMeterListImpl();
		dmss = new DMSListImpl();
		warn_signs = new WarningSignList();
		cameras = new CameraList();
		lcss = new LCSListImpl();
		initialize();
		openVault(props);
	}

	/** Get the detector list */
	public IndexedList getDetectorList() { return detectors; }

	/** Get the station map */
	public StationMap getStationMap() {
		return station_map;
	}

	/** Get the r_node map */
	public R_NodeMap getR_NodeMap() {
		return r_nodes;
	}

	/** Get the timing plan list */
	public TimingPlanList getTimingPlanList() { return plans; }

	/** Get the ramp meter list */
	public DeviceList getRampMeterList() {
		return meters;
	}

	/** Get the dynamic message sign list */
	public DMSList getDMSList() { return dmss; }

	/** Get the warning sign list */
	public DeviceList getWarningSignList() {
		return warn_signs;
	}

	/** Get the camera list */
	public DeviceList getCameraList() {
		return cameras;
	}

	/** Get the lane control signal list */
	public LCSList getLCSList() {
		return lcss;
	}

	/** Get a TMS object by its object ID */
	public TMSObject getObject(int oid) {
		Object obj = vault.getObject(oid);
		if(obj instanceof TMSObject)
			return (TMSObject)obj;
		else
			return null;
	}

	/** Download to all controllers */
	static protected void download() throws NamespaceError {
		namespace.findObject(Controller.SONAR_TYPE,
			new Checker<ControllerImpl>()
		{
			public boolean check(ControllerImpl c) {
				c.setDownload(false);
				return false;
			}
		});
	}

	/** Poll all controllers 30 second interval */
	static protected void poll30Second(final Completer comp)
		throws NamespaceError
	{
		namespace.findObject(Controller.SONAR_TYPE,
			new Checker<ControllerImpl>()
		{
			public boolean check(ControllerImpl c) {
				MessagePoller p = c.getPoller();
				if(p != null)
					p.poll30Second(c, comp);
				return false;
			}
		});
	}

	/** Poll all controllers 5 minute interval */
	static protected void poll5Minute(final Completer comp)
		throws NamespaceError
	{
		namespace.findObject(Controller.SONAR_TYPE,
			new Checker<ControllerImpl>()
		{
			public boolean check(ControllerImpl c) {
				MessagePoller p = c.getPoller();
				if(p != null)
					p.poll5Minute(c, comp);
				return false;
			}
		});
	}

	/** Poll all controllers for 1 hour interval */
	static protected void poll1Hour() throws NamespaceError {
		namespace.findObject(Controller.SONAR_TYPE,
			new Checker<ControllerImpl>()
		{
			public boolean check(ControllerImpl c) {
				c.resetPeriod(ErrorCounter.PERIOD_1_HOUR);
				return false;
			}
		});
	}

	/** Poll all controllers for 1 day interval */
	static protected void poll1Day() throws NamespaceError {
		namespace.findObject(Controller.SONAR_TYPE,
			new Checker<ControllerImpl>()
		{
			public boolean check(ControllerImpl c) {
				c.resetPeriod(ErrorCounter.PERIOD_1_DAY);
				return false;
			}
		});
	}

	/** Poll all controllers for sign status */
	static protected void pollSigns(final Completer comp)
		throws NamespaceError
	{
		namespace.findObject(Controller.SONAR_TYPE,
			new Checker<ControllerImpl>()
		{
			public boolean check(ControllerImpl c) {
				MessagePoller p = c.getPoller();
				if(p instanceof SignPoller) {
					SignPoller sp = (SignPoller)p;
					sp.pollSigns(c, comp);
				}
				return false;
			}
		});
	}
}
