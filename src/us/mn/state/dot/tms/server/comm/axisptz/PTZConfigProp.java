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
 * PTZ config property.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class PTZConfigProp extends AxisProp {

	/** PTZ config params */
	private enum Param {
		STORE_PRESET		("setserverpresetno"),
		OSD_MENU		("osdmenu");

		private Param(String c) {
			cmd = c;
		}
		public final String cmd;
	}

	/** Create a new PTZ config property */
	public PTZConfigProp() {
		super("ptzconfig.cgi");
	}

	/** Add a store preset param */
	public void addStorePreset(int p) {
		addParam(Param.STORE_PRESET.cmd, Integer.toString(p));
	}
}
