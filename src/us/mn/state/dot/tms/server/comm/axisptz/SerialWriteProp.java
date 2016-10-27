/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2015  AHMCT, University of California
 * Copyright (C) 2016  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.utils.HexString;

/**
 * Serial port write property.
 *
 * @author Travis Swanston
 * @author Douglas Lau
 */
public class SerialWriteProp extends AxisProp {

	/** Serial write params */
	private enum Param {
		COMM_PORT	("port"),
		SERIAL_WRITE	("write");

		private Param(String c) {
			cmd = c;
		}
		public final String cmd;
	}

	/** Create a new serial write property */
	public SerialWriteProp() {
		super("serial.cgi");
	}

	/** Add a comm port param */
	public void addCommPort(int p) {
		addParam(Param.COMM_PORT.cmd, Integer.toString(p));
	}

	/** Add a data param */
	public void addData(byte[] data) {
		String hex = HexString.format(data).toLowerCase();
		addParam(Param.SERIAL_WRITE.cmd, hex);
	}
}
