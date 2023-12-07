/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.onvifptz.lib;

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
import java.text.SimpleDateFormat;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.Node;
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
	protected String namespace;
	protected String username;
	protected String password;
	protected boolean authenticate;

	protected Document getBaseDocument() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}

		Document d = db.newDocument();
		d.setXmlStandalone(true);
		Element envelope = d.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "SOAP-ENV:Envelope");
		envelope.setAttribute("xmlns:wsdl", namespace);
		envelope.setAttribute("xmlns:tt", "http://www.onvif.org/ver10/schema");
		d.appendChild(envelope);
		Element header = d.createElement("SOAP-ENV:Header");
		envelope.appendChild(header);

		Element body = d.createElement("SOAP-ENV:Body");
		envelope.appendChild(body);

		return d;
	}

	/**
	 * Generates a random 16-byte string for use as a nonce in password digest
	 */
	private static String getNonce() {
		Random random = new SecureRandom();
		StringBuilder nonce = new StringBuilder();

		char[] allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
		for (int i = 0; i < 16; i++) {
			nonce.append(allowedChars[random.nextInt(allowedChars.length)]);
		}

		return nonce.toString();
	}

	private static String getUTCTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(new Date());
	}

	protected Document addSecurityHeaderDocument(Document doc) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		Element env = (Element) doc.getElementsByTagName("SOAP-ENV:Envelope").item(0);
		env.setAttribute("xmlns:wsse",
			"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		env.setAttribute("xmlns:wsu",
			"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

		Element head = (Element) env.getElementsByTagName("SOAP-ENV:Header").item(0);
		Element sec = doc.createElement("wsse:Security");
		head.appendChild(sec);
		Element usernameToken = doc.createElement("wsse:UsernameToken");
		sec.appendChild(usernameToken);
		Element usernameElement = doc.createElement("wsse:Username");
		usernameElement.appendChild(doc.createTextNode(username));
		usernameToken.appendChild(usernameElement);
		Element passwordElement = doc.createElement("wsse:Password");
		usernameToken.appendChild(passwordElement);
		Element nonce = doc.createElement("wsse:Nonce");
		usernameToken.appendChild(nonce);
		Element created = doc.createElement("wsu:Created");
		usernameToken.appendChild(created);

		// Generate and encode nonce, inserting it into the Nonce element
		Base64.Encoder e = Base64.getEncoder();
		byte[] nonceBinaryData = getNonce().getBytes("UTF-8");
		String nonceBase64 = e.encodeToString(nonceBinaryData);
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

	public String sendRequestDocument(Document doc) {
		String resp = "";
		try {
			URL url = new URL(endpoint);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");

			if (authenticate && !"".equals(username)) {
				addSecurityHeaderDocument(doc);
			} else {
				System.out.println("Sending unauthenticated request...");
			}

			String soapRequest = DOMUtils.getString(doc);
			System.out.println("Final SOAP request:\n" + soapRequest);

			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = soapRequest.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					String inputLine;
					StringBuilder response = new StringBuilder();
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}

					// Process the response here (XML parsing, etc.)
					resp = response.toString();
				}
			} else {
				resp = "Request failed. Response code: " + responseCode;
			}

			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
}
