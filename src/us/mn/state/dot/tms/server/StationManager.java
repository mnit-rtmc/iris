/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2012  Minnesota Department of Transportation
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
import java.util.Date;
import java.util.Iterator;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.StationHelper;

/**
 * This class writes out station XML files.
 *
 * @author Douglas Lau
 */
class StationManager {

	/** Location of station sample XML file */
	static private final String SAMPLE_XML = "stat_sample.xml";

	/** Location of old, deprecated station XML file */
	static private final String STATION_XML = "station.xml";

	/** FLUSH Scheduler for writing XML (I/O to disk) */
	private final Scheduler flush;

	/** Job to be performed after data has been processed */
	private final FlushXmlJob flush_job;

	/** Create a new station manager */
	public StationManager(Scheduler f) {
		flush = f;
		flush_job = new FlushXmlJob(this);
	}

	/** Calculate the current data for all stations */
	public void calculateData() {
		Iterator<Station> it = StationHelper.iterator();
		while(it.hasNext()) {
			Station s = it.next();
			if(s instanceof StationImpl) {
				StationImpl si = (StationImpl)s;
				si.calculateData();
			}
		}
		flush.addJob(flush_job);
	}

	/** Write the station sample data out as XML */
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

	/** Print the header of the station sample XML file */
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

	/** Print the body of the station sample XML file */
	private void printSampleXmlBody(PrintWriter out) {
		Iterator<Station> it = StationHelper.iterator();
		while(it.hasNext()) {
			Station s = it.next();
			if(s instanceof StationImpl) {
				StationImpl si = (StationImpl)s;
				si.printSampleXml(out);
			}
		}
	}

	/** Print the tail of the station sample XML file */
	private void printSampleXmlTail(PrintWriter out) {
		out.println("</traffic_sample>");
	}

	/** Write the station data out as XML.  This is retained for backwards
	 * compatibility.  It is not recommended to use this XML file for new
	 * software, since it may be removed in the future. */
	public void writeStationXml() throws IOException {
		XmlWriter w = new XmlWriter(STATION_XML, false) {
			public void print(PrintWriter out) {
				printStationXmlHead(out);
				printStationXmlBody(out);
				printStationXmlTail(out);
			}
		};
		w.write();
	}

	/** Print the header of the station sample XML file */
	private void printStationXmlHead(PrintWriter out) {
		out.println("<?xml version='1.0'?>");
		out.println("<station_data time_stamp='" +
			TimeSteward.getDateInstance() +"' sample_period='30'>");
	}

	/** Print the body of the station sample XML file */
	private void printStationXmlBody(PrintWriter out) {
		Iterator<Station> it = StationHelper.iterator();
		while(it.hasNext()) {
			Station s = it.next();
			if(s instanceof StationImpl) {
				StationImpl si = (StationImpl)s;
				si.printStationXmlElement(out);
			}
		}
	}

	/** Print the tail of the station sample XML file */
	private void printStationXmlTail(PrintWriter out) {
		out.println("</station_data>");
	}
}
