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
 * Service for ONVIF PTZ messages
 *
 * @author Ethan Beauclaire
 */
public class PTZService extends Service {
	public PTZService(String ptzServiceAddress, String u, String p) {
		endpoint = ptzServiceAddress;
		namespace = "http://www.onvif.org/ver20/ptz/wsdl";
		username = u;
		password = p;
		authenticate = true;
	}

	public static PTZService getPTZService(String ptzServiceAddress, String u, String p) {
		return new PTZService(ptzServiceAddress, u, p);
	}

	/** Document builder function for GetConfigurations */
	public Document getConfigurationsDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element elem = doc.createElement("wsdl:GetConfigurations");
		body.appendChild(elem);

		return doc;
	}

	/** Gets the list of existing PTZ configurations and their constraints on the device */
	public String getConfigurations() {
		Document doc = getConfigurationsDocument();
		return sendRequestDocument(doc);
	}

	/** Document builder function for GetConfigurationOptions */
	public Document getConfigurationOptionsDocument(String cToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element configOptions = doc.createElement("wsdl:GetConfigurationOptions");
		body.appendChild(configOptions);

		Element configToken = doc.createElement("wsdl:ConfigurationToken");
		configToken.appendChild(doc.createTextNode(cToken));
		configOptions.appendChild(configToken);

		return doc;
	}

	/**
	 * Gets the list of supported coordinate systems and range limitations
	 * of a configuration
	 *
	 * @param cToken reference token to the relevant PTZ configuration
	 */
	public String getConfigurationOptions(String cToken) {
		Document doc = getConfigurationOptionsDocument(cToken);
		return sendRequestDocument(doc);
	}

	/** Document builder function for GetNodes */
	public Document getNodesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getNodes = doc.createElement("wsdl:GetNodes");
		body.appendChild(getNodes);

		return doc;
	}

	/** Gets the descriptions of available PTZ nodes */
	public String getNodes() {
		Document doc = getNodesDocument();
		return sendRequestDocument(doc);
	}

	/** Document builder function for GetNode */
	public Document getNodeDocument(String nToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getNode = doc.createElement("wsdl:GetNode");
		body.appendChild(getNode);

		Element nodeToken = doc.createElement("wsdl:NodeToken");
		nodeToken.appendChild(doc.createTextNode(nToken));
		getNode.appendChild(nodeToken);

		return doc;
	}

	/**
	 * Gets information about a specific PTZ node
	 *
	 * @param nToken reference token to the relevant PTZ node
	 */
	public String getNode(String nToken) {
		Document doc = getNodeDocument(nToken);
		return sendRequestDocument(doc);
	}

	/** Document builder function for ContinuousMove */
	public Document getContinuousMoveDocument(float xVel, float yVel, float zVel) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element continuousMove = doc.createElement("wsdl:ContinuousMove");
		body.appendChild(continuousMove);

		Element profileToken = doc.createElement("wsdl:ProfileToken");
		profileToken.appendChild(doc.createTextNode("Profile1"));
		continuousMove.appendChild(profileToken);

		Element velocity = doc.createElement("wsdl:Velocity");
		continuousMove.appendChild(velocity);

		Element panTilt = doc.createElement("tt:PanTilt");
		panTilt.setAttribute("x", String.valueOf(xVel));
		panTilt.setAttribute("y", String.valueOf(yVel));
		panTilt.setAttribute("space", "http://www.onvif.org/ver10/tptz/PanTiltSpaces/VelocityGenericSpace");
		velocity.appendChild(panTilt);

		Element zoom = doc.createElement("tt:Zoom");
		zoom.setAttribute("x", String.valueOf(zVel));
		zoom.setAttribute("space", "http://www.onvif.org/ver10/tptz/ZoomSpaces/VelocityGenericSpace");
		velocity.appendChild(zoom);

		return doc;
	}

	/**
	 * Sends a continuous move operation to the PTZ node; currently uses hard-coded MediaProfile token
	 *
	 * @param x the x value (pan speed) of the move [-1.0, 1.0]
	 * @param y the y value (tilt speed) of the move [-1.0, 1.0]
	 * @param z the zoom speed of the move [-1.0, 1.0]
	 */
	public String continuousMove(float x, float y, float z) {
		Document doc = getContinuousMoveDocument(x, y, z);
		return sendRequestDocument(doc);
	}

	/** Document builder function for RelativeMove */
	public Document getRelativeMoveDocument(float x, float y, float z) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element relativeMove = doc.createElement("wsdl:RelativeMove");
		body.appendChild(relativeMove);

		Element profileToken = doc.createElement("wsdl:ProfileToken");
		profileToken.appendChild(doc.createTextNode("Profile1"));
		relativeMove.appendChild(profileToken);

		Element translation = doc.createElement("wsdl:Translation");
		relativeMove.appendChild(translation);

		Element panTilt = doc.createElement("tt:PanTilt");
		panTilt.setAttribute("x", String.valueOf(x));
		panTilt.setAttribute("y", String.valueOf(y));
		panTilt.setAttribute("space", "http://www.onvif.org/ver10/tptz/PanTiltSpaces/VelocityGenericSpace");
		translation.appendChild(panTilt);

		Element zoom = doc.createElement("tt:Zoom");
		zoom.setAttribute("x", String.valueOf(z));
		zoom.setAttribute("space", "http://www.onvif.org/ver10/tptz/ZoomSpaces/VelocityGenericSpace");
		translation.appendChild(zoom);

		return doc;
	}

	/**
	 * Sends a relative move operation to the PTZ node; currently uses hard-coded MediaProfile token
	 *
	 * @param x the x value (pan) of the move [-1.0, 1.0]
	 * @param y the y value (tilt) of the move [-1.0, 1.0]
	 * @param z the zoom of the move [-1.0, 1.0]
	 */
	public String relativeMove(float x, float y, float z) {
		Document doc = getRelativeMoveDocument(x, y, z);
		return sendRequestDocument(doc);
	}

	/** Document builder function for GetPresets */
	public Document getPresetsDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element getPresetsElement = doc.createElement("wsdl:GetPresets");
		body.appendChild(getPresetsElement);

		Element profileToken = doc.createElement("wsdl:ProfileToken");
		profileToken.appendChild(doc.createTextNode("Profile1"));
		getPresetsElement.appendChild(profileToken);

		return doc;
	}

	/**
	 * Gets the list of PTZ presets for the PTZNode of the selected
	 * MediaProfile; currently uses hardcoded profile token.
	 */
	public String getPresets() {
		Document doc = getPresetsDocument();
		return sendRequestDocument(doc);
	}

	/** Document builder function for GotoPreset */
	public Document gotoPresetDocument(String pToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element gotoPresetElement = doc.createElement("wsdl:GotoPreset");
		body.appendChild(gotoPresetElement);

		Element profileToken = doc.createElement("wsdl:ProfileToken");
		profileToken.appendChild(doc.createTextNode("Profile1"));
		gotoPresetElement.appendChild(profileToken);

		Element presetToken = doc.createElement("wsdl:PresetToken");
		presetToken.appendChild(doc.createTextNode(pToken));
		gotoPresetElement.appendChild(presetToken);

		return doc;
	}

	/**
	 * Point the camera in a saved preset direction for the PTZNode of
	 * selected MediaProfile; currently uses hardcoded profile token.
	 *
	 * @param pToken reference token to the saved PTZ preset
	 */
	public String gotoPreset(String pToken) {
		Document doc = gotoPresetDocument(pToken);
		return sendRequestDocument(doc);
	}

	/** Document builder function for SetPreset */
	public Document setPresetDocument(String pToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element setPresetElement = doc.createElement("wsdl:SetPreset");
		body.appendChild(setPresetElement);

		Element profileToken = doc.createElement("wsdl:ProfileToken");
		profileToken.appendChild(doc.createTextNode("Profile1"));
		setPresetElement.appendChild(profileToken);

		Element presetToken = doc.createElement("wsdl:PresetToken");
		presetToken.appendChild(doc.createTextNode(pToken));
		setPresetElement.appendChild(presetToken);

		return doc;
	}

	/**
	 * Saves the current position to a preset; currently uses hardcoded
	 * MediaProfile token.
	 *
	 * Calling this will overwrite if an existing preset has a matching token.
	 *
	 * @param pToken unique reference token for the PTZ preset
	 */
	public String setPreset(String pToken) {
		Document doc = setPresetDocument(pToken);
		return sendRequestDocument(doc);
	}

	/** Document builder function for Stop */
	public Document getStopDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element stop = doc.createElement("wsdl:Stop");
		body.appendChild(stop);

		Element profileToken = doc.createElement("wsdl:ProfileToken");
		profileToken.appendChild(doc.createTextNode("Profile1"));
		stop.appendChild(profileToken);

		Element panTilt = doc.createElement("wsdl:PanTilt");
		panTilt.appendChild(doc.createTextNode("true"));
		stop.appendChild(panTilt);

		Element zoom = doc.createElement("wsdl:Zoom");
		zoom.appendChild(doc.createTextNode("true"));
		stop.appendChild(zoom);

		return doc;
	}

	/** Stops all ongoing PTZ movements */
	public String stop() {
		Document doc = getStopDocument();
		return sendRequestDocument(doc);
	}

	/**
	 * Document builder function for SendAuxiliaryCommand
	 *
	 * Relevant for wiper and other extra functions
	 */
	public Document getAuxiliaryCommandDocument(String command, String state) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagName("SOAP-ENV:Body").item(0);

		Element sendAuxiliaryCommand = doc.createElement("wsdl:SendAuxiliaryCommand");
		body.appendChild(sendAuxiliaryCommand);

		Element profileToken = doc.createElement("wsdl:ProfileToken");
		profileToken.appendChild(doc.createTextNode("Profile1"));
		sendAuxiliaryCommand.appendChild(profileToken);

		Element auxiliaryData = doc.createElement("wsdl:AuxiliaryData");
		auxiliaryData.appendChild(doc.createTextNode("tt:" + command + "|" + state));
		sendAuxiliaryCommand.appendChild(auxiliaryData);

		return doc;
	}

	/**
	 * Calls SendAuxiliaryCommand, setting the wiper value
	 *
	 * @param state the requested state ("On", "Off") of the wiper
	 */
	public String setWiper(String state) {
		authenticate = true;
		Document doc = getAuxiliaryCommandDocument("Wiper", state);
		return sendRequestDocument(doc);
	}
}
