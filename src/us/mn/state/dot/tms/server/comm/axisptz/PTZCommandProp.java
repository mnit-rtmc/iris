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
package us.mn.state.dot.tms.server.comm.axisptz;

/**
 * PTZ command property.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class PTZCommandProp extends AxisProp {

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

	/** Map a [-1.0,1.0] float value to an [-100,100] integer value. */
	static private String mapPTZ(float v) {
		int mapped = Math.round(v * 100);
		// sanity:
		if (mapped < -100)
			mapped = -100;
		if (mapped > 100)
			mapped =  100;
		return Integer.toString(mapped);
	}

	/** Create a new PTZ command property */
	public PTZCommandProp() {
		super("ptz.cgi");
	}

	/** Add a pan/tilt param */
	public void addPanTilt(float p, float t) {
		addParam(Param.PAN_TILT.cmd, mapPTZ(p) + ',' + mapPTZ(t));
	}

	/** Add a zoom param */
	public void addZoom(float z) {
		addParam(Param.ZOOM.cmd, mapPTZ(z));
	}

	/** Add a recall preset param */
	public void addRecallPreset(int p) {
		addParam(Param.RECALL_PRESET.cmd, Integer.toString(p));
	}

	/** Add a focus param */
	public void addFocus(int f) {
		addParam(Param.FOCUS.cmd, mapPTZ(f));
	}

	/** Add an iris param */
	public void addIris(int i) {
		addParam(Param.IRIS.cmd, mapPTZ(i));
	}

	/** Add an auto-focus param */
	public void addAutoFocus(boolean on) {
		addParam(Param.AUTO_FOCUS.cmd, on ? "on" : "off");
	}

	/** Add an auto-iris param */
	public void addAutoIris(boolean on) {
		addParam(Param.AUTO_IRIS.cmd, on ? "on" : "off");
	}
}
