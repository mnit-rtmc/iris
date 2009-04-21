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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Iterator;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.SignPoller;
import us.mn.state.dot.tms.comm.VideoMonitorPoller;
import us.mn.state.dot.vault.ObjectVault;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * The TMSImpl class is an RMI object which contains all the global traffic
 * management system object lists
 *
 * @author Douglas Lau
 */
public final class TMSImpl extends TMSObjectImpl implements TMS {

	/** Detector sample file */
	static protected final String SAMPLE_XML = "det_sample.xml";

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
			props.getProperty("db.url"),
			props.getProperty("db.user"),
			props.getProperty("db.password")
		);
		vault.addRootTable( UnicastRemoteObject.class );
		vault.enableLogging( false );
		store = new SQLConnection(
			props.getProperty("db.url"),
			props.getProperty("db.user"),
			props.getProperty("db.password")
		);
	}

	/** Load the TMS root object from the ObjectVault */
	void loadFromVault() throws ObjectVaultException, TMSException,
		RemoteException
	{
		System.err.println( "Loading lane control signals..." );
		lcss.load( LaneControlSignalImpl.class, "id" );
	}

	/** Station manager */
	protected transient StationManager station_manager;

	/** Schedule all repeating jobs */
	public void scheduleJobs() {
		station_manager = new StationManager(namespace);
		TIMER.addJob(new TimerJobSigns(
			SystemAttributeHelper.getDmsPollFreqSecs()));
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
			public void perform() throws Exception {
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
			public void perform() throws Exception {
				writeXmlConfiguration();
			}
		});
	}

	/** Write the TMS xml configuration files */
	protected void writeXmlConfiguration() throws IOException,
		NamespaceError
	{
		System.err.println("Writing TMS XML files @ " + new Date());
		new DetectorXmlWriter(namespace).write();
		corridors = new CorridorManager(namespace);
		new R_NodeXmlWriter(corridors).write();
		new RampMeterXmlWriter(namespace).write();
		System.err.println("Completed TMS XML dump @ " + new Date());
	}

	/** Write the sample data out as XML */
	public void writeSampleXml() throws IOException, NamespaceError {
		XmlWriter w = new XmlWriter(SAMPLE_XML, true) {
			public void print(PrintWriter out) {
				printSampleXmlHead(out);
				printSampleXmlBody(out);
				printSampleXmlTail(out);
			}
		};
		w.write();
	}

	/** Print the header of the detector sample XML file */
	protected void printSampleXmlHead(PrintWriter out) {
		out.println("<?xml version='1.0'?>");
		out.println("<!DOCTYPE traffic_sample SYSTEM 'tms.dtd'>");
		out.println("<traffic_sample time_stamp='" + new Date() +
			"' period='30'>");
		out.println("\t&detectors;");
	}

	/** Print the body of the detector sample XML file */
	static protected void printSampleXmlBody(final PrintWriter out) {
		namespace.findObject(Detector.SONAR_TYPE,
			new Checker<DetectorImpl>()
		{
			public boolean check(DetectorImpl det) {
				det.calculateFakeData();
				det.printSampleXmlElement(out);
				return false;
			}
		});
	}

	/** Print the tail of the detector sample XML file */
	protected void printSampleXmlTail(PrintWriter out) {
		out.println("</traffic_sample>");
	}

	/** Sign polling timer job */
	protected class TimerJobSigns extends Job {

		/** Job completer */
		protected final Completer comp;

		/** Job to be performed on each completion */
		protected final Job job = new Job() {
			public void perform() {
				lcss.notifyStatus();
			}
		};

		/** Create a new sign polling timer job */
		protected TimerJobSigns(int intervalSecs) {
			super(Calendar.SECOND, intervalSecs, Calendar.SECOND,4);
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
				try {
					writeSampleXml();
					station_manager.calculateData(stamp);
					station_manager.writeSampleXml();
					station_manager.writeStationXml();
				}
				catch(IOException e) {
					e.printStackTrace();
				}
				catch(NamespaceError e) {
					e.printStackTrace();
				}
				if(!isHoliday(stamp))
					validateTimingPlans();
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

	/** Validate all timing plans */
	protected void validateTimingPlans() {
		lookupTimingPlans(new Checker<TimingPlan>() {
			public boolean check(TimingPlan p) {
				TimingPlanImpl plan = (TimingPlanImpl)p;
				plan.validate();
				return false;
			}
		});
		StratifiedPlanState.processAllStates();
		namespace.findObject(RampMeter.SONAR_TYPE,
			new Checker<RampMeterImpl>()
		{
			public boolean check(RampMeterImpl m) {
				m.updateRatePlanned();
				return false;
			}
		});
		namespace.findObject(DMS.SONAR_TYPE, new Checker<DMSImpl>() {
			public boolean check(DMSImpl s) {
				s.updateTravelTime();
				return false;
			}
		});
	}

	/** 5-minute timer job */
	protected class TimerJob5Min extends Job {

		/** Job completer */
		protected final Completer comp;

		/** Current time stamp */
		protected Calendar stamp;

		/** Job to be performed on completion */
		protected final Job job = new Job(500) {
			public void perform() throws NamespaceError {
				flushDetectorData(stamp);
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

	/** Lane Control Signals list */
	protected final LCSListImpl lcss;

	/** Initialize the subset list */
	protected void initialize() throws RemoteException {
		// This is an ugly hack, but it works
		lcsList = lcss;
	}

	/** Create a new TMS object */
	TMSImpl(Properties props) throws IOException, TMSException,
		ObjectVaultException
	{
		super();
		lcss = new LCSListImpl();
		initialize();
		openVault(props);
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
		namespace.findObject(DMS.SONAR_TYPE, new Checker<DMSImpl>() {
			public boolean check(DMSImpl s) {
				s.setSignRequest(SignRequest.
					QUERY_PIXEL_FAILURES.ordinal());
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

	/** Flush the detector data to disk */
	static protected void flushDetectorData(final Calendar stamp)
		throws NamespaceError
	{
		System.err.println("Starting FLUSH @ " + new Date() + " for " +
			stamp.getTime());
		namespace.findObject(Detector.SONAR_TYPE,
			new Checker<DetectorImpl>()
		{
			public boolean check(DetectorImpl det) {
				det.flush(stamp);
				return false;
			}
		});
		System.err.println("Finished FLUSH @ " + new Date());
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

	/** Select a camera on a video monitor */
	static public void selectMonitorCamera(final VideoMonitor m,
		final String cam) throws NamespaceError
	{
		namespace.findObject(Controller.SONAR_TYPE,
			new Checker<ControllerImpl>()
		{
			public boolean check(ControllerImpl c) {
				MessagePoller p = c.getPoller();
				if(p instanceof VideoMonitorPoller) {
					VideoMonitorPoller vmp =
						(VideoMonitorPoller)p;
					vmp.setMonitorCamera(c, m, cam);
				}
				return false;
			}
		});
	}

	/** Unpublish a camera */
	static public void unpublishCamera(final Camera cam)
		throws NamespaceError, TMSException
	{
		final LinkedList<VideoMonitorImpl> restricted =
			new LinkedList<VideoMonitorImpl>();
		namespace.findObject(VideoMonitor.SONAR_TYPE,
			new Checker<VideoMonitorImpl>()
		{
			public boolean check(VideoMonitorImpl m) {
				if(m.getRestricted() && (m.getCamera() == cam))
					restricted.add(m);
				return false;
			}
		});
		for(VideoMonitorImpl m: restricted)
			m.selectCamera("");
	}

	/** Lookup timing plans plans */
	static public void lookupTimingPlans(Checker<TimingPlan> checker) {
		namespace.findObject(TimingPlan.SONAR_TYPE, checker);
	}
}
