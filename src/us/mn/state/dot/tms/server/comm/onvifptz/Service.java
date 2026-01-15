/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.onvifptz;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Random;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default Service class for ONVIF messages.
 * Handles building base messages, adding security headers, and sending requests
 *
 * @author Ethan Beauclaire
 */
public abstract class Service {
	protected String endpoint;
	protected String username;
	protected String password;
	protected String WSDL;
	protected final String SOAP = "http://www.w3.org/2003/05/soap-envelope";
	protected final String TT = "http://www.onvif.org/ver10/schema";
	protected final String WSSE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	protected final String WSU = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
	static int nonceLength = 16;

	/** Logger method */
	protected void log(String s) {
		OnvifPTZPoller.slog("Service:" + s);
	}

	protected String getSOAPAction(Document doc) {
		Element body = (Element) doc.getElementsByTagNameNS(SOAP, "Body").item(0);
		Element action = null;

		NodeList children = body.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				action = (Element) n;
				break;
			}
		}
		return action.getNamespaceURI() + "/" + action.getLocalName();
	}

	/**
	 * Get the base document that all other services add to; creates the header
	 * and body, and adds relevant namespace attributes.
	 */
	protected Document getBaseDocument() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			log("getBaseDocument: " + e.getMessage());
			return null;
		}

		db.setErrorHandler(new DOMUtils.OnvifErrorHandler("New base document"));
		Document d = db.newDocument();
		d.setXmlStandalone(true);
		Element envelope = d.createElementNS(SOAP, "s:Envelope");
		d.appendChild(envelope);
		Element header = d.createElementNS(SOAP, "s:Header");
		envelope.appendChild(header);

		Element body = d.createElementNS(SOAP, "s:Body");
		body.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
		body.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		envelope.appendChild(body);

		return d;
	}

	/** Generates a random nonceLength-byte string for use as a nonce in password digest */
	private static String getNonce() {
		Random random = new SecureRandom();
		StringBuilder nonce = new StringBuilder();

		char[] allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
		for (int i = 0; i < nonceLength; i++) {
			nonce.append(allowedChars[random.nextInt(allowedChars.length)]);
		}

		return nonce.toString();
	}

	/** Returns the current UTC time for use in password digest */
	private static String getUTCTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(new Date());
	}

	/**
	 * Add the security headers to the document, using WS-Security specification
	 *
	 * @param doc the document for which to add headers
	 */
	protected Document addSecurityHeaderDocument(Document doc) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		Element env = (Element) doc.getElementsByTagNameNS(SOAP, "Envelope").item(0);
		Element head = (Element) env.getElementsByTagNameNS(SOAP, "Header").item(0);
		Element sec = doc.createElementNS(WSSE, "Security");
		sec.setAttributeNS(SOAP, "s:mustUnderstand", "1");
		head.appendChild(sec);
		Element usernameToken = doc.createElement("UsernameToken");
		sec.appendChild(usernameToken);
		Element usernameElement = doc.createElement("Username");
		usernameElement.appendChild(doc.createTextNode(username));
		usernameToken.appendChild(usernameElement);
		Element passwordElement = doc.createElement("Password");
		usernameToken.appendChild(passwordElement);
		Element nonce = doc.createElement("Nonce");
		usernameToken.appendChild(nonce);
		Element created = doc.createElementNS(WSU, "Created");
		usernameToken.appendChild(created);

		// Generate and encode nonce, inserting it into the Nonce element
		Base64.Encoder e = Base64.getEncoder();
		String n = getNonce();
		byte[] nonceBinaryData = n.getBytes("UTF-8");
		String nonceBase64 = e.encodeToString(nonceBinaryData);
		nonce.setAttribute("EncodingType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
		nonce.appendChild(doc.createTextNode(nonceBase64));

		// Get the date and save it into the Created element
		String utctimeStringData = getUTCTime();
		byte[] utctimeBinaryData = utctimeStringData.getBytes("UTF-8");
		created.appendChild(doc.createTextNode(utctimeStringData));

		// Encode the password and add it to the element
		byte[] passwordBinaryData = password.getBytes("UTF-8");
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		sha1.update(nonceBinaryData);
		sha1.update(utctimeBinaryData);
		sha1.update(passwordBinaryData);
		byte[] passwordDigest = sha1.digest();
		String passwordDigestBase64 = e.encodeToString(passwordDigest);
		passwordElement.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest");
		passwordElement.appendChild(doc.createTextNode(passwordDigestBase64));

		return doc;
	}

	/**
	 * Default class to send a built request document
	 *
	 * Adds security headers by default
	 */
	public String sendRequestDocument(Document doc) throws IOException {
		return sendRequestDocument(doc, true, "");
	}

	/**
	 * Send request document and only specify security
	 */
	public String sendRequestDocument(Document doc, boolean doSecurity) throws IOException {
		return sendRequestDocument(doc, doSecurity, "");
	}

	/**
	 * Send request document and only specify SOAP Action
	 */
	public String sendRequestDocument(Document doc, String soapAction) throws IOException {
		return sendRequestDocument(doc, true, soapAction);
	}

	/**
	 * Sends an XML message to the handler URL, specified in the service fields;
	 * adds authentication headers if specified.
	 *
	 * @param doc the XML DOM Document to send
	 * @return the response from the device, as a String
	 */
	public String sendRequestDocument(Document doc, boolean doSecurity, String soapAction) throws IOException {
		if (endpoint == null) return "No service endpoint specified";
		String resp = "";

		URL url = new URL(endpoint);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		if (!"".equals(soapAction)) {
			connection.setRequestProperty("Content-Type",
					"application/soap+xml; charset=utf-8; action=\"" + soapAction + "\"");
			connection.setRequestProperty("SOAPAction",
					"\"" + soapAction + "\"");
		} else {
			connection.setRequestProperty("Content-Type",
					"application/soap+xml; charset=utf-8");
		}
		connection.setRequestProperty("Connection", "Close");
		connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		connection.setRequestProperty("Accept", "application/soap+xml");

		if (doSecurity && !"".equals(username)) {
			try {
				addSecurityHeaderDocument(doc);
			}
			catch (Exception e) {
				log("sendRequestDocument: " + e.getMessage());
				return "Could not add security header";
			}
		} else {
			log("Sending unauthenticated request...");
		}

		String soapRequest = DOMUtils.getString(doc);
		if (soapRequest == null) return "Could not convert document to string";
		log("\nSending soapRequest to " + endpoint + ":\n" + soapRequest);
		//log("\nSending soapRequest...");

		try (OutputStream os = connection.getOutputStream()) {
			byte[] input = soapRequest.getBytes("utf-8");
			os.write(input, 0, input.length);
		}

		int responseCode = connection.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			InputStream is = connection.getInputStream();
			if (is == null) return "HTTP_OK: " + responseCode + "; error reading stream";
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			resp = in.lines().collect(Collectors.joining("\n"));
			in.close();
		} else {
			log("Request failed. Response code: " + responseCode);

			InputStream is = connection.getErrorStream();
			if (is == null) return "HTTP error: " + responseCode + "; error reading stream";

			BufferedReader err = new BufferedReader(new InputStreamReader(is));
			resp = err.lines().collect(Collectors.joining("\n"));
			err.close();
		}

		connection.disconnect();

		return resp;
	}
}
