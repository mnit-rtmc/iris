/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
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

import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;

import us.mn.state.dot.tms.server.comm.onvifptz.lib.*;

/**
 * PTZ command property.
 *
 * @author Ethan Beauclaire
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class PTZCommandProp extends OnvifProp {

	/** PTZ command params */
	private enum Param {
		PAN_TILT	("continuouspantiltmove"),
		ZOOM		("continuouszoommove"),
		RECALL_PRESET	("gotoserverpresetno"),
		FOCUS		("continuousfocusmove"),
		IRIS		("continuousirismove"),
		AUTO_FOCUS	("autofocus"),
		AUTO_IRIS	("autoiris");

		private Param(String c) {
			cmd = c;
		}
		public final String cmd;
	}

	/** Logger method */
	private void log(String s) {
		OnvifPTZPoller.slog("PTZCommandProp:" + s);
	}

	/** Create a new PTZ command property */
	public PTZCommandProp(String service) {
		switch (service) {
			case "device":
				service_path = "/onvif/device_service";
				break;
			case "media":
				service_path = ":80/onvif/media";
				break;
			case "ptz":
				service_path = ":80/onvif/ptz";
				break;
			case "imaging":
				service_path = ":80/onvif/imaging";
				break;
			default:
				service_path = "/onvif/device_service";
		}
	}

	/** Set message to PanTiltZoom SOAP message */
	public void addPanTiltZoom(float p, float t, float z) {
		log("Final URL to PTZService: " + url + service_path);
		PTZService ptz = PTZService.getPTZService(url + service_path, "admin", "admin");
		message = ptz.getContinuousMoveDocument(p, t, z);
		log("Message from PTZService: " + DOMUtils.getString(message));

		// set service field for sending from OnvifProp
		service = ptz;
	}

	/** Add a store preset param */
	public void addStorePreset(int p) {
		log("Final URL to PTZService: " + url + service_path);
		PTZService ptz = PTZService.getPTZService(url + service_path, "admin", "admin");
		message = ptz.setPresetDocument(String.valueOf(p));
		log("Message from PTZService: " + DOMUtils.getString(message));

		// set service field for sending from OnvifProp
		service = ptz;
	}

	/** Add a recall preset param */
	public void addRecallPreset(int p) {
		log("Final URL to PTZService: " + url + service_path);
		PTZService ptz = PTZService.getPTZService(url + service_path, "admin", "admin");
		message = ptz.gotoPresetDocument(String.valueOf(p));
		log("Message from PTZService: " + DOMUtils.getString(message));

		// set service field for sending from OnvifProp
		service = ptz;
	}

	//TODO: implement focus and iris commands below

	///** Add a focus param */
	//public void addFocus(int f) {
	//	
	//}

	///** Add an iris param */
	//public void addIris(int i) {
	//	log("Final URL to ImagingService: " + url + service_path);
	//	ImagingService imaging = ImagingService.getImagingService(url + service_path, "admin", "admin");
	//	message = setImagingSettingsDocument(vToken, "iris", i);
	//	log("Message from ImagingService: " + message);

	//	// set service field for sending from OnvifProp
	//	service = imaging;
	//}

	///** Add an auto-focus param */
	//public void addAutoFocus(boolean on) {
	//}

	///** Add an auto-iris param */
	//public void addAutoIris(boolean on) {
	//}
}
