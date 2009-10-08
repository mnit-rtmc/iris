/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.smartsensor;

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * A Classification Request contains vehicle classification length thresholds.
 *
 * @author Douglas Lau
 */
public class ClassificationRequest extends MemoryRequest {

	/** Default minimum length for SHORT vehicle classification */
	static protected final int SHORT_MIN = 0;

	/** Default maximum length for SHORT vehicle classification */
	static protected final int SHORT_MAX = 21;

	/** Default minimum length for MEDIUM vehicle classification */
	static protected final int MEDIUM_MIN = SHORT_MAX;

	/** Default maximum length for MEDIUM vehicle classification */
	static protected final int MEDIUM_MAX = 35;

	/** Default minimum length for LONG vehicle classification */
	static protected final int LONG_MIN = MEDIUM_MAX;

	/** Default maximum length for LONG vehicle classification */
	static protected final int LONG_MAX = 328;

	/** Minimum length for SHORT vehicle classification */
	int short_min = SHORT_MIN;

	/** Maximum length for SHORT vehicle classification */
	int short_max = SHORT_MAX;

	/** Minimum length for MEDIUM vehicle classification */
	int medium_min = MEDIUM_MIN;

	/** Maximum length for MEDIUM vehicle classification */
	int medium_max = MEDIUM_MAX;

	/** Minimum length for LONG vehicle classification */
	int long_min = LONG_MIN;

	/** Maximum length for LONG vehicle classification */
	int long_max = LONG_MAX;

	/** Get the SmartSensor memory buffer address */
	protected int memoryAddress() {
		return 0x020000;
	}

	/** Get the SmartSensor memory buffer length */
	protected short memoryLength() {
		return 40;
	}

	/** Format the buffer to write to SmartSensor memory */
	protected String formatBuffer() {
		return hex(short_min, 4) + hex(short_max, 4) + hex(0, 8) +
			hex(medium_min, 4) + hex(medium_max, 4) + hex(0, 8) +
			hex(long_min, 4) + hex(long_max, 4);
	}

	/** Set the response to the request */
	protected void setResponse(String r) throws IOException {
		super.setResponse(r);
		try {
			short_min = Integer.parseInt(r.substring(0, 4), 16);
			short_max = Integer.parseInt(r.substring(4, 8), 16);
			medium_min = Integer.parseInt(r.substring(16, 20), 16);
			medium_max = Integer.parseInt(r.substring(20, 24), 16);
			long_min = Integer.parseInt(r.substring(32, 36), 16);
			long_max = Integer.parseInt(r.substring(36, 40), 16);
		}
		catch(NumberFormatException e) {
			throw new ParsingException(
				"Invalid classification lengths: " + r);
		}
	}

	/** Is the classification set to the default values? */
	public boolean isDefault() {
		return short_min == SHORT_MIN && short_max == SHORT_MAX &&
			medium_min == MEDIUM_MIN && medium_max == MEDIUM_MAX &&
			long_min == LONG_MIN && long_max == LONG_MAX;
	}

	/** Get a string representation of the request */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Classification: ");
		sb.append(short_min);
		sb.append('-');
		sb.append(short_max);
		sb.append(',');
		sb.append(medium_min);
		sb.append('-');
		sb.append(medium_max);
		sb.append(',');
		sb.append(long_min);
		sb.append('-');
		sb.append(long_max);
		return sb.toString();
	}
}
