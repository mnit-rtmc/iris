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
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.ArrayList;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.WarningSignHelper;
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

	/** Get DMS and LCS periodic polling frequency in seconds */
	private int getPeriodicDmsPollingFreqSecs() {
		int secs = SystemAttrEnum.DMS_POLL_FREQ_SECS.getInt();
		if(secs <= 0)
			return 0;
		return (secs < 5 ? 5 : secs);
	}

	/** Schedule all repeating jobs */
	public void scheduleJobs() {
		int secs = getPeriodicDmsPollingFreqSecs();
		if(secs > 0) {
			TIMER.addJob(new DmsQueryMsgJob(secs));
			TIMER.addJob(new LcsQueryMsgJob(secs));
			TIMER.addJob(new WarnQueryStatusJob(secs));
		}
		TIMER.addJob(new DmsQueryStatusJob());
		TIMER.addJob(new AlarmQueryStatusJob());
		TIMER.addJob(new TimerJob1Min());
		TIMER.addJob(new SampleQuery30SecJob(TIMER));
		TIMER.addJob(new SampleQuery5MinJob(FLUSH));
		TIMER.addJob(new DmsXmlJob());
		TIMER.addJob(new CameraNoFailJob());
		TIMER.addJob(new ProfilingJob());
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
		new DetectorXmlWriter().write();
		corridors = new CorridorManager(namespace);
		new R_NodeXmlWriter(corridors).write();
		new RampMeterXmlWriter().write();
		new CameraXmlWriter().write();
		new GeoLocXmlWriter().write();
		System.err.println("Completed TMS XML dump @ " + new Date());
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

	/** Perform 1 minute jobs */
	protected void do1MinuteJobs() throws Exception {
		KmlFile.writeServerFile(this);
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
