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

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Service for ONVIF device messages
 *
 * @author Ethan Beauclaire
 */
public class DeviceService extends Service {
	public DeviceService(String deviceServiceAddress, String u, String p) {
		endpoint = deviceServiceAddress;
		namespace = "http://www.onvif.org/ver10/device/wsdl";
		username = u;
		password = p;
	}

	public static DeviceService getDeviceService(String deviceServiceAddress, String u, String p) {
		return new DeviceService(deviceServiceAddress, u, p);
	}

	/** Document builder function for GetServices */
	public Document getServicesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getServices = doc.createElement("wsdl:GetServices");
		body.appendChild(getServices);

		return doc;
	}

	/** Get information about services on the device, including service capabilities */
	public String getServices() throws IOException {
		Document doc = getServicesDocument();
		return sendRequestDocument(doc);
	}

	/** Gets a service address by its namespace */
	public String getServiceAddr(String name, String capabilitiesRes)
		throws IOException
	{
		if (capabilitiesRes == null || capabilitiesRes.isEmpty()) {
			capabilitiesRes = getCapabilities();
		}
		Document capabilitiesDoc = DOMUtils.getDocument(capabilitiesRes);
		if (capabilitiesDoc == null) return null;

		Element capabilities = (Element) capabilitiesDoc.getElementsByTagNameNS("*", "Capabilities").item(0);
		Element service = (Element) capabilities.getElementsByTagNameNS("*", name).item(0);
		return ((Element) service.getElementsByTagNameNS("*", "XAddr").item(0)).getTextContent();
	}

	/** Get the PTZ binding address */
	public String getPTZBinding(String capabilities) throws IOException {
		return getServiceAddr("PTZ", capabilities);
	}

	/** Get the media binding address */
	public String getMediaBinding(String capabilities) throws IOException {
		return getServiceAddr("Media", capabilities);
	}

	/** Get the imaging binding address */
	public String getImagingBinding(String capabilities) throws IOException {
		return getServiceAddr("Imaging", capabilities);
	}

	/** Document builder function for GetScopes */
	public Document getScopesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getScopes = doc.createElement("wsdl:GetScopes");
		body.appendChild(getScopes);

		return doc;
	}

	/** Get the scope parameters of the device */
	public String getScopes() throws IOException {
		Document doc = getScopesDocument();
		return sendRequestDocument(doc);
	}

	/** Document builder function for GetServiceCapabilities */
	public Document getServiceCapabilitiesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getServiceCapabilities = doc.createElement("wsdl:GetServiceCapabilities");
		body.appendChild(getServiceCapabilities);

		return doc;
	}

	/** Get the capabilities of the device service */
	public String getServiceCapabilities() throws IOException {
		Document doc = getServiceCapabilitiesDocument();
		return sendRequestDocument(doc);
	}

	/** Document builder function for GetCapabilities */
	public Document getCapabilitiesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getCapabilities = doc.createElement("wsdl:GetCapabilities");
		body.appendChild(getCapabilities);

		return doc;
	}

	/** Get the capabilities of the device */
	public String getCapabilities() throws IOException {
		Document doc = getCapabilitiesDocument();
		return sendRequestDocument(doc);
	}

	/** Document builder function for SendAuxiliaryCommand */
	public Document getAuxiliaryCommandDocument(String command, String state) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element sendAuxiliaryCommand = doc.createElement("wsdl:SendAuxiliaryCommand");
		body.appendChild(sendAuxiliaryCommand);

		Element auxiliaryCommand = doc.createElement("wsdl:AuxiliaryCommand");
		sendAuxiliaryCommand.appendChild(auxiliaryCommand);
		auxiliaryCommand.appendChild(doc.createTextNode("tt:" + command + "|" + state));

		return doc;
	}

	public Document getSystemRebootDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element systemRebootElem = doc.createElement("wsdl:SystemReboot");
		body.appendChild(systemRebootElem);

		return doc;
	}

	/** Reboots the device */
	public String systemReboot() throws IOException {
		Document doc = getSystemRebootDocument();
		return sendRequestDocument(doc);
	}
}
