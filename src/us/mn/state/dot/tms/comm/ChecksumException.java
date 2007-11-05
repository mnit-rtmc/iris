/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2002  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.comm;

/**
 * Thrown whenever there is a serial I/O checksum error
 *
 * @author Douglas Lau
 */
public class ChecksumException extends SerialIOException {

	/** Scanned data for debugging checksum exceptions */
	protected final String scannedData;

	/** Get the scanned data for debugging checksum exceptions */
	public String getScannedData() { return scannedData; }

	/** Create a new checksum exception */
	public ChecksumException() {
		super("SERIAL I/O CHECKSUM ERROR" );
		scannedData = "";
	}

	/** Create a new checksum exception with the specified message */
	public ChecksumException(String m) {
		super(m);
		scannedData = "";
	}

	/** Create a new checksum exception with scanned data */
	public ChecksumException(byte[] data) {
		super("SERIAL I/O CHECKSUM ERROR" );
		StringBuffer s = new StringBuffer();
		for(int i = 0; i < data.length; i++) {
			if(i > 0) s.append(':');
			s.append(Integer.toHexString(data[i] & 0xFF));
		}
		scannedData = s.toString().toUpperCase();
	}
}
