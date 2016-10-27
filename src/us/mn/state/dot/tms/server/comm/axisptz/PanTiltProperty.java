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
 * This class performs a VAPIX request for a continuous pan/tilt movement.
 *
 * @author Travis Swanston
 */
public class PanTiltProperty extends AxisPTZProperty {

	/** Requested pan value */
	private final int pan;

	/** Requested tilt value */
	private final int tilt;

	public PanTiltProperty(int p, int t) {
		super();
		pan = p;
		tilt = t;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		VapixCmd cmd = new VapixCmd(VapixCmd.CMD_PTZ);
		cmd.addParam(VapixCmd.PARAM_CAMERA, c.getDrop());
		cmd.addParam(VapixCmd.PARAM_PANTILT,
			"" + pan + "," + tilt);
		issueRequest(c, os, cmd);
	}

	/** Get a short description of the property */
	@Override
	public String getDesc() {
		return "P/T";
	}

}

