/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2025  Minnesota Department of Transportation
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
package us.mn.state.dot.tms.server.comm.cap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Common Alerting Protocol (CAP) XML reader.
 *
 * Reads CAP XML documents, converts alerts to JSON and stores to the database.
 *
 * @author Douglas Lau
 * @author Michael Janson
 * @author Gordon Parikh
 */
public class CapXmlReader {

	/** Date formatter for formatting error file names */
	static private final SimpleDateFormat DT_FMT =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	/** Most recent successful request date
	 *  (at startup, initialize to an hour ago) */
	static private Date REQ_SUCCESS = new Date(
		TimeSteward.currentTimeMillis() - 60 * 60 * 1000);

	/** Get most recent successful request date */
	static public Date getReqDate() {
		return REQ_SUCCESS;
	}

	/** Get XML save enabled setting */
	static private boolean getXmlSaveEnabled() {
		return SystemAttrEnum.CAP_XML_SAVE_ENABLE.getBoolean();
	}

	/** Alert processor */
	static private final AlertProcessor PROCESSOR = new AlertProcessor();

	/** Input stream */
	private final InputStream input;

	/** Output stream to cache copy of XML */
	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

	/** Alert handler */
	private final AlertHandler handler;

	/** Create a new CAP XML reader */
	public CapXmlReader(InputStream is) {
		input = is;
		handler = new AlertHandler(PROCESSOR);
	}

	/** Parse alerts */
	public void parse() throws IOException {
		Date now = TimeSteward.getDateInstance();
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser parser = spf.newSAXParser();
			parser.parse(inputStream(), handler);
			REQ_SUCCESS = now;
		}
		catch (ParserConfigurationException | SAXException e) {
			CapPoller.slog("parse error: " + e.getMessage());
			saveXmlFile();
		}
	}

	/** Get input stream containing the XML */
	private InputStream inputStream() throws IOException {
		if (getXmlSaveEnabled()) {
			// make a copy of the input stream - if we hit an
			// exception we will save the XML and the text of the
			// exception on the server
			byte[] buf = new byte[1024];
			int len;
			while ((len = input.read(buf)) > -1)
				baos.write(buf, 0, len);
			baos.flush();
			return new ByteArrayInputStream(baos.toByteArray());
		} else
			return input;
	}

	/** Save the XML contents to a file */
	private void saveXmlFile() throws IOException {
		if (getXmlSaveEnabled()) {
			String fn = "/var/log/iris/cap_err_" + DT_FMT.format(
				TimeSteward.getDateInstance()) + ".xml";
			baos.writeTo(new FileOutputStream(fn));
		}
	}
}
