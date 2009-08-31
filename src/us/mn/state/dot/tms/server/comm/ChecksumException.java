/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

/**
 * Thrown whenever there is a checksum error on received data.
 *
 * @author Douglas Lau
 */
public class ChecksumException extends ParsingException {

	/** Format scanned data for debugging output */
	static protected String formatScannedData(byte[] data) {
		StringBuffer s = new StringBuffer();
		for(int i = 0; i < data.length; i++) {
			if(i > 0)
				s.append(':');
			s.append(Integer.toHexString(data[i] & 0xFF));
		}
		return s.toString().toUpperCase();
	}

	/** Create a new checksum exception */
	public ChecksumException() {
		super("CHECKSUM ERROR");
	}

	/** Create a new checksum exception with the specified message */
	public ChecksumException(String m) {
		super("CHECKSUM ERROR: " + m);
	}

	/** Create a new checksum exception with scanned data */
	public ChecksumException(byte[] data) {
		super("CHECKSUM ERROR: " + formatScannedData(data));
	}
}
