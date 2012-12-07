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
package us.mn.state.dot.tms.server;

import java.nio.ByteBuffer;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;

/**
 * Periodic sample data type.
 *
 * @author Douglas Lau
 */
public enum PeriodicSampleType {

	/** Volume (vehicle count) */
	VOLUME("v", 1, Byte.MAX_VALUE, Aggregation.SUM),

	/** Occupancy (percent of time occupied) */
	OCCUPANCY("o", 2, Short.MAX_VALUE, Aggregation.AVERAGE),

	/** Scan count (60 Hz scans) */
	SCAN("c", 2, Short.MAX_VALUE, Aggregation.SUM),

	/** Speed (mph) */
	SPEED("s", 1, Byte.MAX_VALUE, Aggregation.AVERAGE),

	/** Motorcycle volume (count of MOTORCYCLE vehicle class) */
	MOTORCYCLE("vmc", 1, Byte.MAX_VALUE, Aggregation.SUM),

	/** Short volume (count of SHORT vehicle class) */
	SHORT("vs", 1, Byte.MAX_VALUE, Aggregation.SUM),

	/** Medium volume (count of MEDIUM vehicle class) */
	MEDIUM("vm", 1, Byte.MAX_VALUE, Aggregation.SUM),

	/** Long volume (count of LONG vehicle class) */
	LONG("vl", 1, Byte.MAX_VALUE, Aggregation.SUM),

	/** Precipitation rate (um; micrometers) */
	PRECIP_RATE("pr", 2, Short.MAX_VALUE, Aggregation.SUM),

	/** Precipitation type (rain, snow, etc.) */
	PRECIP_TYPE("pt", 1, Byte.MAX_VALUE, Aggregation.NONE);

	/** Maximum bytes to store any sample type */
	static public final int MAX_BYTES = 2;

	/** Base of file extension for archiving samples */
	public final String extension;

	/** Number of bytes per sample */
	public final int sample_bytes;

	/** Maximum sample value allowed */
	public final int max_value;

	/** Sample data aggregation method (SUM or AVERAGE) */
	public final Aggregation aggregation;

	/** Create a new periodic sample type */
	private PeriodicSampleType(String e, int b, int m, Aggregation a) {
		assert b <= MAX_BYTES;
		extension = e;
		sample_bytes = b;
		max_value = m;
		aggregation = a;
	}

	/** Put a sample value into a buffer.
	 * @param buffer Byte buffer.
	 * @param value Sample value. */
	public void putValue(ByteBuffer buffer, int value) {
		if(sample_bytes == 1)
			buffer.put((byte)value);
		else if(sample_bytes == 2)
			buffer.putShort((short)value);
	}

	/** Is a periodic sample valid? */
	public boolean isValid(PeriodicSample ps) {
		return ps.period > 0 &&
		       ps.value > MISSING_DATA &&
		       ps.value <= max_value;
	}
}
