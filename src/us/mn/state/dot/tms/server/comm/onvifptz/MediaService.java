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
		WSDL = "http://www.onvif.org/ver10/media/wsdl";
		username = u;
		password = p;
	}

	public static MediaService getMediaService(String mediaServiceAddress, String u, String p) {
		if (mediaServiceAddress == null || mediaServiceAddress.isEmpty())
			return null;
		return new MediaService(mediaServiceAddress, u, p);
	}

	/** Document builder function for GetProfiles */
	public Document getProfilesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagNameNS(SOAP, "Body").item(0);

		Element getProfiles = doc.createElementNS(WSDL, "wsdl:GetProfiles");
		body.appendChild(getProfiles);

		return doc;
	}

	/** Gets the list of media profiles for the device */
	public String getProfiles() throws IOException {
		Document doc = getProfilesDocument();
		return sendRequestDocument(doc);
	}

	/** Document builder function for GetVideoSources */
	public Document getVideoSourcesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagNameNS(SOAP, "Body").item(0);

		Element getVideoSources = doc.createElementNS(WSDL, "wsdl:GetVideoSources");
		body.appendChild(getVideoSources);

		return doc;
	}

	/** Gets the list of available physical video inputs for the device */
	public String getVideoSources() throws IOException {
		Document doc = getVideoSourcesDocument();
		return sendRequestDocument(doc);
	}
}
