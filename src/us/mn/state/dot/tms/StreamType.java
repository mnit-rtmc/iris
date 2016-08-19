/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

/**
 * Stream type enumeration.  The ordinal values correspond to the records
 * in the iris.stream_type look-up table.
 *
 * @author Douglas Lau
 */
public enum StreamType {
	UNKNOWN,	/* use for MMS or other weird protocols */
	MJPEG,		/* motion JPEG */
	MPEG4,
	H264,
	H265;

	/** Get a stream type from an ordinal value */
	static public StreamType fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return UNKNOWN;
	}
}
