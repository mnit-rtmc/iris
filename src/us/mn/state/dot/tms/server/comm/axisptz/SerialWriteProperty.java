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
import us.mn.state.dot.tms.utils.HexString;

/**
 * This class performs a VAPIX request to write to a serial port.
 *
 * @author Travis Swanston
 */
public class SerialWriteProperty extends AxisPTZProperty {

	/** The serial port number */
	private final int port;

	/** The data to write */
	private final byte[] data;

	public SerialWriteProperty(int p, byte[] d) {
		super();
		port = p;
		data = d;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		VapixCmd cmd = new VapixCmd(VapixCmd.CMD_SERIAL);
		cmd.addParam(VapixCmd.PARAM_COM_PORT, port);
		String byteString = HexString.format(data).toLowerCase();
		cmd.addParam(VapixCmd.PARAM_SERIAL_WRITE, byteString);
		issueRequest(c, os, cmd);
	}

	/** Get a short description of the property */
	@Override
	public String getDesc() {
		return "serial";
	}

}

