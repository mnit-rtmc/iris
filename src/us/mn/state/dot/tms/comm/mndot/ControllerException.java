/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2005  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.mndot;

import java.io.IOException;
import us.mn.state.dot.tms.comm.ChecksumException;
import us.mn.state.dot.tms.comm.DownloadRequestException;
import us.mn.state.dot.tms.comm.ParsingException;

/**
 * ControllerException
 *
 * @author Douglas Lau
 */
public class ControllerException extends IOException {

	/** Status codes for 170 communication protocol */
	static public final int OK = 0;
	static public final int BAD_MESSAGE = 1;
	static public final int BAD_POLL_CHECKSUM = 2;
	static public final int DOWNLOAD_REQUEST = 3;
	static public final int WRITE_PROTECT = 4;
	static public final int MESSAGE_SIZE = 5;
	static public final int NO_DATA = 6;
	static public final int NO_RAM = 7;
	static public final int DOWNLOAD_REQUEST_4 = 8; // 4-bit addressing

	/** Check controller status code and throw an equivalent exception */
	static public void checkStatus(int status) throws IOException {
		switch(status) {

		case OK:
			return;
		case BAD_MESSAGE:
			throw new ControllerException(BAD_MESSAGE,
				"BAD MESSAGE");
		case BAD_POLL_CHECKSUM:
			throw new ChecksumException(
				"CONTROLLER I/O CHECKSUM ERROR");
		case DOWNLOAD_REQUEST:
		case DOWNLOAD_REQUEST_4:
			throw new DownloadRequestException("CODE: " + status);
		case WRITE_PROTECT:
			throw new ControllerException(WRITE_PROTECT,
				"WRITE PROTECT");
		case MESSAGE_SIZE:
			throw new ControllerException(MESSAGE_SIZE,
				"MESSAGE SIZE");
		case NO_DATA:
			throw new NoSampleData(NO_DATA, "NO DATA RECORDS");
		case NO_RAM:
			throw new NoSampleData(NO_RAM, "NO RAM");
		default:
			throw new ParsingException("BAD STATUS: " +
				status);
		}
	}

	/** Controller status code */
	public final int status;

	/** Create a new controller exception */
	protected ControllerException(int s, String m) {
		super(m);
		status = s;
	}

	/** Controller exception indicating sample data not available */
	static public class NoSampleData extends ControllerException {
		protected NoSampleData(int s, String m) {
			super(s, m);
		}
	}
}
