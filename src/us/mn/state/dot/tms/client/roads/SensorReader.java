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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SensorReader reads and parses an XML document at a 30-second interval.
 * SensorSample objects are created for each sample element, and reported
 * to any registered listeners.
 *
 * @author Douglas Lau
 */
public class SensorReader implements Runnable {

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

	/** The thread the client runs in */
	private Thread thread = null;

	/** Logger to use */
	protected final Logger logger;

	/** Should this run as a daemon? */
	private boolean daemon = true;

	/** Time to wait till re-reading data */
	private int sleepTime = 30000;

	/** SAX parser */
	private final SAXParser parser;

	/** Time stamp from previous read */
	private String last_stamp = "";

	/** Flag to indicate the time stamp changed since last time */
	private boolean time_changed = false;

	/** List of listeners */
	private List<SensorListener> listeners =
		new LinkedList<SensorListener>();

	/** Create a new sensor reader */
	public SensorReader(URL u, Logger l) throws SAXException,
		ParserConfigurationException
	{
		url = u;
		logger = l;
		SAXParserFactory factory = SAXParserFactory.newInstance();
		parser = factory.newSAXParser();
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while(isRunning()) {
			readData();
			try {
				Thread.sleep(sleepTime);
			}
			catch(InterruptedException ie) {
				logger.info("Interrupted: " + url);
				logger.fine(ie.getMessage());
				break;
			}
		}
	}

	/** Read the data from the xml file */
	protected void readData() {
		logger.info("Reading data from " + url);
		try {
			readXmlFile();
		}
		catch(IOException ioe) {
			logger.warning("IOException reading data from " + url);
			logger.warning(ioe.getMessage());
		}
		catch(Exception e) {
			logger.warning("Error reading xml from " + url +
				"(" + e + "), " + "will retry in " +
				(sleepTime / 1000) + " seconds.");
			e.printStackTrace();
		}
	}

	/**
	 * Starts a new thread running that reads the xml file and fires update
	 * events to registered listeners when new data arrives.
	 */
	public void start() {
		logger.info("start() " + url);
		synchronized(this) {
			if(!isRunning()) {
				thread = new Thread(this);
				thread.setDaemon(daemon);
				thread.start();
			}
		}
	}

	/**
	 * Stops the running thread
	 */
	public void stop() {
		logger.info("stop() " + url);
		synchronized(this) {
			Thread t = thread;
			thread = null;
			if(t != null) {
				try {
					t.interrupt();
					t.join();
				}
				catch(InterruptedException e) {
					logger.info("Join interrupted: " +
						e.getMessage());
				}
			}
		}
	}

	/** Check if the client is running */
	private boolean isRunning() {
		return thread != null;
	}

	/** Add a TDXML listener */
	public void addSensorListener(SensorListener l) {
		LinkedList<SensorListener> lsnr =
			new LinkedList<SensorListener>(listeners);
		lsnr.add(l);
		listeners = lsnr;
	}

	/** Remove a TDXML listener */
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
		logger.info("Openning connection to " + url);
		URLConnection conn = url.openConnection();
		logger.info("Setting connect timeout on " + url);
		conn.setConnectTimeout(60000);
		logger.info("Setting read timeout on " + url);
		conn.setReadTimeout(60000);
		logger.info("Getting input stream from " + url);
		InputStream in = new GZIPInputStream(conn.getInputStream());
		logger.info("Parsing XML for " + url);
		parse(in);
		logger.info("Parse complete for " + url);
	}

	/** Parse the XML document and notify clients */
	protected void parse(InputStream in) throws IOException, SAXException {
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
