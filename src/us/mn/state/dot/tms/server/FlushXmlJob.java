/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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
import java.util.Date;
import java.util.Iterator;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Job to flush XML data
 *
 * @author Douglas Lau
 */
public class FlushXmlJob extends Job {

	/** Detector data file */
	static private final String SAMPLE_XML = "det_sample.xml";

	/** Station manager */
	private final StationManager station_manager;

	/** Create a new flush XML samples job */
	public FlushXmlJob(StationManager sm) {
		station_manager = sm;
	}

	/** Perform flush XML data to disk */
	@Override
	public void perform() throws IOException {
		if (SystemAttrEnum.LEGACY_XML_DETECTOR_ENABLE.getBoolean()) {
			writeSampleXml();
			station_manager.writeSampleXml();
		}
		station_manager.writeSampleJson();
	}

	/** Write the detector data out as XML */
	private void writeSampleXml() throws IOException {
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

	/** Write the header of the detector XML file */
	private void writeSampleXmlHead(Writer w) throws IOException {
		long stamp = station_manager.getStamp();
		w.write(XmlWriter.XML_DECLARATION);
		writeDtd(w);
		w.write("<traffic_sample time_stamp='" + new Date(stamp) +
			"' period='30'>\n");
	}

	/** Write the DTD */
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

	/** Write the body of the detector XML file */
	private void writeSampleXmlBody(Writer w) throws IOException {
		long stamp = station_manager.getStamp();
		int per_ms = DetectorImpl.BIN_PERIOD_MS;
		Iterator<Detector> it = DetectorHelper.iterator();
		while (it.hasNext()) {
			Detector d = it.next();
			if (d instanceof DetectorImpl) {
				DetectorImpl det = (DetectorImpl) d;
				det.writeSampleXml(w, stamp, per_ms);
			}
		}
	}

	/** Write the tail of the detector XML file */
	private void writeSampleXmlTail(Writer w) throws IOException {
		w.write("</traffic_sample>\n");
	}
}
