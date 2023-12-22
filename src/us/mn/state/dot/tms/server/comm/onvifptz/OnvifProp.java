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
package us.mn.state.dot.tms.server.comm.onvifptz;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

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
	 * List of commands for multi-step operation
	 *
	 * String[] is { "OpName", "param1", ... }
	 */
	List<String[]> cmds;

	/** Create a new Onvif property */
	protected OnvifProp() {
		cmds = new ArrayList<String[]>();
	}

	/** Logger method */
	protected void log(String s) {
		OnvifPTZPoller.slog("PTZCommandProp:" + s);
	}

	public void setUrl(String u) {
		url = u;
	}

	/** Build and send the SOAP messages */
	public String sendSoap() {
		// if no cmd items, nothing to do
		if (cmds.size() < 1) {
			return "No cmd specified";
		}

		// create each service
		// TODO: generalize service paths? (device service specified by ONVIF standard)
		DeviceService dev = DeviceService.getDeviceService(url + "/onvif/device_service", user, pass);
		PTZService ptz = PTZService.getPTZService(url + ":80/onvif/ptz", user, pass);
		MediaService media = MediaService.getMediaService(url + ":80/onvif/media", user, pass);
		ImagingService img = ImagingService.getImagingService(url + ":80/onvif/imaging", user, pass);

		String mediaProfile = null, videoSource = null;
		int mediaWidth = 0, videoWidth = 0;  // to find maximum values

		// Should contain all necessary tokens
		Document getProfilesRes = DOMUtils.getDocument(media.getProfiles());

		if (getProfilesRes != null) {
			NodeList profiles = getProfilesRes.getElementsByTagName("trt:Profiles");
			for (int i = 0; i < profiles.getLength(); i++) {
				int mx = 0, vx = 0;
				Element profile = (Element) profiles.item(i);

				// get the video source and its width
				Element videoConfig = (Element) profile.getElementsByTagName("tt:VideoSourceConfiguration").item(0);
				if (videoConfig == null) continue;  // we want a profile with a video source
				Element sourceToken = (Element) videoConfig.getElementsByTagName("tt:SourceToken").item(0);
				Element bounds = (Element) videoConfig.getElementsByTagName("tt:Bounds").item(0);
				vx = Integer.parseInt(bounds.getAttribute("width"));

				// get the video encoder and its width, if applicable; only for better profile selection
				Element encoderConfig = (Element) profile.getElementsByTagName("tt:VideoEncoderConfiguration").item(0);
				if (encoderConfig != null) {
					Element widthElem = (Element) encoderConfig.getElementsByTagName("tt:Width").item(0);
					mx = Integer.parseInt(widthElem.getTextContent());
				}

				// if video source bigger than current, replace
				if (vx >= videoWidth) {
					log("Video width larger. Setting videoSource...");
					videoSource = sourceToken.getTextContent();
					videoWidth = vx;
				}
				// replace media profile only if it's larger and the attached source is no smaller
				if (mx >= mediaWidth && vx >= videoWidth) {
					log("Both widths larger. Setting mediaProfile...");
					mediaProfile = profile.getAttribute("token");
					mediaWidth = mx;
				}
			}
		}

		if (mediaProfile != null)
			log("Set media profile: " + mediaProfile);
		if (videoSource != null)
			log("Set video source: " + videoSource);

		// if multi-step operations, send each operation
		StringBuilder sb = new StringBuilder();
		for (String[] c : cmds) {
			switch (c[0]) {
				case "ptz":
					if (c.length < 4) {
						sb.append("Error sending ptz message - missing pan, tilt, and/or zoom");
						break;
					}
					sb.append(ptz.continuousMove(mediaProfile, Float.parseFloat(c[1]), Float.parseFloat(c[2]), Float.parseFloat(c[3])));
					break;
				case "storepreset":
					if (c.length < 2) {
						sb.append("Error storing preset - missing name");
						break;
					}
					sb.append(ptz.setPreset(mediaProfile, c[1]));
					break;
				case "recallpreset":
					if (c.length < 2) {
						sb.append("Error recalling preset - missing name");
						break;
					}
					sb.append(ptz.gotoPreset(mediaProfile, c[1]));
					break;
				case "movefocus":
					if (c.length < 2) {
						sb.append("Error moving focus - missing amount");
						break;
					}
					if (c[1].equals("0") || c[1].equals("0.0"))
						sb.append(img.stop(videoSource));
					else
						sb.append(img.moveFocus(videoSource, Float.parseFloat(c[1])));
					break;
				case "iris":
					if (c.length < 2) {
						sb.append("Error sending iris increment message - no value given");
						break;
					}
					sb.append(img.incrementIris(videoSource, c[1]));
					break;
				case "wiper":
					sb.append(ptz.wiperOneshot(mediaProfile));
					break;
				case "autofocus":
					if (c.length < 2) {
						sb.append("Error sending autofocus message - no value given");
						break;
					}
					sb.append(img.setFocus(videoSource, c[1]));
					break;
				case "autoiris":
					if (c.length < 2) {
						sb.append("Error sending auto iris message - no value given");
						break;
					}
					sb.append(img.setIris(videoSource, c[1]));
					break;
				case "reboot":
					sb.append(dev.systemReboot());
					break;
				default:
					sb.append("Unexpected cmd");
					break;
			}

			sb.append("\n");
		}

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
