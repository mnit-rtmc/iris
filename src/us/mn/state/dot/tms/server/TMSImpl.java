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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.ArrayList;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DmsActionHelper;
import us.mn.state.dot.tms.Holiday;
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneActionHelper;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.TimingPlan;
import us.mn.state.dot.tms.TimingPlanHelper;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.WarningSignHelper;
import us.mn.state.dot.tms.server.comm.AlarmPoller;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.SamplePoller;
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

	/** Calendar instance for calculating the minute of day */
	static protected final Calendar STAMP = Calendar.getInstance();

	/** Get the current minute of the day */
	static protected int minute_of_day() {
		synchronized(STAMP) {
			STAMP.setTimeInMillis(System.currentTimeMillis());
			return STAMP.get(Calendar.HOUR_OF_DAY) * 60 +
				STAMP.get(Calendar.MINUTE);
		}
	}

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

	/** Get DMS and LCS periodic polling frequency in seconds */
	private int getPeriodicDmsPollingFreqSecs() {
		int secs = SystemAttrEnum.DMS_POLL_FREQ_SECS.getInt();
		if(secs <= 0)
			return 0;
		return (secs < 5 ? 5 : secs);
	}

	/** Schedule all repeating jobs */
	public void scheduleJobs() {
		station_manager = new StationManager(namespace);
		int secs = getPeriodicDmsPollingFreqSecs();
		if(secs > 0) {
			TIMER.addJob(new TimerJobDMS(secs));
			TIMER.addJob(new TimerJobLCS(secs));
		}
		TIMER.addJob(new TimerJob30Sec());
		TIMER.addJob(new TimerJob1Min());
		TIMER.addJob(new TimerJob5Min());
		TIMER.addJob(new Job(Calendar.HOUR, 1) {
			public void perform() throws Exception {
				System.out.println(new Date());
				Profile.printMemory(System.out);
				Profile.printThreads(System.out);
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
	protected void writeXmlConfiguration() throws IOException {
		System.err.println("Writing TMS XML files @ " + new Date());
		new DetectorXmlWriter(namespace).write();
		corridors = new CorridorManager(namespace);
		new R_NodeXmlWriter(corridors).write();
		new RampMeterXmlWriter(namespace).write();
		new CameraXmlWriter(namespace).write();
		new GeoLocXmlWriter(namespace).write();
		System.err.println("Completed TMS XML dump @ " + new Date());
	}

	/** Write the current state to XML files */
	protected void writeXmlState() throws IOException {
		new DMSXmlWriter(namespace).write();
	}

	/** Write the sample data out as XML */
	public void writeSampleXml() throws IOException {
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
		out.println(XmlWriter.XML_DECLARATION);
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

	/** LCS polling timer job */
	protected class TimerJobLCS extends Job {

		/** Create a new LCS polling timer job */
		protected TimerJobLCS(int intervalSecs) {
			super(Calendar.SECOND, intervalSecs, Calendar.SECOND,
				19);
		}

		/** Perform the LCS poll job */
		public void perform() throws Exception {
			pollLCSs();
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
				if(!isHoliday(stamp))
					validateTimingPlans();
				performActions();
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
		TimingPlanHelper.find(new Checker<TimingPlan>() {
			public boolean check(TimingPlan p) {
				TimingPlanImpl plan = (TimingPlanImpl)p;
				plan.validate();
				return false;
			}
		});
		StratifiedPlanState.processAllStates();
		RampMeterHelper.find(new Checker<RampMeter>() {
			public boolean check(RampMeter m) {
				RampMeterImpl meter = (RampMeterImpl)m;
				meter.updateRatePlanned();
				return false;
			}
		});
		final int minute = minute_of_day();
		TimeActionHelper.find(new Checker<TimeAction>() {
			public boolean check(TimeAction ta) {
				if(ta.getMinute() == minute)
					performTimeAction(ta);
				return false;
			}
		});
	}

	/** Perform a time action */
	protected void performTimeAction(TimeAction ta) {
		ActionPlan ap = ta.getActionPlan();
		if(ap instanceof ActionPlanImpl) {
			ActionPlanImpl api = (ActionPlanImpl)ap;
			if(api.getActive())
				api.setDeployedNotify(ta.getDeploy());
		}
	}

	/** Perform all current actions */
	protected void performActions() {
		DmsActionHelper.find(new Checker<DmsAction>() {
			public boolean check(DmsAction da) {
				ActionPlan ap = da.getActionPlan();
				if(ap.getActive()) {
					if(ap.getDeployed() == da.getOnDeploy())
						performDmsAction(da);
				}
				return false;
			}
		});
		DMSHelper.find(new Checker<DMS>() {
			public boolean check(DMS dms) {
				if(dms instanceof DMSImpl)
					((DMSImpl)dms).updateScheduledMessage();
				return false;
			}
		});
		LaneActionHelper.find(new Checker<LaneAction>() {
			public boolean check(LaneAction la) {
				ActionPlan ap = la.getActionPlan();
				if(ap.getActive()) {
					LaneMarking m = la.getLaneMarking();
					if(m != null)
						m.setDeployed(ap.getDeployed());
				}
				return false;
			}
		});
	}

	/** Perform a DMS action */
	protected void performDmsAction(final DmsAction da) {
		SignGroup sg = da.getSignGroup();
		DMSHelper.find(new Checker<DMS>() {
			public boolean check(DMS d) {
				if(d instanceof DMSImpl) {
					final DMSImpl dms = (DMSImpl)d;
					// We need to create a new Job here so
					// that when performAction is called,
					// we're not holding the SONAR TypeNode
					// locks for DMS and DmsAction.
					TIMER.addJob(new Job() {
						public void perform() {
							dms.performAction(da);
						}
					});
				}
				return false;
			}
		}, sg);
	}

	/** 1-minute timer job */
	protected class TimerJob1Min extends Job {

		/** Create a new 1-minute timer job */
		protected TimerJob1Min() {
			// start the job at hh:mm:45
			super(Calendar.MINUTE, 1, Calendar.SECOND, 45);
		}

		/** Perform the 1-minute timer job */
		public void perform() throws Exception {
			do1MinuteJobs();
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
		openVault(props);
	}

	/** Download to all controllers */
	static protected void download() {
		namespace.findObject(Controller.SONAR_TYPE,
			new Checker<ControllerImpl>()
		{
			public boolean check(ControllerImpl c) {
				c.setDownload(false);
				return false;
			}
		});
		final int req = DeviceRequest.SEND_SETTINGS.ordinal();
		DMSHelper.find(new Checker<DMS>() {
			public boolean check(DMS dms) {
				dms.setDeviceRequest(req);
				dms.setDeviceRequest(DeviceRequest.
					QUERY_PIXEL_FAILURES.ordinal());
				return false;
			}
		});
		RampMeterHelper.find(new Checker<RampMeter>() {
			public boolean check(RampMeter meter) {
				meter.setDeviceRequest(req);
				return false;
			}
		});
		WarningSignHelper.find(new Checker<WarningSign>() {
			public boolean check(WarningSign sign) {
				sign.setDeviceRequest(req);
				return false;
			}
		});
	}

	/** Poll all sampling controllers 30 second interval */
	static protected void poll30Second(final Completer comp) {
		namespace.findObject(Controller.SONAR_TYPE,
			new Checker<ControllerImpl>()
		{
			public boolean check(ControllerImpl c) {
				MessagePoller p = c.getPoller();
				if(p instanceof SamplePoller) {
					SamplePoller sp = (SamplePoller)p;
					sp.querySamples(c, 30, comp);
				}
				return false;
			}
		});
	}

	/** Perform 1 minute jobs */
	protected void do1MinuteJobs() throws Exception {
		KmlFile.writeServerFile(this);
		UptimeLog.writeServerLog(namespace);
		writeXmlState();
		CameraHelper.find(new Checker<Camera>() {
			public boolean check(Camera c) {
				if(c instanceof CameraImpl) {
					CameraImpl cam = (CameraImpl)c;
					cam.clearFailed();
				}
				return false;
			}
		});
	}

	/** Poll all controllers 5 minute interval */
	static protected void poll5Minute(final Completer comp) {
		namespace.findObject(Controller.SONAR_TYPE,
			new Checker<ControllerImpl>()
		{
			public boolean check(ControllerImpl c) {
				MessagePoller p = c.getPoller();
				if(p instanceof SamplePoller) {
					SamplePoller sp = (SamplePoller)p;
					sp.querySamples(c, 300, comp);
				}
				if(p instanceof AlarmPoller) {
					AlarmPoller ap = (AlarmPoller)p;
					ap.queryAlarms(c);
				}
				return false;
			}
		});
		final int req = DeviceRequest.QUERY_STATUS.ordinal();
		DMSHelper.find(new Checker<DMS>() {
			public boolean check(DMS dms) {
				if(DMSHelper.isPeriodicallyQueriable(dms))
					dms.setDeviceRequest(req);
				return false;
			}
		});
	}

	/** Flush the detector data to disk */
	static protected void flushDetectorData(final Calendar stamp) {
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

	/** Poll all DMS message status */
	static protected void pollDMSs() {
		final int req = DeviceRequest.QUERY_MESSAGE.ordinal();
		DMSHelper.find(new Checker<DMS>() {
			public boolean check(DMS dms) {
				if(DMSHelper.isPeriodicallyQueriable(dms))
					dms.setDeviceRequest(req);
				return false;
			}
		});
	}

	/** Poll all LCS indications */
	static protected void pollLCSs() {
		final int req = DeviceRequest.QUERY_MESSAGE.ordinal();
		LCSArrayHelper.find(new Checker<LCSArray>() {
			public boolean check(LCSArray lcs_array) {
				lcs_array.setDeviceRequest(req);
				return false;
			}
		});
	}

	/** Poll all warning signs */
	static protected void pollWarningSigns() {
		final int req = DeviceRequest.QUERY_STATUS.ordinal();
		WarningSignHelper.find(new Checker<WarningSign>() {
			public boolean check(WarningSign sign) {
				sign.setDeviceRequest(req);
				return false;
			}
		});
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
