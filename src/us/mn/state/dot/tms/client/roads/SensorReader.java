/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;

/**
 * SensorReader reads and parses an XML document at a 30-second interval.
 * SensorSample objects are created for each sample element, and reported
 * to any registered listeners.
 *
 * @author Douglas Lau
 */
public class SensorReader {

	/** Reader worker thread */
	static private final Scheduler READER = new Scheduler("READER");

	/** Seconds to offset each read from start of interval */
	static private final int OFFSET_SECS = 4;

	/** Entity declaration */
	static private final String ENTITY_DECL =
		"<?xml version='1.0' encoding='UTF-8'?>";

	/** Parse an attribute as an integer value */
	static private Integer parseInt(String v) {
		try {
			if(v != null)
				return Integer.parseInt(v);
		}
		catch(NumberFormatException e) {
			// Invalid value
		}
		return null;
	}

	/** The URL of the xml document */
	private final URL url;

	/** SAX parser */
	private final SAXParser parser;

	/** Time stamp from previous read */
	private String last_stamp = "";

	/** Flag to indicate the time stamp changed since last time */
	private boolean time_changed = false;

	/** List of listeners */
	private List<SensorListener> listeners =
		new LinkedList<SensorListener>();

	/** Job to perform */
	private final Job job = new Job(Calendar.SECOND, 30, Calendar.SECOND,
		OFFSET_SECS)
	{
		public void perform() throws Exception {
			readXmlFile();
		}
	};

	/** Create a new sensor reader */
	public SensorReader(URL u) throws SAXException,
		ParserConfigurationException
	{
		url = u;
		SAXParserFactory factory = SAXParserFactory.newInstance();
		parser = factory.newSAXParser();
		// Read the sensor data right away
		READER.addJob(new Job() {
			public void perform() throws Exception {
				readXmlFile();
			}
		});
		// Read the sensor data every 30 seconds
		READER.addJob(job);
	}

	/** Dispose of the sensor listener */
	public void dispose() {
		READER.removeJob(job);
	}

	/** Add a sensor listener */
	public void addSensorListener(SensorListener l) {
		LinkedList<SensorListener> lsnr =
			new LinkedList<SensorListener>(listeners);
		lsnr.add(l);
		listeners = lsnr;
	}

	/** Remove a sensor listener */
	public void removeSensorListener(SensorListener l) {
		LinkedList<SensorListener> lsnr =
			new LinkedList<SensorListener>(listeners);
		lsnr.remove(l);
		listeners = lsnr;
	}

	/** Remove all of the registered data listeners */
	public void removeAllSensorListeners() {
		listeners = new LinkedList<SensorListener>();
	}

	/** Notifier for listeners */
	abstract private class Notifier {
		abstract void notify(SensorListener l);
	}

	/** Notify all listeners of an update */
	private void doNotify(Notifier n) {
		for(SensorListener l: listeners)
			n.notify(l);
	}

	/** Notify listeners of the start of new data */
	private void notifyStart() {
		doNotify(new Notifier() {
			void notify(SensorListener l) {
				l.update(false);
			}
		});
	}

	/** Notify listeners that new data is finished */
	private void notifyFinish() {
		doNotify(new Notifier() {
			void notify(SensorListener l) {
				l.update(true);
			}
		});
	}

	/** Notify listeners of a sensor data sample */
	private void notifySample(final SensorSample s) {
		doNotify(new Notifier() {
			void notify(SensorListener l) {
				l.update(s);
			}
		});
	}

	/** Read and parse an XML file */
	private void readXmlFile() throws Exception {
		URLConnection conn = url.openConnection();
		conn.setConnectTimeout(6000);
		conn.setReadTimeout(6000);
		InputStream in = new GZIPInputStream(conn.getInputStream());
		parse(in);
	}

	/** Parse the XML document and notify clients */
	private void parse(InputStream in) throws IOException, SAXException {
		notifyStart();
		try {
			SensorHandler h = new SensorHandler();
			parser.parse(in, h);
		}
		finally {
			notifyFinish();
		}
	}

	/** Inner class to handle parsing sensor elements */
	protected class SensorHandler extends DefaultHandler {
		public InputSource resolveEntity(String publicId,
			String systemId) throws IOException, SAXException
		{
			return new InputSource(new StringReader(ENTITY_DECL));
		}
		public void startElement(String uri, String localName,
			String qname, Attributes attrs)
		{
			if(qname.equals("traffic_sample"))
				handleTrafficSample(attrs);
			if(qname.equals("sample"))
				handleSample(attrs);
		}
	}

	/** Handle a traffic_sample element */
	protected void handleTrafficSample(Attributes attrs) {
		String stamp = attrs.getValue("time_stamp");
		time_changed = !stamp.equals(last_stamp);
		last_stamp = stamp;
	}

	/** Notify listeners of one sensor sample */
	protected void notifySensorSample(String sensor, String f, String s) {
		Integer flow = parseInt(f);
		Integer speed = parseInt(s);
		if(flow != null || speed != null)
			notifySample(new SensorSample(sensor, flow, speed));
	}

	/** Handle one sensor sample element */
	protected void handleSample(Attributes attrs) {
		if(time_changed) {
			String sensor = attrs.getValue("sensor");
			String flow = attrs.getValue("flow");
			String speed = attrs.getValue("speed");
			if(sensor != null)
				notifySensorSample(sensor, flow, speed);
		}
	}
}
