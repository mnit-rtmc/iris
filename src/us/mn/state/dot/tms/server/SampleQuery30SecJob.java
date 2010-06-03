/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.HolidayHelper;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.TimingPlan;
import us.mn.state.dot.tms.TimingPlanHelper;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.SamplePoller;

/**
 * Job to query 30-second sample data
 *
 * @author Douglas Lau
 */
public class SampleQuery30SecJob extends Job {

	/** Detector sample file */
	static protected final String SAMPLE_XML = "det_sample.xml";

	/** Seconds to offset each poll from start of interval */
	static protected final int OFFSET_SECS = 8;

	/** Station manager */
	protected final StationManager station_manager;

	/** Job completer */
	protected final Completer comp;

	/** Current time stamp */
	protected Calendar stamp;

	/** Job to be performed on completion */
	protected final Job complete_job = new Job() {
		public void perform() throws IOException {
			try {
				station_manager.calculateData();
				station_manager.writeSampleXml();
				station_manager.writeStationXml();
				writeSampleXml();
				BaseObjectImpl.corridors.findBottlenecks();
			}
			finally {
				if(!HolidayHelper.isHoliday(stamp))
					validateTimingPlans();
			}
		}
	};

	/** Create a new 30-second timer job */
	public SampleQuery30SecJob(Scheduler flush) {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
		station_manager = new StationManager();
		comp = new Completer("30-Second", flush, complete_job);
	}

	/** Perform the 30-second timer job */
	public void perform() {
		if(!comp.checkComplete())
			return;
		Calendar s = Calendar.getInstance();
		s.add(Calendar.SECOND, -30);
		stamp = s;
		comp.reset(stamp);
		try {
			querySample30Sec();
		}
		finally {
			comp.makeReady();
		}
	}

	/** Poll all sampling controllers 30-second interval */
	protected void querySample30Sec() {
		ControllerHelper.find(new Checker<Controller>() {
			public boolean check(Controller c) {
				if(c instanceof ControllerImpl)
					querySample30Sec((ControllerImpl)c);
				return false;
			}
		});
	}

	/** Query 30-second sample data from one controller */
	protected void querySample30Sec(ControllerImpl c) {
		MessagePoller p = c.getPoller();
		if(p instanceof SamplePoller) {
			SamplePoller sp = (SamplePoller)p;
			sp.querySamples(c, 30, comp);
		}
	}

	/** Write the sample data out as XML */
	protected void writeSampleXml() throws IOException {
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
	protected void printSampleXmlBody(final PrintWriter out) {
		DetectorHelper.find(new Checker<Detector>() {
			public boolean check(Detector d) {
				if(d instanceof DetectorImpl) {
					DetectorImpl det = (DetectorImpl)d;
					det.calculateFakeData();
					det.printSampleXmlElement(out);
				}
				return false;
			}
		});
	}

	/** Print the tail of the detector sample XML file */
	protected void printSampleXmlTail(PrintWriter out) {
		out.println("</traffic_sample>");
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
	}
}
