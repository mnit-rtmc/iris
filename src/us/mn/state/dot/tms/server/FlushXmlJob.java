/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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

import java.io.PrintWriter;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Job to flush XML sample data
 *
 * @author Douglas Lau
 */
public class FlushXmlJob extends Job {

	/** Check if obsolete station.xml output is enabled */
	static private boolean isStationXmlEnabled() {
		return SystemAttrEnum.STATION_XML_ENABLE.getBoolean();
	}

	/** Detector sample file */
	static private final String SAMPLE_XML = "det_sample.xml";

	/** Station manager */
	private final StationManager station_manager;

	/** Create a new flush XML samples job */
	public FlushXmlJob(StationManager sm) {
		station_manager = sm;
	}

	/** Perform flush XML data to disk */
	public void perform() {
		writeSampleXml();
		station_manager.writeSampleXml();
		if(isStationXmlEnabled())
			station_manager.writeStationXml();
	}

	/** Write the sample data out as XML */
	private void writeSampleXml() {
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
	private void printSampleXmlHead(PrintWriter out) {
		out.println(XmlWriter.XML_DECLARATION);
		printDtd(out);
		out.println("<traffic_sample time_stamp='" +
			TimeSteward.getDateInstance() + "' period='30'>");
	}

	/** Print the DTD */
	private void printDtd(PrintWriter out) {
		out.println("<!DOCTYPE traffic_sample [");
		out.println("<!ELEMENT traffic_sample (sample)*>");
		out.println("<!ATTLIST traffic_sample time_stamp " +
			"CDATA #REQUIRED>");
		out.println("<!ATTLIST traffic_sample period CDATA #REQUIRED>");
		out.println("<!ELEMENT sample EMPTY>");
		out.println("<!ATTLIST sample sensor CDATA #REQUIRED>");
		out.println("<!ATTLIST sample flow CDATA 'UNKNOWN'>");
		out.println("<!ATTLIST sample speed CDATA 'UNKNOWN'>");
		out.println("<!ATTLIST sample occ CDATA 'UNKNOWN'>");
		out.println("]>");
	}

	/** Print the body of the detector sample XML file */
	private void printSampleXmlBody(final PrintWriter out) {
		DetectorHelper.find(new Checker<Detector>() {
			public boolean check(Detector d) {
				if(d instanceof DetectorImpl) {
					DetectorImpl det = (DetectorImpl)d;
					det.calculateFakeData();
					det.printSampleXml(out);
				}
				return false;
			}
		});
	}

	/** Print the tail of the detector sample XML file */
	private void printSampleXmlTail(PrintWriter out) {
		out.println("</traffic_sample>");
	}
}
