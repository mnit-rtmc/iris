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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Service for ONVIF PTZ messages
 *
 * @author Ethan Beauclaire
 */
public class PTZService extends Service {
	public PTZService(String ptzServiceAddress, String u, String p) {
		endpoint = ptzServiceAddress;
		WSDL = "http://www.onvif.org/ver20/ptz/wsdl";
		username = u;
		password = p;
	}

	public static PTZService getPTZService(String ptzServiceAddress, String u, String p) {
		if (ptzServiceAddress == null || ptzServiceAddress.isEmpty())
			return null;
		return new PTZService(ptzServiceAddress, u, p);
	}

	/** Document builder function for GetConfigurations */
	public Document getConfigurationsDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagNameNS(SOAP, "Body").item(0);

		Element elem = doc.createElementNS(WSDL, "wsdl:GetConfigurations");
		body.appendChild(elem);

		return doc;
	}

	/** Gets the list of existing PTZ configurations and their constraints on the device */
	public String getConfigurations() throws IOException {
		Document doc = getConfigurationsDocument();
		return sendRequestDocument(doc);
	}

	/** Document builder function for GetConfigurationOptions */
	public Document getConfigurationOptionsDocument(String cToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagNameNS(SOAP, "Body").item(0);

		Element configOptions = doc.createElementNS(WSDL, "wsdl:GetConfigurationOptions");
		body.appendChild(configOptions);

		Element configToken = doc.createElementNS(WSDL, "wsdl:ConfigurationToken");
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
	public String getConfigurationOptions(String cToken) throws IOException {
		Document doc = getConfigurationOptionsDocument(cToken);
		return sendRequestDocument(doc);
	}

	/** Document builder function for GetNodes */
	public Document getNodesDocument() {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagNameNS(SOAP, "Body").item(0);

		Element getNodes = doc.createElementNS(WSDL, "wsdl:GetNodes");
		body.appendChild(getNodes);

		return doc;
	}

	/** Gets the descriptions of available PTZ nodes */
	public String getNodes() throws IOException {
		Document doc = getNodesDocument();
		return sendRequestDocument(doc);
	}

	/** Document builder function for GetNode */
	public Document getNodeDocument(String nToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagNameNS(SOAP, "Body").item(0);

		Element getNode = doc.createElementNS(WSDL, "wsdl:GetNode");
		body.appendChild(getNode);

		Element nodeToken = doc.createElementNS(WSDL, "wsdl:NodeToken");
		nodeToken.appendChild(doc.createTextNode(nToken));
		getNode.appendChild(nodeToken);

		return doc;
	}

	/**
	 * Gets information about a specific PTZ node
	 *
	 * @param nToken reference token to the relevant PTZ node
	 */
	public String getNode(String nToken) throws IOException {
		Document doc = getNodeDocument(nToken);
		return sendRequestDocument(doc);
	}

	/** Document builder function for ContinuousMove */
	public Document getContinuousMoveDocument(String profile, String xVel, String yVel, String zVel) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagNameNS(SOAP, "Body").item(0);

		Element continuousMove = doc.createElementNS(WSDL, "wsdl:ContinuousMove");
		body.appendChild(continuousMove);

		Element profileToken = doc.createElementNS(WSDL, "wsdl:ProfileToken");
		profileToken.appendChild(doc.createTextNode(profile));
		continuousMove.appendChild(profileToken);

		Element velocity = doc.createElementNS(WSDL, "wsdl:Velocity");
		continuousMove.appendChild(velocity);

		Element panTilt = doc.createElementNS(TT, "tt:PanTilt");
		panTilt.setAttribute("x", xVel);
		panTilt.setAttribute("y", yVel);
		panTilt.setAttribute("space", "http://www.onvif.org/ver10/tptz/PanTiltSpaces/VelocityGenericSpace");
		velocity.appendChild(panTilt);

		Element zoom = doc.createElementNS(TT, "tt:Zoom");
		zoom.setAttribute("x", zVel);
		zoom.setAttribute("space", "http://www.onvif.org/ver10/tptz/ZoomSpaces/VelocityGenericSpace");
		velocity.appendChild(zoom);

		return doc;
	}

	/**
	 * Sends a continuous move operation to the PTZ node; currently uses hard-coded MediaProfile token
	 *
	 * @param profile the profile to send the operation to
	 * @param x       the x value (pan speed) of the move [-1.0, 1.0]
	 * @param y       the y value (tilt speed) of the move [-1.0, 1.0]
	 * @param z       the zoom speed of the move [-1.0, 1.0]
	 */
	public String continuousMove(String profile, String x, String y, String z)
		throws IOException
	{
		Document doc = getContinuousMoveDocument(profile, x, y, z);
		return sendRequestDocument(doc);
	}

	/** Document builder function for RelativeMove */
	public Document getRelativeMoveDocument(String profile, float x, float y, float z) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagNameNS(SOAP, "Body").item(0);

		Element relativeMove = doc.createElementNS(WSDL, "wsdl:RelativeMove");
		body.appendChild(relativeMove);

		Element profileToken = doc.createElementNS(WSDL, "wsdl:ProfileToken");
		profileToken.appendChild(doc.createTextNode(profile));
		relativeMove.appendChild(profileToken);

		Element translation = doc.createElementNS(WSDL, "wsdl:Translation");
		relativeMove.appendChild(translation);

		Element panTilt = doc.createElementNS(TT, "tt:PanTilt");
		panTilt.setAttribute("x", String.valueOf(x));
		panTilt.setAttribute("y", String.valueOf(y));
		panTilt.setAttribute("space", "http://www.onvif.org/ver10/tptz/PanTiltSpaces/VelocityGenericSpace");
		translation.appendChild(panTilt);

		Element zoom = doc.createElementNS(TT, "tt:Zoom");
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
	public String relativeMove(String profile, float x, float y, float z)
		throws IOException
	{
		Document doc = getRelativeMoveDocument(profile, x, y, z);
		return sendRequestDocument(doc);
	}

	/** Document builder function for GetPresets */
	public Document getPresetsDocument(String profile) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagNameNS(SOAP, "Body").item(0);

		Element getPresetsElement = doc.createElementNS(WSDL, "wsdl:GetPresets");
		body.appendChild(getPresetsElement);

		Element profileToken = doc.createElementNS(WSDL, "wsdl:ProfileToken");
		profileToken.appendChild(doc.createTextNode(profile));
		getPresetsElement.appendChild(profileToken);

		return doc;
	}

	/**
	 * Gets the list of PTZ presets for the PTZNode of the selected
	 * MediaProfile; currently uses hardcoded profile token.
	 */
	public String getPresets(String profile) throws IOException {
		Document doc = getPresetsDocument(profile);
		return sendRequestDocument(doc);
	}

	/** Document builder function for GotoPreset */
	public Document gotoPresetDocument(String profToken, String pToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagNameNS(SOAP, "Body").item(0);

		Element gotoPresetElement = doc.createElementNS(WSDL, "wsdl:GotoPreset");
		body.appendChild(gotoPresetElement);

		Element profileToken = doc.createElementNS(WSDL, "wsdl:ProfileToken");
		profileToken.appendChild(doc.createTextNode(profToken));
		gotoPresetElement.appendChild(profileToken);

		Element presetToken = doc.createElementNS(WSDL, "wsdl:PresetToken");
		presetToken.appendChild(doc.createTextNode(pToken));
		gotoPresetElement.appendChild(presetToken);

		return doc;
	}

	/**
	 * Checks if a PTZ preset exists
	 * @param profileToken reference to the media profile
	 * @param pToken       reference token to the saved PTZ preset
	 */
	public boolean presetExists(String profileToken, String pToken)
		throws IOException
	{
		Document presets = DOMUtils.getDocument(getPresets(profileToken));
		if (presets == null) {
			log("Error parsing presets response");
			return false;
		}

		NodeList presetList = presets.getElementsByTagNameNS("*", "Preset");
		for (int i = 0; i < presetList.getLength(); i++) {
			if (pToken.equals(((Element) presetList.item(i)).getAttribute("token")))
				return true;
		}
		return false;
	}

	/**
	 * Returns the token of the first preset with a given name, or null
	 * @param profileToken reference to the media profile
	 * @param pName        name of the saved PTZ preset
	 */
	public String getPresetToken(String profileToken, String pName)
		throws IOException
	{
		Document presets = DOMUtils.getDocument(getPresets(profileToken));
		if (presets == null) {
			log("Error parsing presets response");
			return null;
		}

		NodeList presetList = presets.getElementsByTagNameNS("*", "Preset");

		// Return token from first element with a matching preset Name
		for (int i = 0; i < presetList.getLength(); i++) {
			Node name = ((Element) presetList.item(i)).getElementsByTagNameNS("*", "Name").item(0);
			String n;
			if (name == null || (n = name.getTextContent()) == null) continue;
			if (n.equals(pName))
				return ((Element) presetList.item(i)).getAttribute("token");
		}

		// No preset with given name; null token in SetPreset indicates new preset
		return null;
	}

	/**
	 * Point the camera in a saved preset direction for the PTZNode of
	 * selected MediaProfile
	 *
	 * @param profileToken reference to the media profile
	 * @param pName        name of the saved PTZ preset
	 */
	public String gotoPreset(String profileToken, String pName)
		throws IOException
	{
		String pToken = getPresetToken(profileToken, pName);
		if (pToken == null)
			return null;

		Document doc = gotoPresetDocument(profileToken, pToken);
		return sendRequestDocument(doc);
	}

	/** Document builder function for RemovePreset */
	public Document removePresetDocument(String profToken, String pToken) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagNameNS(SOAP, "Body").item(0);

		Element removePresetElement = doc.createElementNS(WSDL, "wsdl:RemovePreset");
		body.appendChild(removePresetElement);

		Element profileToken = doc.createElementNS(WSDL, "wsdl:ProfileToken");
		profileToken.appendChild(doc.createTextNode(profToken));
		removePresetElement.appendChild(profileToken);

		Element presetToken = doc.createElementNS(WSDL, "wsdl:PresetToken");
		presetToken.appendChild(doc.createTextNode(pToken));
		removePresetElement.appendChild(presetToken);

		return doc;
	}

	/**
	 * Removes the preset designated by pToken
	 *
	 * @param profileToken reference to the media profile
	 * @param pToken       unique reference token for the PTZ preset
	 */
	public String removePreset(String profileToken, String pToken)
		throws IOException
	{
		Document doc = removePresetDocument(profileToken, pToken);
		return sendRequestDocument(doc);
	}

	/** Document builder function for SetPreset */
	public Document setPresetDocument(String profToken, String pToken, String pName) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagNameNS(SOAP, "Body").item(0);

		Element setPresetElement = doc.createElementNS(WSDL, "wsdl:SetPreset");
		body.appendChild(setPresetElement);

		Element profileToken = doc.createElementNS(WSDL, "wsdl:ProfileToken");
		profileToken.appendChild(doc.createTextNode(profToken));
		setPresetElement.appendChild(profileToken);

		// No token if creating new preset
		if (pToken != null) {
			Element presetToken = doc.createElementNS(WSDL, "wsdl:PresetToken");
			presetToken.appendChild(doc.createTextNode(pToken));
			setPresetElement.appendChild(presetToken);
		}

		Element presetName = doc.createElementNS(WSDL, "wsdl:PresetName");
		presetName.appendChild(doc.createTextNode(pName));
		setPresetElement.appendChild(presetName);

		return doc;
	}

	/**
	 * Saves the current position to a preset
	 *
	 * Calling this will overwrite existing preset with matching name
	 *
	 * @param profileToken reference to the media profile
	 * @param pName        name for PTZ preset
	 */
	public String setPreset(String profileToken, String pName)
		throws IOException
	{
		// pToken will be null if no matching pName; indicates new preset
		// existing pToken indicates overwrite
		String pToken = getPresetToken(profileToken, pName);

		Document doc = setPresetDocument(profileToken, pToken, pName);
		return sendRequestDocument(doc);
	}

	/** Document builder function for Stop */
	public Document getStopDocument(String profile) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagNameNS(SOAP, "Body").item(0);

		Element stop = doc.createElementNS(WSDL, "wsdl:Stop");
		body.appendChild(stop);

		Element profileToken = doc.createElementNS(WSDL, "wsdl:ProfileToken");
		profileToken.appendChild(doc.createTextNode(profile));
		stop.appendChild(profileToken);

		Element panTilt = doc.createElementNS(WSDL, "wsdl:PanTilt");
		panTilt.appendChild(doc.createTextNode("true"));
		stop.appendChild(panTilt);

		Element zoom = doc.createElementNS(WSDL, "wsdl:Zoom");
		zoom.appendChild(doc.createTextNode("true"));
		stop.appendChild(zoom);

		return doc;
	}

	/** Stops all ongoing PTZ movements */
	public String stop(String profile) throws IOException {
		Document doc = getStopDocument(profile);
		return sendRequestDocument(doc);
	}

	/**
	 * Document builder function for SendAuxiliaryCommand
	 *
	 * Relevant for wiper and other extra functions
	 */
	public Document getAuxiliaryCommandDocument(String profToken, String command, String state) {
		Document doc = getBaseDocument();
		Element body = (Element) doc.getElementsByTagNameNS(SOAP, "Body").item(0);

		Element sendAuxiliaryCommand = doc.createElementNS(WSDL, "wsdl:SendAuxiliaryCommand");
		body.appendChild(sendAuxiliaryCommand);

		Element profileToken = doc.createElementNS(WSDL, "wsdl:ProfileToken");
		profileToken.appendChild(doc.createTextNode(profToken));
		sendAuxiliaryCommand.appendChild(profileToken);

		Element auxiliaryData = doc.createElementNS(WSDL, "wsdl:AuxiliaryData");
		auxiliaryData.appendChild(doc.createTextNode("tt:" + command + "|" + state));
		sendAuxiliaryCommand.appendChild(auxiliaryData);

		return doc;
	}

	/**
	 * Calls SendAuxiliaryCommand, setting the wiper value
	 *
	 * @param profileToken reference to the media profile
	 * @param state        the requested state ("On", "Off") of the wiper
	 */
	public String setWiper(String profileToken, String state)
		throws IOException
	{
		Document doc = getAuxiliaryCommandDocument(profileToken, "Wiper", state);
		return sendRequestDocument(doc);
	}

	/**
	 * Sets the wiper on and off immediately to emulate a single wiper action
	 */
	public String wiperOneshot(String profileToken) throws IOException {
		String onResponse = setWiper(profileToken, "On");
		String offResponse = setWiper(profileToken, "Off");
		return onResponse + offResponse;
	}
}
