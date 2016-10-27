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
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * This class performs a VAPIX request for an operation with no parameters.
 *
 * @author Travis Swanston
 */
public class NullaryProperty extends AxisPTZProperty {

	/** Requested dev_req value */
	private final DeviceRequest dev_req;

	public NullaryProperty(DeviceRequest dr) {
		super();
		dev_req = dr;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		VapixCmd cmd = new VapixCmd(VapixCmd.CMD_PTZ);
		cmd.addParam(VapixCmd.PARAM_CAMERA, c.getDrop());
		switch (dev_req) {
			case CAMERA_FOCUS_MANUAL:
				cmd.addParam(VapixCmd.PARAM_AUTOFOCUS,
					VapixCmd.VALUE_MODE_OFF);
				break;
			case CAMERA_FOCUS_AUTO:
				cmd.addParam(VapixCmd.PARAM_AUTOFOCUS,
					VapixCmd.VALUE_MODE_ON);
				break;
			case CAMERA_IRIS_MANUAL:
				cmd.addParam(VapixCmd.PARAM_AUTOIRIS,
					VapixCmd.VALUE_MODE_OFF);
				break;
			case CAMERA_IRIS_AUTO:
				cmd.addParam(VapixCmd.PARAM_AUTOIRIS,
					VapixCmd.VALUE_MODE_ON);
				break;
			default:
				return;
		}
		issueRequest(c, os, cmd);
	}

	/** Get a short description of the property */
	@Override
	public String getDesc() {
		switch (dev_req) {
			case CAMERA_FOCUS_MANUAL:
				return "A/F";
			case CAMERA_FOCUS_AUTO:
				return "A/F";
			case CAMERA_IRIS_MANUAL:
				return "A/I";
			case CAMERA_IRIS_AUTO:
				return "A/I";
			default:
				return "?";
		}
	}

}

