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

import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Service for ONVIF media messages
 *
 * @author Ethan Beauclaire
 */
public class MediaService extends Service {
	public MediaService(String mediaServiceAddress, String u, String p) {
		endpoint = mediaServiceAddress;
		namespace = "http://www.onvif.org/ver10/media/wsdl";
		username = u;
		password = p;
		authenticate = true;
	}

	public static MediaService getMediaService(String mediaServiceAddress, String u, String p) {
		return new MediaService(mediaServiceAddress, u, p);
	}

	public Document getProfilesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getProfiles = doc.createElement("wsdl:GetProfiles");
		body.appendChild(getProfiles);

		return doc;
	}

	public String getProfiles() {
		Document doc = getProfilesDocument();
		return sendRequestDocument(doc);
	}

	public Document getVideoSourcesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getVideoSources = doc.createElement("wsdl:GetVideoSources");
		body.appendChild(getVideoSources);

		return doc;
	}

	public String getVideoSources() {
		Document doc = getVideoSourcesDocument();
		return sendRequestDocument(doc);
	}
}
