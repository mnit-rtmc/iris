/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.g4;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Vehicle classification property.
 *
 * @author Douglas Lau
 */
public class VehClassProperty extends G4Property {

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] data = new byte[0];
		os.write(formatRequest(QualCode.CLASS_QUERY, c.getDrop(),
			data));
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		parseFrame(is, c.getDrop());
	}

	/** Parse the data from one frame.
	 * @param qual Qualifier code.
	 * @param data Data packet. */
	@Override
	protected void parseData(QualCode qual, byte[] data)
		throws IOException
	{
		switch (qual) {
		case CLASSIFICATION:
			parseClassification(data);
			break;
		default:
			super.parseData(qual, data);
		}
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] data = new byte[G4VehClass.size];
		for (int i = 0; i < data.length; i++) {
			G4VehClass vc = G4VehClass.fromOrdinal(i + 1);
			format8(data, i, getClassLen(vc));
		}
		os.write(formatRequest(QualCode.CLASSIFICATION, c.getDrop(),
			data));
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		parseFrame(is, c.getDrop());
	}

	/** Vehicle classificaiton lengths (decimeters) */
	private int[] class_len = new int[G4VehClass.size];

	/** Get the lower-bound length of a vehicle class (decimeters) */
	public int getClassLen(G4VehClass vc) {
		return class_len[vc.ordinal()];
	}

	/** Set the lower-bound length of a vehicle class (decimeters) */
	public void setClassLen(G4VehClass vc, int l) {
		class_len[vc.ordinal()] = l;
	}

	/** Parse classification data */
	private void parseClassification(byte[] data) throws ParsingException {
		if (data.length != G4VehClass.size)
			throw new ParsingException("INVALID CLASS LENGTH");
		for (int i = 0; i < data.length; i++) {
			G4VehClass vc = G4VehClass.fromOrdinal(i + 1);
			setClassLen(vc, parse8(data, i));
		}
	}

	/** Get a string representation of the vehicle classifications */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (G4VehClass vc: G4VehClass.values()) {
			if (sb.length() > 0)
				sb.append(' ');
			sb.append(vc);
			sb.append(':');
			sb.append(getClassLen(vc));
		}
		return sb.toString();
	}
}
