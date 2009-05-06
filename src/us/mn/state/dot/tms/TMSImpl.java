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
import java.util.ArrayList;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.tms.comm.DMSPoller;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.VideoMonitorPoller;
import us.mn.state.dot.tms.comm.WarningSignPoller;
import us.mn.state.dot.tms.kml.KmlDocument;
import us.mn.state.dot.tms.kml.KmlFolder;
import us.mn.state.dot.tms.kml.KmlFeature;
import us.mn.state.dot.tms.kml.KmlFile;
import us.mn.state.dot.tms.kml.KmlRenderer;
import us.mn.state.dot.tms.kml.KmlStyleSelector;

/**
 * The TMSImpl class is an RMI object which contains all the global traffic
 * management system object lists
 *
 * @author Douglas Lau
 */
public final class TMSImpl implements KmlDocument {

	/** Detector sample file */
	static protected final String SAMPLE_XML = "det_sample.xml";

	/** Worker thread */
	static protected final Scheduler TIMER =
		new Scheduler("Scheduler: TIMER");

	/** Detector data flush thread */
	static public final Scheduler FLUSH =
		new Scheduler("Scheduler: FLUSH");

	/** SQL connection */
	static SQLConnection store;

	/** SONAR namespace */
	static ServerNamespace namespace;

	/** Corridor manager */
	static CorridorManager corridors;

	/** Open the database connection */
	static protected void openVault(Properties props) throws IOException,
		TMSException
	{
		store = new SQLConnection(
			props.getProperty("db.url"),
			props.getProperty("db.user"),
			props.getProperty("db.password")
		);
	}

	/** Station manager */
	protected transient StationManager station_manager;

	/** Schedule all repeating jobs */
	public void scheduleJobs() {
		station_manager = new StationManager(namespace);
		TIMER.addJob(new TimerJobDMS(
			SystemAttrEnum.DMS_POLL_FREQ_SECS.getInt()));
		TIMER.addJob(new TimerJob30Sec());
		TIMER.addJob(new TimerJob1Min());
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

	/** DMS polling timer job */
	protected class TimerJobDMS extends Job {

		/** Create a new DMS polling timer job */
		protected TimerJobDMS(int intervalSecs) {
			super(Calendar.SECOND, intervalSecs, Calendar.SECOND,4);
		}

		/** Perform the DMS poll job */
		public void perform() throws Exception {
			pollDMSs();
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
				pollLCSs();
				pollWarningSigns();
			}
			finally {
				comp.makeReady();
			}
		}
	}

	/** Check if the given date/time matches any holiday */
	static protected boolean isHoliday(Calendar stamp) {
		return lookupHoliday(stamp) != null;
	}

	/** Lookup a holiday which matches the given calendar */
	static protected HolidayImpl lookupHoliday(final Calendar stamp) {
		return (HolidayImpl)namespace.findObject(Holiday.SONAR_TYPE,
			new Checker<HolidayImpl>()
		{
			public boolean check(HolidayImpl h) {
				return h.matches(stamp);
			}
		});
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

	/** 1-minute timer job */
	protected class TimerJob1Min extends Job {

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

		/** Create a new 1-minute timer job */
		protected TimerJob1Min() {
			// start the job at hh:mm:45
			super(Calendar.MINUTE, 1, Calendar.SECOND, 45);
			comp = new Completer("1-Minute", FLUSH, job);
		}

		/** Perform the 1-minute timer job */
		public void perform() throws Exception {
			if(!comp.checkComplete()) {
				System.err.println("TimerJob1Min: failed " + 
					"to complete at " + new Date());
				return;
			}
			stamp = Calendar.getInstance();
			Calendar s = (Calendar)stamp.clone();
			s.add(Calendar.MINUTE, -1);
			comp.reset(s);
			try {
				do1MinuteJobs(comp);
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

	/** Create a new TMS object */
	TMSImpl(Properties props) throws IOException, TMSException {
		super();
		initialize();
		openVault(props);
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

	/** perform 1 minute jobs */
	protected void do1MinuteJobs(final Completer comp) {
		// write kml file
		KmlFile.writeServerFile(this);

		// write uptime log
		UptimeLog.writeServerLog(namespace);
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

	/** Poll all DMS message status */
	static protected void pollDMSs() throws NamespaceError {
		namespace.findObject(DMS.SONAR_TYPE, new Checker<DMSImpl>() {
			public boolean check(DMSImpl d) {
				if(!d.isConnectedViaModem())
					pollDMS(d);
				return false;
			}
		});
	}

	/** Poll one DMS message status */
	static protected void pollDMS(DMSImpl d) {
		DMSPoller p = d.getDMSPoller();
		if(p != null)
			p.sendRequest(d, SignRequest.QUERY_MESSAGE);
	}

	/** Poll all LCS status */
	static protected void pollLCSs() throws NamespaceError {
		namespace.findObject(LCSArray.SONAR_TYPE,
			new Checker<LCSArrayImpl>()
		{
			public boolean check(LCSArrayImpl lcs_array) {
				pollLCSArray(lcs_array);
				return false;
			}
		});
	}

	/** Poll one LCS array status */
	static protected void pollLCSArray(LCSArrayImpl lcs_array) {
		LCSPoller p = lcs.getLCSPoller();
		if(p != null)
			p.sendRequest(lcs_array, SignRequest.QUERY_STATUS);
	}

	/** Poll all warning signs */
	static protected void pollWarningSigns() throws NamespaceError {
		namespace.findObject(WarningSign.SONAR_TYPE,
			new Checker<WarningSignImpl>()
		{
			public boolean check(WarningSignImpl warn_sign) {
				pollWarningSign(warn_sign);
				return false;
			}
		});
	}

	/** Poll one warning sign status */
	static protected void pollWarningSign(WarningSignImpl warn_sign) {
		WarningSignPoller p = warn_sign.getWarningSignPoller();
		if(p != null)
			p.sendRequest(warn_sign, SignRequest.QUERY_STATUS);
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

	/** Lookup a DMS in the SONAR namespace */
	static public DMSImpl lookupDms(String name) {
		return (DMSImpl)namespace.lookupObject(DMS.SONAR_TYPE, name);
	}

	/** get kml document name (KmlDocument interface) */
	public String getDocumentName() {
		return "IRIS ATMS";
	}

	/** render to kml (KmlDocument interface) */
	public String renderKml() {
		return KmlRenderer.render(this);
	}

	/** render innert elements to kml (KmlPoint interface) */
	public String renderInnerKml() {
		return "";
	}

	/** return the kml document features (KmlDocument interface) */
	public ArrayList<KmlFeature> getKmlFeatures() {
		ArrayList<KmlFeature> ret = new ArrayList<KmlFeature>();
		ret.add((KmlFolder)DMSList.get());
		//ret.add((KmlFolder)getLCSList());
		//ret.add((KmlFolder)getRampMeterList());
		// cameras
		return ret;
	}

	/** return kml style selector (KmlDocument interface) */
	public KmlStyleSelector getKmlStyleSelector() {
		return null;
	}
}
