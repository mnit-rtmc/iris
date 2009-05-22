/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2009  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.tms.Station;

/**
 * This class writes out station XML files.
 *
 * @author Douglas Lau
 */
class StationManager {

	/** Location of station sample XML file */
	static protected final String SAMPLE_XML = "stat_sample.xml";

	/** Location of station XML file */
	static protected final String STATION_XML = "station.xml";

	/** SONAR namespace */
	protected final ServerNamespace namespace;

	/** Most recent time stamp of calculated data */
	protected transient Calendar stamp;

	/** Create a new station manager */
	public StationManager(ServerNamespace ns) {
		namespace = ns;
		stamp = Calendar.getInstance();
	}

	/** Calculate the current data for all stations */
	public void calculateData(Calendar st) throws NamespaceError {
		if(stamp.after(st)) {
			System.err.println("StationData OUT OF ORDER: " +
				stamp.getTime() + " > " + st.getTime());
			return;
		}
		stamp = st;
		namespace.findObject(Station.SONAR_TYPE,
			new Checker<StationImpl>()
		{
			public boolean check(StationImpl s) {
				s.calculateData();
				return false;
			}
		});
	}

	/** Write the station sample data out as XML */
	public void writeSampleXml() throws NamespaceError {
		XmlWriter w = new XmlWriter(SAMPLE_XML, true) {
			public void print(PrintWriter out) {
				printSampleXmlHead(out);
				printSampleXmlBody(out);
				printSampleXmlTail(out);
			}
		};
		try {
			w.write();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/** Print the header of the station sample XML file */
	protected void printSampleXmlHead(PrintWriter out) {
		out.println("<?xml version='1.0'?>");
		out.println(
			"<!DOCTYPE traffic_sample SYSTEM 'tms_config.dtd'>");
		out.println("<traffic_sample time_stamp='" + new Date() +
			"' period='30'>");
		out.println("\t&r_nodes;");
		out.println("\t&detectors;");
	}

	/** Print the body of the station sample XML file */
	protected void printSampleXmlBody(final PrintWriter out) {
		namespace.findObject(Station.SONAR_TYPE,
			new Checker<StationImpl>()
		{
			public boolean check(StationImpl s) {
				s.printSampleXmlElement(out);
				return false;
			}
		});
	}

	/** Print the tail of the station sample XML file */
	protected void printSampleXmlTail(PrintWriter out) {
		out.println("</traffic_sample>");
	}

	/** Write the station data out as XML */
	public void writeStationXml() throws NamespaceError {
		XmlWriter w = new XmlWriter(STATION_XML, false) {
			public void print(PrintWriter out) {
				printStationXmlHead(out);
				printStationXmlBody(out);
				printStationXmlTail(out);
			}
		};
		try {
			w.write();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/** Print the header of the station sample XML file */
	protected void printStationXmlHead(PrintWriter out) {
		out.println("<?xml version='1.0'?>");
		out.println("<station_data time_stamp='" + new Date() +
			"' sample_period='30'>");
	}

	/** Print the body of the station sample XML file */
	protected void printStationXmlBody(final PrintWriter out) {
		namespace.findObject(Station.SONAR_TYPE,
			new Checker<StationImpl>()
		{
			public boolean check(StationImpl s) {
				s.printStationXmlElement(out);
				return false;
			}
		});
	}

	/** Print the tail of the station sample XML file */
	protected void printStationXmlTail(PrintWriter out) {
		out.println("</station_data>");
	}
}
