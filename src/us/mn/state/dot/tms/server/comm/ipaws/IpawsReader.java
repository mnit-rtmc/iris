/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ipaws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.IpawsAlert;
import us.mn.state.dot.tms.IpawsAlertHelper;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.IpawsAlertImpl;
import us.mn.state.dot.tms.server.IpawsProcJob;
import us.mn.state.dot.tms.utils.Json;

/**
 * Integrated Public Alert and Warning System (IPAWS) alert reader. Reads and
 * parses IPAWS CAP XMLs into IpawsAlert objects and saves them to the
 * database.
 *
 * @author Michael Janson
 * @author Gordon Parikh
 */
public class IpawsReader {

	/** Email message text for errors */
	static private final String EMAIL_MSG =
		"Error encountered in IPAWS alert parsing system.  " +
		"Check the server logs for details.  " +
		"The alert that produced the error was saved on the server " +
		"in the file: ";

	/** Date formatters */
	// 2020-05-12T21:59:23-00:00
	static private final String dtFormat = "yyyy-MM-dd'T'HH:mm:ssX";
	static private final SimpleDateFormat dtFormatter =
			new SimpleDateFormat(dtFormat);

	/** Parse a date from an XML value */
	static private Date parseDate(String dte) {
		try {
			return dtFormatter.parse(dte);
		} catch (ParseException | NullPointerException e) {
			return null;
		}
	}

	/** Read alerts from an InputStream */
	static public void readIpaws(InputStream is) throws IOException {
		// make a copy of the input stream - if we hit an exception we
		// will save the XML and the text of the exception on the server
		// TODO make these controllable with a system attribute
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while ((len = is.read(buf)) > -1)
			baos.write(buf, 0, len);
		baos.flush();
		InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
		try {
			parseAlerts(is1);
		}
		catch (ParserConfigurationException | SAXException |
			ParseException | TMSException | SonarException e)
		{
			e.printStackTrace();

			// save the XML contents to a file
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(
				"yyyyMMdd-HHmmss");
			String dts = dtf.format(LocalDateTime.now());
			String fn = String.format(
				"/var/log/iris/IpawsAlert_err_%s.xml", dts);
			OutputStream xmlos = new FileOutputStream(fn);
			baos.writeTo(xmlos);

			IpawsProcJob.sendEmailAlert(EMAIL_MSG + fn);
		}
	}

	/** Parse alerts from an input stream as an XML document */
	static private void parseAlerts(InputStream is) throws IOException,
		ParseException, ParserConfigurationException, SAXException,
		SonarException, TMSException
	{
		DocumentBuilderFactory dbFactory =
			DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(is);
		doc.getDocumentElement().normalize();

		NodeList nodes = doc.getElementsByTagName("alert");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
				parseAlert((Element) node);
		}
	}

	/** Lookup or create an IPAWS alert */
	static private IpawsAlertImpl lookupOrCreateAlert(String alertId)
		throws TMSException, SonarException
	{
		IpawsAlert xa = IpawsAlertHelper.lookupByIdentifier(alertId);
		if (xa instanceof IpawsAlertImpl) {
			IpawsProcJob.log("updating alert with name: " +
				xa.getName());
			return (IpawsAlertImpl) xa;
		} else {
			String name = IpawsAlertImpl.createUniqueName();
			IpawsProcJob.log("creating alert with name: " + name);
			IpawsAlertImpl ia = new IpawsAlertImpl(name, alertId);
			ia.notifyCreate();
			return ia;
		}
	}

	/** Parse an IPAWS alert element */
	static private void parseAlert(Element element) throws ParseException,
		SonarException, TMSException
	{
		// check if the alert exists
		String alertId = getTagValue("identifier", element);
		IpawsAlertImpl ia = lookupOrCreateAlert(alertId);

		// either way set all the values
		ia.setSenderNotify(getTagValue("sender", element));
		ia.setSentDateNotify(parseDate(getTagValue("sent", element)));
		ia.setStatusNotify(getTagValue("status", element));
		ia.setMsgTypeNotify(getTagValue("msgType", element));
		ia.setScopeNotify(getTagValue("scope", element));
		ia.setCodesNotify(getTagValueArray("code", element));
		ia.setNoteNotify(getTagValue("note", element));
		ia.setAlertReferencesNotify(getTagValueArray("references",
			element));
		ia.setIncidentsNotify(getTagValueArray("incidents", element));
		ia.setCategoriesNotify(getTagValueArray("category", element));
		ia.setEventNotify(getTagValue("event", element));
		ia.setResponseTypesNotify(getTagValueArray("responseType",
			element));
		ia.setUrgencyNotify(getTagValue("urgency", element));
		ia.setSeverityNotify(getTagValue("severity", element));
		ia.setCertaintyNotify(getTagValue("certainty", element));
		ia.setAudienceNotify(getTagValue("audience", element));
		ia.setEffectiveDateNotify(parseDate(getTagValue("effective",
			element)));
		ia.setOnsetDateNotify(parseDate(getTagValue("onset", element)));
		ia.setExpirationDateNotify(parseDate(getTagValue("expires",
			element)));
		ia.setSenderNameNotify(getTagValue("senderName", element));
		ia.setHeadlineNotify(getTagValue("headline", element));
		ia.setAlertDescriptionNotify(getTagValue("description",
			element));
		ia.setInstructionNotify(getTagValue("instruction", element));
		ia.setParametersNotify(getValuePairJson("parameter", element));
		ia.setAreaNotify(getAreaJson(element));
	}

	/** Get the first child element with a given tag name */
	static private String getTagValue(String tag, Element element) {
		NodeList nodes = element.getElementsByTagName(tag);
		return (nodes.getLength() > 0)
		      ? nodes.item(0).getTextContent()
		      : null;
	}

	/** Get all child elements with a given tag name as an array */
	static private List<String> getTagValueArray(String tag,
		Element element)
	{
		List<String> values = new ArrayList<String>();
		NodeList nodes = element.getElementsByTagName(tag);
		for (int i = 0; i < nodes.getLength(); i++) {
			values.add(nodes.item(i).getTextContent());
		}
		return values;
	}

	/** Get key/value pairs as JSON */
	static private String getValuePairJson(String tag, Element element) {
		HashMap<String, ArrayList<String>> kvPairs =
			getChildElements(tag, element);

		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for (String key: kvPairs.keySet()) {
			ArrayList<String> vals = kvPairs.get(key);
			// FIXME the && !"UGC".equals(key) is a hack to make
			//       sure we always get UGC codes as an array (it
			//       works well enough though)
			if (vals.size() > 1 || "UGC".equals(key)) {
				String[] valsArr = vals.toArray(new String[0]);
				sb.append(Json.arr(key, valsArr));
			} else
				sb.append(Json.str(key, vals.get(0)));
		}
		if (sb.charAt(sb.length() - 1) == ',')
			sb.setLength(sb.length() - 1);
		sb.append("}");
		return sb.toString();
	}

	/** Store key/value pairs in a HashMap of ArrayLists to allow
	 * for multiple instances of the same key */
	static private HashMap<String, ArrayList<String>> getChildElements(
		String tag, Element element)
	{
		HashMap<String, ArrayList<String>> kvPairs =
			new HashMap<String, ArrayList<String>>();
		NodeList nodes = element.getElementsByTagName(tag);
		for (int i = 0; i < nodes.getLength(); i++) {
			Element child = (Element) nodes.item(i);
			Node keyNode = child.getElementsByTagName(
				"valueName").item(0);
			Node valueNode = child.getElementsByTagName("value")
				.item(0);
			if (keyNode != null && valueNode != null) {
				String key = keyNode.getTextContent();
				String value = valueNode.getTextContent();
				if (!kvPairs.containsKey(key))
					kvPairs.put(key, new ArrayList<String>());
				ArrayList<String> vals = kvPairs.get(key);
				vals.add(value);
			}
		}
		return kvPairs;
	}

	/** Area child elements */
	static private final String[] AREA_ELEMENTS = {"areaDesc", "polygon",
	          "circle", "geocode", "altitude", "ceiling"};

	/** Get area element as JSON */
	static private String getAreaJson(Element element) {
		StringBuilder sb = new StringBuilder();
		sb.append('{');

		// Loop through area elements
		NodeList nodes = element.getElementsByTagName("area");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element area = (Element) nodes.item(i);
			for (String ce: AREA_ELEMENTS) {
				if ("geocode".equals(ce))
					appendElementJson(ce, area, sb, true);
				else
					appendElementJson(ce, area, sb, false);
			}
		}
		if (sb.charAt(sb.length() - 1) == ',')
			sb.setLength(sb.length() - 1);
		sb.append("}");
		return sb.toString();
	}

	/** Append an element to JSON buffer.
	 *
	 *  TODO this won't handle multiple <area> or <polygon> blocks
	 *       correctly, but those seem to be rare (NWS doesn't seem to use
	 *       them even though CAP/IPAWS allows them) */
	static private void appendElementJson(String tag, Element element,
		StringBuilder sb, boolean forceKV)
	{
		NodeList nodes = element.getElementsByTagName(tag);
		if (nodes.getLength() == 1 && !forceKV) {
			sb.append(Json.str(tag, nodes.item(0).getTextContent()));
		} else if (nodes.getLength() > 1 || forceKV) {
			sb.append(Json.sub(tag, getValuePairJson(tag, element)));
			sb.append(',');
		}
	}
}
