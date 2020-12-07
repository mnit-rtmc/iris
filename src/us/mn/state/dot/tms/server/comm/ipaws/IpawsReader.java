/*
 * IRIS -- Intelligent Roadway Information System
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
	
	/** Date formatters */
	// 2020-05-12T21:59:23-00:00
	private static final String dtFormat = "yyyy-MM-dd'T'HH:mm:ssX";
	private static final SimpleDateFormat dtFormatter =
			new SimpleDateFormat(dtFormat);

	/** Read alerts from a directory containing XML files. */
	public static void readIpaws(String dirPath) throws IOException {
		File folder = new File(dirPath);

		File[] files = folder.listFiles();
		
		DocumentBuilderFactory dbFactory =
				DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			for (File xmlFile : files) {
					InputStream in = new GZIPInputStream(
					        new FileInputStream(xmlFile));
					Document doc = dBuilder.parse(in);
					doc.getDocumentElement().normalize();

					NodeList nodeList = doc.getElementsByTagName("alert");
					for (int i = 0; i < nodeList.getLength(); i++)
						getIpawsAlert(nodeList.item(i));
			}
		} catch(ParserConfigurationException | SAXException | SonarException
				| ParseException | TMSException e) {
			e.printStackTrace();
		}
	}
	
	/** Read alerts from an InputStream (live feed). */
	public static void readIpaws(InputStream is) throws IOException {
		// make a copy of the input stream - if we hit an exception we will
		// save the XML and the text of the exception on the server
		// TODO make these controllable with a system attribute
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while ((len = is.read(buf)) > -1)
			baos.write(buf, 0, len);
		baos.flush();
		InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
		
		DocumentBuilderFactory dbFactory =
				DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is1);
			doc.getDocumentElement().normalize();

			NodeList nodeList = doc.getElementsByTagName("alert");
			for (int i = 0; i < nodeList.getLength(); i++) {
				getIpawsAlert(nodeList.item(i));
			}
		} catch (ParserConfigurationException | SAXException | ParseException
				| TMSException | SonarException e)
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
			
			// send an email alert
			IpawsProcJob.sendEmailAlert("Error encountered in IPAWS alert " +
				"parsing system. Check the server logs for details. " + 
				"The alert that produced the error was saved on the server " +
				"in the file:  " + fn);
		}
	}
	
	private static void getIpawsAlert(Node node)
			throws ParseException, SonarException, TMSException {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element) node;
			
			// check if the alert exists
			String n = getTagValue("identifier", element);
			String name = n;
			IpawsAlertImpl ia =
					(IpawsAlertImpl) IpawsAlertHelper.lookup(name);
			
			// if it doesn't, create a new one
			if (ia == null) {
				IpawsProcJob.log("Creating new alert with name: " + name);
				ia = new IpawsAlertImpl(name);
				ia.notifyCreate();
			} else
				IpawsProcJob.log("Updating alert with name: " + name);
			
			// either way set all the values
			ia.doSetIdentifier(getTagValue("identifier", element));
			ia.doSetSender(getTagValue("sender", element));
			ia.doSetSentDate(parseDate(getTagValue("sent", element)));
			ia.doSetStatus(getTagValue("status", element));
			ia.doSetMsgType(getTagValue("msgType", element));
			ia.doSetScope(getTagValue("scope", element));
			ia.doSetCodes(getTagValueArray("code", element));
			ia.doSetNote(getTagValue("note", element));
			ia.doSetAlertReferences(getTagValueArray("references", element));
			ia.doSetIncidents(getTagValueArray("incidents", element));
			ia.doSetCategories(getTagValueArray("category", element));
			ia.doSetEvent(getTagValue("event", element));
			ia.doSetResponseTypes(getTagValueArray("responseType", element));
			ia.doSetUrgency(getTagValue("urgency", element));
			ia.doSetSeverity(getTagValue("severity", element));
			ia.doSetCertainty(getTagValue("certainty", element));
			ia.doSetAudience(getTagValue("audience", element));
			ia.doSetEffectiveDate(parseDate(getTagValue("effective", element)));
			ia.doSetOnsetDate(parseDate(getTagValue("onset", element)));
			ia.doSetExpirationDate(parseDate(getTagValue("expires", element)));
			ia.doSetSenderName(getTagValue("senderName", element));
			ia.doSetHeadline(getTagValue("headline", element));
			ia.doSetAlertDescription(getTagValue("description", element));
			ia.doSetInstruction(getTagValue("instruction", element));
			ia.doSetParameters(getValuePairJson("parameter", element));
			ia.doSetArea(getAreaJson("area", element));
		}

	}
	
	private static Date parseDate(String dte) {
		try {
			return dtFormatter.parse(dte);
		} catch(ParseException | NullPointerException e) {
			return null;
		}
	}  		
			
	private static String getTagValue(String tag, Element element) {  
		// Check if tag exists
		if (element.getElementsByTagName(tag).getLength() > 0)
			return element.getElementsByTagName(tag).item(0).getTextContent();
		else 
			return null;
	}
	   
	private static List<String> getTagValueArray(String tag, Element element) {
		List<String> tag_values = new ArrayList<String>();
		
		// Check if tag exists
		if (element.getElementsByTagName(tag).getLength() > 0) {       
			NodeList nodeList = element.getElementsByTagName(tag);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = (Node) nodeList.item(i);
				tag_values.add(node.getTextContent());
			}
		}
		return tag_values;
	}
	
	
	private static String getValuePairJson(String tag, Element element) {
		
		// store key/value pairs in a HashMap of ArrayLists to allow for
		// multiple instances of the same key
		HashMap<String,ArrayList<String>> kvPairs =
				new HashMap<String,ArrayList<String>>();
		if (element.getElementsByTagName(tag).getLength() > 0) {       
			NodeList nodeList = element.getElementsByTagName(tag);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element childElement = (Element)  nodeList.item(i);
				String key = childElement.getElementsByTagName("valueName")
				        .item(0).getTextContent();
				String value = childElement.getElementsByTagName("value")
				        .item(0).getTextContent();
				if (!kvPairs.containsKey(key))
					kvPairs.put(key, new ArrayList<String>());
				ArrayList<String> vals = kvPairs.get(key);
				vals.add(value);
			}
		}
		
		// make a JSON string with all the key/value pairs
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		
		for (String key: kvPairs.keySet()) {
			ArrayList<String> vals = kvPairs.get(key);
			
			// FIXME the && !"UGC".equals(key) is a hack to make sure we
			// always get UGC codes as an array (it works well enough though)
			if (vals.size() > 1 || "UGC".equals(key)) {
				String[] valsArr = vals.toArray(new String[0]);
				sb.append(Json.arr(key, valsArr));
			} else
				sb.append(Json.str(key, vals.get(0)));
		}
		
		// remove trailing comma
		if (sb.charAt(sb.length() - 1) == ',')
			sb.setLength(sb.length() - 1);
		sb.append("}");
		return sb.toString();
	}
	
	private static String getAreaJson(String tag, Element element) {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		
		// Check if tag exists
		if (element.getElementsByTagName(tag).getLength() > 0) {       
			NodeList areaNodeList = element.getElementsByTagName(tag);

			// Loop through areas
			for (int i = 0; i < areaNodeList.getLength(); i++) {
				Element areaElement = (Element) areaNodeList.item(i);

				String [] element_list = {"areaDesc","polygon",
				          "circle","geocode","altitude","ceiling"};
				for (String ce: element_list) {
					if ("geocode".equals(ce))
						appendElementJson(ce, areaElement, sb, true);
					else
						appendElementJson(ce, areaElement, sb, false);
				}
			}
		}
		
		// remove trailing comma
		if (sb.charAt(sb.length() - 1) == ',')
			sb.setLength(sb.length() - 1);
		sb.append("}");
		return sb.toString();
	}

	
	private static void appendElementJson(String tag,
			Element element, StringBuilder sb, boolean forceKV) {
		if (element.getElementsByTagName(tag).getLength() == 1 && !forceKV)      
			sb.append(Json.str(tag, element.getElementsByTagName(tag)
					.item(0).getTextContent()));
		// TODO this won't handle multiple <area> or <polygon> blocks
		// correctly, but those seem to be rare (NWS doesn't seem to use them
		// even though CAP/IPAWS allows them)
		else if (element.getElementsByTagName(tag).getLength() > 1 || forceKV)
			sb.append(Json.sub(tag, getValuePairJson(tag, element)));
	}
	

}
	
