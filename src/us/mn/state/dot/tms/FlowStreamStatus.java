/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  Minnesota Department of Transportation
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
 * FlowStreamStatus enumeration.  The ordinal values correspond to the records
 * in the iris.flow_stream_status look-up table.
 *
 * @author Douglas Lau
 */
public enum FlowStreamStatus {
	FAILED,   /* 0 */
	STARTING, /* 1 */
	PLAYING;  /* 2 */

	/** Cached values array */
	static private final FlowStreamStatus[] VALUES = values();

	/** Get status from an ordinal value */
	static public FlowStreamStatus fromOrdinal(int o) {
		return (o >= 0 && o < VALUES.length) ? VALUES[o] : FAILED;
	}
}
