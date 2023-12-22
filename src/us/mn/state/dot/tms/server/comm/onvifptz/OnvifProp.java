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
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

import org.w3c.dom.Document;

/**
 * Onvif Property.
 *
 * @author Douglas Lau
 * @author Ethan Beauclaire
 */
abstract public class OnvifProp extends ControllerProperty {

	/** Onvif Service Path */
	String service_path;

	/** Device URL */
	String url;

	/** ONVIF Service */
	Service service;

	/** SOAP Message */
	Document message;

	/** SOAP command for multi-step operation */
	String cmd;

	/** Value for multi-step operation */
	String val;

	/** Create a new Onvif property */
	protected OnvifProp() {
		service_path = "";
	}

	/** Logger method */
	protected void log(String s) {
		OnvifPTZPoller.slog("PTZCommandProp:" + s);
	}

	/** Get as a string */
	@Override
	public String toString() {
		if (message != null)
			return DOMUtils.getString(message);
		return "";
	}

	/** Get the path + query for a property */
	@Override
	public String getPathQuery() {
		return service_path;
	}

	public void setUrl(String u) {
		url = u;
	}

	/** Send the SOAP message */
	public String sendSoap() {
		if (service == null)
			return "Error sending SOAP message - null service";

		// if multi-step operation, send specified ops within Service class
		if (cmd != null) {
			switch (cmd) {
				case "iris":
					if (val == null)
						return "Error sending iris increment message - null value";
					return ((ImagingService) service).incrementIris("Visible Camera", val);
				case "wiper":
					return ((PTZService) service).wiperOneshot();
				case "autoirisfocus":
					String irisString = ((ImagingService) service).setIris("Visible Camera", "auto");
					String focusString = ((ImagingService) service).setFocus("Visible Camera", "auto");
					return irisString + focusString;
				default:
					return "Unexpected cmd";
			}
		}

		// if single-step operation, message is already set, so send it
		if (message == null)
			return "Error sending SOAP message - null/invalid cmd and message";
		return service.sendRequestDocument(message);
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
