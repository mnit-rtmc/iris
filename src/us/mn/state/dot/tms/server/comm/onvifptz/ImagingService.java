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
 * Service for ONVIF imaging messages
 *
 * @author Ethan Beauclaire
 */
public class ImagingService extends Service {
	public ImagingService(String imagingServiceAddress, String u, String p) {
		endpoint = imagingServiceAddress;
		namespace = "http://www.onvif.org/ver20/imaging/wsdl";
		username = u;
		password = p;
	}

	public static ImagingService getImagingService(String imagingServiceAddress, String u, String p) {
		return new ImagingService(imagingServiceAddress, u, p);
	}

	/** Document builder function for GetOptions */
	public Document getOptionsDocument(String vToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getOptions = doc.createElement("wsdl:GetOptions");
		body.appendChild(getOptions);

		Element videoSourceToken = doc.createElement("wsdl:VideoSourceToken");
		videoSourceToken.appendChild(doc.createTextNode(vToken));
		getOptions.appendChild(videoSourceToken);

		return doc;
	}

	/**
	 * Gets the valid ranges for relevant imaging parameters
	 *
	 * @param vToken reference token to the relevant video source
	 */
	public String getOptions(String vToken) throws IOException {
		Document doc = getOptionsDocument(vToken);
		return sendRequestDocument(doc);
	}

	/** Document builder function for GetImagingSettings */
	public Document getImagingSettingsDocument(String vToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getImagingSettings = doc.createElement("wsdl:GetImagingSettings");
		body.appendChild(getImagingSettings);

		Element videoSourceToken = doc.createElement("wsdl:VideoSourceToken");
		videoSourceToken.appendChild(doc.createTextNode(vToken));
		getImagingSettings.appendChild(videoSourceToken);

		return doc;
	}

	/**
	 * Gets the imaging configuration for the requested video source
	 *
	 * @param vToken reference token to the relevant video source
	 */
	public String getImagingSettings(String vToken) throws IOException {
		Document doc = getImagingSettingsDocument(vToken);
		return sendRequestDocument(doc);
	}

	/**
	 * Document builder function for SetImagingSettings, as used by focus and
	 * iris requests.
	 */
	public Document setImagingSettingsDocument(String vToken, String setting, String value) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element setImagingSettings = doc.createElement("wsdl:SetImagingSettings");
		body.appendChild(setImagingSettings);

		Element videoSourceToken = doc.createElement("wsdl:VideoSourceToken");
		videoSourceToken.appendChild(doc.createTextNode(vToken));
		setImagingSettings.appendChild(videoSourceToken);

		Element imagingSettings = doc.createElement("wsdl:ImagingSettings");
		setImagingSettings.appendChild(imagingSettings);

		// Add more as implemented:
		switch (setting) {
			case "focus":
				Element focus = doc.createElement("tt:Focus");
				imagingSettings.appendChild(focus);

				Element afMode = doc.createElement("tt:AFMode");
				focus.appendChild(afMode);

				Element autoFocusMode = doc.createElement("tt:AutoFocusMode");
				focus.appendChild(autoFocusMode);
				if (!"auto".equalsIgnoreCase(value)) {
					autoFocusMode.appendChild(doc.createTextNode("MANUAL"));
					// only allows options for autofocus; use Move to set lens
				} else {
					autoFocusMode.appendChild(doc.createTextNode("AUTO"));
				}
				break;
			case "iris":
				Element exposure = doc.createElement("tt:Exposure");
				imagingSettings.appendChild(exposure);
				if (!"auto".equalsIgnoreCase(value) && !"manual".equalsIgnoreCase(value)) {
					Element mode = doc.createElement("tt:Mode");
					mode.appendChild(doc.createTextNode("MANUAL"));
					exposure.appendChild(mode);
					// uncomment if necessary at some point:
					//Element eTime = doc.createElement("tt:ExposureTime");
					//eTime.appendChild(doc.createTextNode("0.0"));
					//exposure.appendChild(eTime);
					//Element gain = doc.createElement("tt:Gain");
					//gain.appendChild(doc.createTextNode("0.48"));
					//exposure.appendChild(gain);
					Element iris = doc.createElement("tt:Iris");
					iris.appendChild(doc.createTextNode(value));
					exposure.appendChild(iris);
				} else if ("auto".equalsIgnoreCase(value)) {
					Element mode = doc.createElement("tt:Mode");
					mode.appendChild(doc.createTextNode("AUTO"));
					exposure.appendChild(mode);
				} else {
					Element mode = doc.createElement("tt:Mode");
					mode.appendChild(doc.createTextNode("MANUAL"));
					exposure.appendChild(mode);
				}
				break;
		}

		return doc;
	}

	/**
	 * Sets the focus configuration (auto, manual)
	 *
	 * @param vToken reference token to the relevant video source
	 * @param value  value to set focus mode; "auto" or "manual"
	 */
	public String setFocus(String vToken, String value) throws IOException {
		Document doc = setImagingSettingsDocument(vToken, "focus", value);
		return sendRequestDocument(doc);
	}

	/**
	 * Sets the iris attenuation
	 *
	 * @param vToken reference token to the relevant video source
	 * @param value  the requested iris attenuation; "auto", "manual", or a float
	 */
	public String setIris(String vToken, String value) throws IOException {
		Document doc = setImagingSettingsDocument(vToken, "iris", value);
		return sendRequestDocument(doc);
	}

	/**
	 * Gets the iris attenuation
	 *
	 * @param vToken reference token to the relevant video source
	 *
	 * @return float value of iris
	 */
	public float getIris(String vToken) throws IOException {
		String docString = getImagingSettings(vToken);
		Document doc = DOMUtils.getDocument(docString);
		if (doc == null) return 0;

		Element iris = (Element) doc.getElementsByTagNameNS("*", "Iris").item(0);
		return Float.parseFloat(iris.getTextContent());
	}

	/**
	 * Increments the iris attenuation by retrieving and setting it (absolute)
	 *
	 * @param vToken reference token to the relevant video source
	 * @param value  the requested iris attenuation; "auto", "manual", or a float
	 */
	public String incrementIris(String vToken, String value)
		throws IOException
	{
		float newIris = getIris(vToken) + Float.parseFloat(value);
		return setIris(vToken, String.valueOf(newIris));
	}

	/** Document builder function for GetMoveOptions */
	public Document getMoveOptionsDocument(String vToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getMoveOptions = doc.createElement("wsdl:GetMoveOptions");
		body.appendChild(getMoveOptions);

		Element videoSourceToken = doc.createElement("wsdl:VideoSourceToken");
		videoSourceToken.appendChild(doc.createTextNode(vToken));
		getMoveOptions.appendChild(videoSourceToken);

		return doc;
	}

	/**
	 * Gets the move options for the focus lens
	 *
	 * @param vToken reference token to the relevant video source
	 */
	public String getMoveOptions(String vToken) throws IOException {
		Document doc = getMoveOptionsDocument(vToken);
		return sendRequestDocument(doc);
	}

	/** Document builder function for Move request; takes move mode as a parameter. */
	public Document getMoveDocument(String vToken, float distance, String mode) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element moveElement = doc.createElement("wsdl:Move");
		body.appendChild(moveElement);

		Element videoSourceToken = doc.createElement("wsdl:VideoSourceToken");
		videoSourceToken.appendChild(doc.createTextNode(vToken));
		moveElement.appendChild(videoSourceToken);

		Element focusElement = doc.createElement("wsdl:Focus");
		moveElement.appendChild(focusElement);
		// TODO: add autoselection with GetMoveOptions/verify Relative capability
		switch (mode.toLowerCase()) {
			case "absolute":
				// TODO: use GetStatus to get current, then add distance
				// (only necessary if camera doesn't support relative)
				Element absolute = doc.createElement("tt:Absolute");
				focusElement.appendChild(absolute);
				Element position = doc.createElement("tt:Position");
				position.appendChild(doc.createTextNode(String.valueOf(distance)));
				absolute.appendChild(position);
				break;
			case "continuous":
				// Treat distance like speed
				Element continuous = doc.createElement("tt:Continuous");
				focusElement.appendChild(continuous);
				Element speed = doc.createElement("tt:Speed");
				speed.appendChild(doc.createTextNode(String.valueOf(distance)));
				continuous.appendChild(speed);
				break;
			case "relative":
			default:
				Element relative = doc.createElement("tt:Relative");
				focusElement.appendChild(relative);
				Element distanceElement = doc.createElement("tt:Distance");
				distanceElement.appendChild(doc.createTextNode(String.valueOf(distance)));
				relative.appendChild(distanceElement);
				break;
		}

		return doc;
	}

	/**
	 * Moves the focus lens
	 *
	 * @param vToken   reference token to the relevant video source
	 * @param distance the requested move distance
	 * @param mode     mode to send to device ("continuous", "absolute", "relative")
	 */
	public String moveFocus(String vToken, float distance, String mode)
		throws IOException
	{
		Document doc = getMoveDocument(vToken, distance, mode);
		return sendRequestDocument(doc);
	}

	/**
	 * Moves the focus lens
	 *
	 * @param vToken   reference token to the relevant video source
	 * @param distance the requested move distance
	 */
	public String moveFocus(String vToken, float distance)
		throws IOException
	{
		return moveFocus(vToken, distance, "continuous");
	}

	/** Document builder function for GetStatus */
	public Document getStatusDocument(String vToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getStatusElement = doc.createElement("wsdl:GetStatus");
		body.appendChild(getStatusElement);

		Element videoSourceToken = doc.createElement("wsdl:VideoSourceToken");
		videoSourceToken.appendChild(doc.createTextNode(vToken));
		getStatusElement.appendChild(videoSourceToken);

		return doc;
	}

	/**
	 * Gets the current status (position, MoveStatus, extension) of the focus lens
	 *
	 * @param vToken reference token to the relevant video source
	 */
	public String getStatus(String vToken) throws IOException {
		Document doc = getStatusDocument(vToken);
		return sendRequestDocument(doc);
	}

	/** Document builder function for Stop */
	public Document getStopDocument(String vToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element stopElement = doc.createElement("wsdl:Stop");
		body.appendChild(stopElement);

		Element videoSourceToken = doc.createElement("wsdl:VideoSourceToken");
		videoSourceToken.appendChild(doc.createTextNode(vToken));
		stopElement.appendChild(videoSourceToken);

		return doc;
	}

	/**
	 * Stops all manual focus movements of the lens
	 *
	 * @param vToken reference token to the relevant video source
	 */
	public String stop(String vToken) throws IOException {
		Document doc = getStopDocument(vToken);
		return sendRequestDocument(doc);
	}
}
