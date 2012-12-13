/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Version Property.
 *
 * @author Douglas Lau
 */
public class VersionProperty extends SS125Property {

	/** Message ID for version request */
	protected MessageID msgId() {
		return MessageID.VERSION;
	}

	/** Format a QUERY request */
	@Override protected byte[] formatQuery() throws IOException {
		byte[] body = new byte[4];
		formatBody(body, MessageType.READ);
		return body;
	}

	/** Parse a QUERY response */
	@Override protected void parseQuery(byte[] rbody) throws IOException {
		if(rbody.length != 20)
			throw new ParsingException("BODY LENGTH");
		digital = parse32(rbody, 3);
		algorithm = parse32(rbody, 7);
		fpga = parse32(rbody, 11);
		fpaa = parse32(rbody, 15);
	}

	/** Firmware version (digital) */
	protected int digital;

	/** Get the firmware version (digital) */
	public int getDigital() {
		return digital;
	}

	/** Algorithm version */
	protected int algorithm;

	/** Get the algorithm version */
	public int getAlgorithm() {
		return algorithm;
	}

	/** FPGA version */
	protected int fpga;

	/** Get the FPGA version */
	public int getFpga() {
		return fpga;
	}

	/** FPAA version */
	protected int fpaa;

	/** Get the FPAA version */
	public int getFpaa() {
		return fpaa;
	}

	/** Get the DSP firmware version */
	public String getVersion() {
		return Integer.toHexString(getDigital());
	}

	/** Get a string representation of the property */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("digital:");
		sb.append(Integer.toHexString(getDigital()));
		sb.append(" algorithm:");
		sb.append(Integer.toHexString(getAlgorithm()));
		sb.append(" fpga:");
		sb.append(Integer.toHexString(getFpga()));
		sb.append(" fpaa:");
		sb.append(Integer.toHexString(getFpaa()));
		return sb.toString();
	}
}
