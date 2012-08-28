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
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * XmlClient reads an xml document at a specified interval and parses it to java
 * classes that can be used by listeners who are alerted when new data is
 * available.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
abstract public class XmlClient implements Runnable {

	/** The URL of the xml document */
	protected final URL url;

	/** The thread the client runs in */
	private Thread thread = null;

	/** Logger to use */
	protected final Logger logger;

	/** Should this run as a daemon? */
	private boolean daemon = true;

	/** Time to wait till re-reading data */
	private int sleepTime = 30000;

	/** List of listeners */
	protected List<SensorListener> listeners =
		new LinkedList<SensorListener>();

	/** Create a new XmlClient */
	protected XmlClient(URL u, Logger l) throws SAXException,
		ParserConfigurationException
	{
		super();
		url = u;
		logger = l;
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

	/** Read and parse an XML file */
	abstract protected void readXmlFile() throws Exception;

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

	/** Notifier for TDXML listeners */
	abstract protected class Notifier {
		abstract void notify(SensorListener l);
	}

	/** Notify all listeners of an update */
	protected void doNotify(Notifier n) {
		for(SensorListener l: listeners)
			n.notify(l);
	}

	/** Notify listeners of the start of new data */
	protected void notifyStart() {
		doNotify(new Notifier() {
			void notify(SensorListener l) {
				l.update(false);
			}
		});
	}

	/** Notify listeners that new data is finished */
	protected void notifyFinish() {
		doNotify(new Notifier() {
			void notify(SensorListener l) {
				l.update(true);
			}
		});
	}

	/** Notify listeners of a sensor data sample */
	protected void notifySample(final SensorSample s) {
		doNotify(new Notifier() {
			void notify(SensorListener l) {
				l.update(s);
			}
		});
	}
}
