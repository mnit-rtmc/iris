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
 * Reads a sensor sample XML document at a specified interval and notifies
 * listeners when there is new data available.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class XmlSensorClient extends XmlClient {

	/** Entity declaration */
	static protected final String ENTITY_DECL =
		"<?xml version='1.0' encoding='UTF-8'?>";

	/** Parse an attribute as an integer value */
	static protected Integer parseInt(String v) {
		try {
			if(v != null)
				return Integer.parseInt(v);
		}
		catch(NumberFormatException e) {
			// Invalid value
		}
		return null;
	}

	/** SAX parser */
	protected final SAXParser parser;

	/** Time stamp from previous read */
	protected String last_stamp = "";

	/** Flag to indicate the time stamp changed since last time */
	protected boolean time_changed = false;

	/** Create a new XmlSensorClient */
	public XmlSensorClient(URL url, Logger l) throws SAXException,
		ParserConfigurationException
	{
		super(url, l);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		parser = factory.newSAXParser();
	}

	/** Read and parse an XML file */
	protected void readXmlFile() throws Exception {
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
