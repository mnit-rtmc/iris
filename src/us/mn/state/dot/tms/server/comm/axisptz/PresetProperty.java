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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * This class performs a VAPIX request for a PTZ preset recall or store.
 *
 * @author Travis Swanston
 */
public class PresetProperty extends AxisPTZProperty {

	/** Store flag */
	private final boolean store;

	/** Preset number */
	private final int preset;

	public PresetProperty(boolean s, int p) {
		super();
		store = s;
		preset = p;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		VapixCmd cmd;
		if (store) {
			cmd = new VapixCmd(VapixCmd.CMD_PTZCONFIG);
			cmd.addParam(VapixCmd.PARAM_STORE_SOFT_PRESET, preset);
		}
		else {
			cmd = new VapixCmd(VapixCmd.CMD_PTZ);
			cmd.addParam(VapixCmd.PARAM_RECALL_SOFT_PRESET,
				preset);
		}
		cmd.addParam(VapixCmd.PARAM_CAMERA, c.getDrop());
		issueRequest(c, os, cmd);
	}

	/** Get a short description of the property */
	@Override
	public String getDesc() {
		return "preset";
	}

}

