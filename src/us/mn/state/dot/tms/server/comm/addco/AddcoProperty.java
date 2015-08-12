/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.addco;

import java.io.InputStream;
import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Addco property.
 *
 * @author Douglas Lau
 */
abstract public class AddcoProperty extends ControllerProperty {

	/** "Any" Address (not multi-drop) */
	static protected final int ADDR_ANY = -1;

	/** Decode header of response */
	protected int decodeHead(InputStream is, MsgCode mc) throws IOException{
		byte[] bc = recvResponse(is, 1);
		if (bc[0] != mc.code)
			throw new ParsingException("MSG CODE: " + bc[0]);
		if (mc == MsgCode.NORMAL) {
			byte[] len = recvResponse(is, 2);
			return parse16le(len, 0);
		} else
			return 0;
	}

	/** Decode body of response */
	protected byte[] decodeBody(InputStream is, int len) throws IOException{
		byte[] body = recvResponse(is, len - 5); // - header / fcs
		int addr = parse16le(body, 0);
		if (addr != ADDR_ANY)
			throw new ParsingException("ADDRESS: " + addr);
		return body;
	}
}
