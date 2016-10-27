/*
 * IRIS -- Intelligent Roadway Information System
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
package us.mn.state.dot.tms.server.comm.axisptz;

import java.util.HashMap;
import java.util.Map;

/**
 * VAPIX PTZ Command
 *
 * @author Travis Swanston
 */
public class VapixCmd {

	// VAPIX commands
	static public final String CMD_PTZ = "/axis-cgi/com/ptz.cgi";
	static public final String CMD_PTZCONFIG
		= "/axis-cgi/com/ptzconfig.cgi";
	static public final String CMD_SERIAL = "/axis-cgi/com/serial.cgi";

	// VAPIX parameters (general)
	static public final String PARAM_CAMERA = "camera";

	// VAPIX parameters for CMD_PTZ
	static public final String PARAM_PANTILT = "continuouspantiltmove";
	static public final String PARAM_ZOOM = "continuouszoommove";
	static public final String PARAM_FOCUS = "continuousfocusmove";
	static public final String PARAM_IRIS = "continuousirismove";
	static public final String PARAM_AUTOFOCUS = "autofocus";
	static public final String PARAM_AUTOIRIS = "autoiris";
	static public final String PARAM_RECALL_SOFT_PRESET
		= "gotoserverpresetno";

	// VAPIX parameters for CMD_PTZCONFIG
	static public final String PARAM_STORE_SOFT_PRESET
		= "setserverpresetno";
	static public final String PARAM_OSD_MENU = "osdmenu";

	// VAPIX parameters for CMD_SERIAL
	static public final String PARAM_COM_PORT = "port";
	static public final String PARAM_SERIAL_WRITE = "write";

	static public final String VALUE_MODE_ON = "on";
	static public final String VALUE_MODE_OFF = "off";

	private final String command;
	private final HashMap<String, String> param_map;


	/**
	 * Create a VAPIX PTZ command.
	 *
	 * @param c The VAPIX base command
	 */
	public VapixCmd(String c) {
		command = c;
		param_map = new HashMap<String, String>();
	}

	public String getCommand() {
		return command;
	}

	public Map<String, String> getParams() {
		return (HashMap<String, String>)param_map.clone();
	}

	public void addParam(String p, String v) {
		if (p == null)
			return;
		param_map.put(p, ((v != null) ? v : "")) ;
	}

	public void addParam(String p, int v) {
		addParam(p, Integer.valueOf(v).toString());
	}

}

