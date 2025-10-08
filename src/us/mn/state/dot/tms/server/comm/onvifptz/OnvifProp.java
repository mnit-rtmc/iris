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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Onvif Property.
 *
 * @author Douglas Lau
 * @author Ethan Beauclaire
 */
abstract public class OnvifProp extends ControllerProperty {

	/** Device URL */
	String url;

	/** Device controller's username and password */
	String user, pass;

	/**
	 * Command string for use in callback sendSoap()
	 *
	 * String[] is { "OpName", "param1", ... }
	 */
	String[] cmd;

	/** Logger method */
	protected void log(String s) {
		OnvifPTZPoller.slog("OnvifProp:" + s);
	}

	public void setUrl(String u) {
		url = u;
	}

	/** Override to queue ONVIF ops while previous ones on a camera finish */
	@Override
	public boolean equals(Object o) {
		if ((!(o instanceof OnvifProp)) || getClass() != o.getClass())
			return false;

		OnvifProp op = (OnvifProp) o;
		return Arrays.equals(cmd, op.cmd) && Objects.equals(url, op.url);
	}

	/** Set up poller, to be deferred by op queue until Send Settings */
	public String initialize(OnvifPTZPoller poller) throws IOException {
		if (poller == null) return "No poller specified";

		// First, get bindings:
		DeviceService dev = DeviceService.getDeviceService(url + "/onvif/device_service", user, pass);
		poller.dev = dev;
		if (poller.dev == null)
			return "initialize(): Missing Device Service for " + poller.name;


		String capabilities = dev.getCapabilities();
		poller.capabilities = capabilities;

		poller.ptz = PTZService.getPTZService(dev.getPTZBinding(capabilities), user, pass);
		poller.media = MediaService.getMediaService(dev.getMediaBinding(capabilities), user, pass);
		poller.img = ImagingService.getImagingService(dev.getImagingBinding(capabilities), user, pass);

		// Now get tokens:
		int mediaWidth = 0, videoWidth = 0;  // to find largest source
		MediaService media = poller.media;
		String mediaProfile = null, videoSource = null;

		// Should contain all necessary tokens
		String profilesString = media.getProfiles();
		if (profilesString == null) return "Error parsing ONVIF media profiles response";
		Document getProfilesRes = DOMUtils.getDocument(profilesString);
		if (getProfilesRes == null) return "Error parsing ONVIF media profiles response as XML. Response string:\n"
			+ profilesString;

		NodeList profiles = getProfilesRes.getElementsByTagNameNS("*", "Profiles");
		for (int i = 0; i < profiles.getLength(); i++) {
			int mx = 0, vx = 0;
			Element profile = (Element) profiles.item(i);

			// get the video source and its width
			Element videoConfig = (Element) profile.getElementsByTagNameNS("*", "VideoSourceConfiguration").item(0);
			if (videoConfig == null) continue;  // we want a profile with a video source
			Element sourceToken = (Element) videoConfig.getElementsByTagNameNS("*", "SourceToken").item(0);
			Element bounds = (Element) videoConfig.getElementsByTagNameNS("*", "Bounds").item(0);
			vx = Integer.parseInt(bounds.getAttribute("width"));

			// get the video encoder and its width, if applicable; only for better profile selection
			Element encoderConfig = (Element) profile.getElementsByTagNameNS("*", "VideoEncoderConfiguration").item(0);
			if (encoderConfig != null) {
				Element widthElem = (Element) encoderConfig.getElementsByTagNameNS("*", "Width").item(0);
				mx = Integer.parseInt(widthElem.getTextContent());
			}

			// if video source bigger than current, replace
			if (vx >= videoWidth) {
				videoSource = sourceToken.getTextContent();
				videoWidth = vx;
			}
			// replace media profile only if it's larger and the attached source is no smaller
			if (mx >= mediaWidth && vx >= videoWidth) {
				mediaProfile = profile.getAttribute("token");
				mediaWidth = mx;
			}
		}

		if (mediaProfile != null) {
			poller.mediaProfile = mediaProfile;
			log("Set media profile: " + mediaProfile);
		}
		if (videoSource != null) {
			poller.videoSource = videoSource;
			log("Set video source: " + videoSource);
		}
		if (mediaProfile == null || videoSource == null)
			return "Could not retrieve profile tokens. Profiles response:\n"
				+ profilesString;

		return "Successfully retrieved service bindings and profile tokens";
	}

	/** Build and send the SOAP messages */
	public String sendSoap(OnvifPTZPoller poller) throws IOException {
		// if no cmd items, nothing to do
		if (cmd == null || cmd.length == 0) {
			return "No cmd specified";
		}
		if (poller == null)
			return "No poller provided";

		// Check for tokens, and if needed for command
		String mediaProfile = poller.mediaProfile;
		String videoSource = poller.videoSource;

		boolean needMedia = (
			cmd[0].equals("ptz") ||
			cmd[0].equals("wiper") ||
			cmd[0].contains("preset")
		);
		boolean haveMedia = mediaProfile != null && !mediaProfile.trim().isEmpty();
		boolean needVideo = (
			cmd[0].contains("iris") ||
			cmd[0].contains("focus")
		);
		boolean haveVideo = videoSource != null && !videoSource.trim().isEmpty();

		if (needMedia && !haveMedia) return "Missing mediaProfile";
		if (needVideo && !haveVideo) return "Missing videoSource";

		// Check for services, if not initializing them
		if (!cmd[0].equals("initialize")) {
			if (poller.dev == null)
				return "Missing Device Service for " + poller.name;
			if (poller.ptz == null)
				return "Missing PTZ Service for " + poller.name;
			if (poller.media == null)
				return "Missing Media Service for " + poller.name;
			if (poller.img == null)
				return "Missing Imaging Service for " + poller.name;

		}

		DeviceService dev = poller.dev;
		PTZService ptz = poller.ptz;
		MediaService media = poller.media;
		ImagingService img = poller.img;

		// We either don't need services/tokens, or have them
		// So, evaluate command:
		StringBuilder sb = new StringBuilder();
		switch (cmd[0]) {
			case "ptz":
				if (cmd.length < 4) {
					sb.append("Error sending ptz message - missing pan, tilt, and/or zoom");
					break;
				}
				sb.append(ptz.continuousMove(mediaProfile, cmd[1], cmd[2], cmd[3]));
				break;
			case "storepreset":
				if (cmd.length < 2) {
					sb.append("Error storing preset - missing name");
					break;
				}
				sb.append(ptz.setPreset(mediaProfile, cmd[1]));
				break;
			case "recallpreset":
				if (cmd.length < 2) {
					sb.append("Error recalling preset - missing name");
					break;
				}
				sb.append(ptz.gotoPreset(mediaProfile, cmd[1]));
				break;
			case "movefocus":
				if (cmd.length < 2) {
					sb.append("Error moving focus - missing amount");
					break;
				}
				if (cmd[1].equals("0") || cmd[1].equals("0.0"))
					sb.append(img.stop(videoSource));
				else
					sb.append(img.moveFocus(videoSource, Float.parseFloat(cmd[1])));
				break;
			case "iris":
				if (cmd.length < 2) {
					sb.append("Error sending iris increment message - no value given");
					break;
				}
				sb.append(img.incrementIris(videoSource, cmd[1]));
				break;
			case "wiper":
				sb.append(ptz.wiperOneshot(mediaProfile));
				break;
			case "autofocus":
				if (cmd.length < 2) {
					sb.append("Error sending autofocus message - no value given");
					break;
				}
				sb.append(img.setFocus(videoSource, cmd[1]));
				break;
			case "autoiris":
				if (cmd.length < 2) {
					sb.append("Error sending auto iris message - no value given");
					break;
				}
				sb.append(img.setIris(videoSource, cmd[1]));
				break;
			case "autoirisfocus":
				if (cmd.length < 2) {
					sb.append("Error sending auto iris/focus message = no value given");
					break;
				}
				sb.append(img.setFocus(videoSource, cmd[1]));
				sb.append(img.setIris(videoSource, cmd[1]));
				break;
			case "reboot":
				sb.append(dev.systemReboot());
				break;
			case "initialize":
				sb.append(initialize(poller));
				break;
			default:
				sb.append("Unexpected cmd");
				break;
		}

		sb.append("\n");

		return sb.toString();
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os) {
		// nothing to do -- encodeStore doesn't support HTTP
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is) {
		// nothing to do -- decoded from HTTP result code
	}
}
