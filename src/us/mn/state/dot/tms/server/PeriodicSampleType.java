/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.nio.ByteBuffer;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;

/**
 * Periodic sample data type.
 *
 * @author Douglas Lau
 */
public enum PeriodicSampleType {

	/** Vehicle count */
	VEH_COUNT("v", 1, Aggregation.SUM),

	/** Scan count (60 Hz scans) */
	SCAN("c", 2, Aggregation.SUM),

	/** Speed (mph) */
	SPEED("s", 1, Aggregation.AVERAGE),

	/** Motorcycle count (vehicles of MOTORCYCLE class) */
	MOTORCYCLE("vmc", 1, Aggregation.SUM),

	/** Short count (vehicles of SHORT class) */
	SHORT("vs", 1, Aggregation.SUM),

	/** Medium count (vehicles of MEDIUM class) */
	MEDIUM("vm", 1, Aggregation.SUM),

	/** Long count (vehicles of LONG class) */
	LONG("vl", 1, Aggregation.SUM),

	/** Precipitation rate (um; micrometers) */
	PRECIP_RATE("pr", 2, Aggregation.SUM),

	/** Precipitation type (rain, snow, etc.) */
	PRECIP_TYPE("pt", 1, Aggregation.NONE);

	/** Maximum bytes to store any sample type */
	static public final int MAX_BYTES = 2;

	/** Base of file extension for archiving samples */
	public final String extension;

	/** Number of bytes per sample */
	public final int sample_bytes;

	/** Sample data aggregation method (SUM or AVERAGE) */
	public final Aggregation aggregation;

	/** Create a new periodic sample type */
	private PeriodicSampleType(String e, int b, Aggregation a) {
		assert b <= MAX_BYTES;
		extension = e;
		sample_bytes = b;
		aggregation = a;
	}

	/** Put a sample value into a buffer.
	 * @param buffer Byte buffer.
	 * @param value Sample value. */
	public void putValue(ByteBuffer buffer, int value) {
		if (sample_bytes == 1) {
			int v = Math.min(Math.max(value, Byte.MIN_VALUE),
			                 Byte.MAX_VALUE);
			buffer.put((byte) v);
		}
		else if (sample_bytes == 2) {
			int v = Math.min(Math.max(value, Short.MIN_VALUE),
			                 Short.MAX_VALUE);
			buffer.putShort((short) v);
		}
	}

	/** Is a periodic sample valid? */
	public boolean isValid(PeriodicSample ps) {
		return ps.period > 0 &&
		       ps.value > MISSING_DATA;
	}
}
