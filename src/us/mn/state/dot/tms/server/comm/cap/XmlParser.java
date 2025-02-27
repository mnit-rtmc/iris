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
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Common Alerting Protocol (CAP) XML document parser.
 *
 * Parses CAP XML documents and converts alerts to JSON.
 *
 * @author Douglas Lau
 */
public class XmlParser implements AlertParser {

	/** Parse alerts */
	@Override
	public List<JSONObject> parse(String doc) throws IOException {
		try {
			Date now = TimeSteward.getDateInstance();
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser parser = spf.newSAXParser();
			InputStream is = new ByteArrayInputStream(
				doc.getBytes("UTF-8"));
			AlertHandler h = new AlertHandler();
			parser.parse(is, h);
			IpawsThread.setReqSuccess(now);
			return h.getAlerts();
		}
		catch (ParserConfigurationException | SAXException e) {
			throw new ParsingException(e);
		}
	}
}
