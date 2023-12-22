/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2023  Minnesota Department of Transportation
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

/**
 * PTZ command property.
 *
 * @author Ethan Beauclaire
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class PTZCommandProp extends OnvifProp {

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

	/** Sets message to store preset */
	public void addStorePreset(int p) {
		log("Final URL to PTZService: " + url + service_path);
		PTZService ptz = PTZService.getPTZService(url + service_path, "admin", "admin");
		message = ptz.setPresetDocument(String.valueOf(p));
		log("Message from PTZService: " + DOMUtils.getString(message));

		// set service field for sending from OnvifProp
		service = ptz;
	}

	/** Sets message to recall preset */
	public void addRecallPreset(int p) {
		log("Final URL to PTZService: " + url + service_path);
		PTZService ptz = PTZService.getPTZService(url + service_path, "admin", "admin");
		message = ptz.gotoPresetDocument(String.valueOf(p));
		log("Message from PTZService: " + DOMUtils.getString(message));

		// set service field for sending from OnvifProp
		service = ptz;
	}

	/** Sets message to move the focus */
	public void addFocus(int f) {
		log("Final URL to ImagingService: " + url + service_path);
		ImagingService img = ImagingService.getImagingService(url + service_path, "admin", "admin");
		if (f == 0)
			message = img.getStopDocument("Visible Camera");
		else
			message = img.getMoveDocument("Visible Camera", f, "continuous");
		log("Message from ImagingService: " + DOMUtils.getString(message));

		// set service field for sending from OnvifProp
		service = img;
	}

	/** Sets callback cmd to iris */
	public void addIris(int i) {
		log("Final URL to ImagingService: " + url + service_path);
		ImagingService img = ImagingService.getImagingService(url + service_path, "admin", "admin");

		// set cmd to use in sendSoap callback
		cmd = "iris";
		val = String.valueOf(i);

		// set service field for sending from OnvifProp
		service = img;
	}

	/** Sets callback cmd to wiper oneshot */
	public void addWiperOneshot() {
		log("Final URL to PTZService: " + url + service_path);
		PTZService ptz = PTZService.getPTZService(url + service_path, "admin", "admin");

		// set cmd to use in sendSoap callback
		cmd = "wiper";

		// set service field for sending from OnvifProp
		service = ptz;
	}

	/** Sets message to auto focus */
	public void addAutoFocus(boolean on) {
		log("Final URL to ImagingService: " + url + service_path);
		ImagingService img = ImagingService.getImagingService(url + service_path, "admin", "admin");
		String f = on ? "Auto" : "Manual";
		message = img.setImagingSettingsDocument("Visible Camera", "focus", f);
		log("Message from ImagingService: " + DOMUtils.getString(message));

		// set service field for sending from OnvifProp
		service = img;
	}

	/** Sets message to auto iris */
	public void addAutoIris(boolean on) {
		log("Final URL to ImagingService: " + url + service_path);
		ImagingService img = ImagingService.getImagingService(url + service_path, "admin", "admin");
		String i = on ? "Auto" : "Manual";
		message = img.setImagingSettingsDocument("Visible Camera", "iris", i);
		log("Message from ImagingService: " + DOMUtils.getString(message));

		// set service field for sending from OnvifProp
		service = img;
	}

	/** Sets callback cmd to both iris and focus to auto */
	public void addAutoIrisAndFocus() {
		log("Final URL to ImagingService: " + url + service_path);
		ImagingService img = ImagingService.getImagingService(url + service_path, "admin", "admin");

		// set cmd to use in sendSoap callback
		cmd = "autoirisfocus";

		// set service field for sending from OnvifProp
		service = img;
	}
}
