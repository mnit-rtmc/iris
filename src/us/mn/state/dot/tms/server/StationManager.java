/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2011  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.StationHelper;

/**
 * This class writes out station XML files.
 *
 * @author Douglas Lau
 */
class StationManager {

	/** Location of station sample XML file */
	static protected final String SAMPLE_XML = "stat_sample.xml";

	/** Location of old, deprecated station XML file */
	static protected final String STATION_XML = "station.xml";

	/** Calculate the current data for all stations */
	public void calculateData() {
		StationHelper.find(new Checker<Station>() {
			public boolean check(Station s) {
				if(s instanceof StationImpl) {
					StationImpl si = (StationImpl)s;
					si.calculateData();
				}
				return false;
			}
		});
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
	protected void printSampleXmlHead(PrintWriter out) {
		out.println(XmlWriter.XML_DECLARATION);
		printDtd(out);
		out.println("<traffic_sample time_stamp='" +
			TimeSteward.getDateInstance() + "' period='30'>");
	}

	/** Print the DTD */
	protected void printDtd(PrintWriter out) {
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
	protected void printSampleXmlBody(final PrintWriter out) {
		StationHelper.find(new Checker<Station>() {
			public boolean check(Station s) {
				if(s instanceof StationImpl) {
					StationImpl si = (StationImpl)s;
					si.printSampleXml(out);
				}
				return false;
			}
		});
	}

	/** Print the tail of the station sample XML file */
	protected void printSampleXmlTail(PrintWriter out) {
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
	protected void printStationXmlHead(PrintWriter out) {
		out.println("<?xml version='1.0'?>");
		out.println("<station_data time_stamp='" +
			TimeSteward.getDateInstance() +"' sample_period='30'>");
	}

	/** Print the body of the station sample XML file */
	protected void printStationXmlBody(final PrintWriter out) {
		StationHelper.find(new Checker<Station>() {
			public boolean check(Station s) {
				if(s instanceof StationImpl) {
					StationImpl si = (StationImpl)s;
					si.printStationXmlElement(out);
				}
				return false;
			}
		});
	}

	/** Print the tail of the station sample XML file */
	protected void printStationXmlTail(PrintWriter out) {
		out.println("</station_data>");
	}
}
