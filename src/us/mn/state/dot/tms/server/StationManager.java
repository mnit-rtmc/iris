/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2014  Minnesota Department of Transportation
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
import java.io.Writer;
import java.util.Iterator;
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
	}

	/** Write the station sample data out as XML */
	public void writeSampleXml() throws IOException {
		XmlWriter w = new XmlWriter(SAMPLE_XML, true) {
			@Override protected void write(Writer w)
				throws IOException
			{
				writeSampleXmlHead(w);
				writeSampleXmlBody(w);
				writeSampleXmlTail(w);
			}
		};
		w.write();
	}

	/** Print the header of the station sample XML file */
	private void writeSampleXmlHead(Writer w) throws IOException {
		w.write(XmlWriter.XML_DECLARATION);
		writeDtd(w);
		w.write("<traffic_sample time_stamp='" +
			TimeSteward.getDateInstance() + "' period='30'>\n");
	}

	/** Print the DTD */
	private void writeDtd(Writer w) throws IOException {
		w.write("<!DOCTYPE traffic_sample [\n");
		w.write("<!ELEMENT traffic_sample (sample)*>\n");
		w.write("<!ATTLIST traffic_sample time_stamp " +
			"CDATA #REQUIRED>\n");
		w.write("<!ATTLIST traffic_sample period CDATA #REQUIRED>\n");
		w.write("<!ELEMENT sample EMPTY>\n");
		w.write("<!ATTLIST sample sensor CDATA #REQUIRED>\n");
		w.write("<!ATTLIST sample flow CDATA 'UNKNOWN'>\n");
		w.write("<!ATTLIST sample speed CDATA 'UNKNOWN'>\n");
		w.write("<!ATTLIST sample occ CDATA 'UNKNOWN'>\n");
		w.write("]>\n");
	}

	/** Print the body of the station sample XML file */
	private void writeSampleXmlBody(Writer w) throws IOException {
		Iterator<Station> it = StationHelper.iterator();
		while(it.hasNext()) {
			Station s = it.next();
			if(s instanceof StationImpl) {
				StationImpl si = (StationImpl)s;
				si.writeSampleXml(w);
			}
		}
	}

	/** Print the tail of the station sample XML file */
	private void writeSampleXmlTail(Writer w) throws IOException {
		w.write("</traffic_sample>\n");
	}
}
