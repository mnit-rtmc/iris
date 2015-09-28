/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2015  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
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

import java.util.LinkedList;

/**
 * Encoder type enumeration.   The ordinal values correspond to the records
 * in the iris.encoder_type look-up table.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public enum EncoderType {

	/** Undefined encoder type (0) */
	NONE(" ", StreamType.NONE, StreamType.NONE),

	/** Axis MJPEG (1) */
	AXIS_MJPEG("Axis MJPEG", StreamType.MJPEG, StreamType.MJPEG),

	/** Axis MPEG4 (2) */
	AXIS_MPEG4("Axis MPEG4", StreamType.MPEG4, StreamType.MJPEG),

	/** Infinova MPEG4 (3) */
	INFINOVA_MPEG4("Infinova MPEG4", StreamType.MPEG4, StreamType.MJPEG),

	/** Axis MPEG4 RTP over RTSP (4) */
	AXIS_MP4_AXRTSP("Axis MP4 axrtsp", StreamType.MPEG4, StreamType.NONE),

	/** Axis MPEG4 RTP over RTSP over HTTP (5) */
	AXIS_MP4_AXRTSPHTTP("Axis MP4 axrtsphttp", StreamType.MPEG4,
	                    StreamType.NONE),

	/** Generic MMS (6) */
	GENERIC_MMS("Generic MMS", StreamType.MMS, StreamType.NONE),

	/** Axis JPEG (7) */
	AXIS_JPEG("Axis JPEG", StreamType.NONE, StreamType.MJPEG);

	/** Create a new encoder type.
	 * @param d Description.
	 * @param dst Stream type of direct stream.
	 * @param ist Stream type of indirect stream (using video servlet). */
	private EncoderType(String d, StreamType dst, StreamType ist) {
		description = d;
		direct_stream = dst;
		indirect_stream = ist;
	}

	/** Description */
	public final String description;

	/** Direct stream type */
	public final StreamType direct_stream;

	/** Indirect stream type (using video servlet) */
	public final StreamType indirect_stream;

	/** Get the string description of the encoder type */
	public String toString() {
		return description;
	}

	/** Get a encoder type from an ordinal value */
	static public EncoderType fromOrdinal(int o) {
		for (EncoderType et: values()) {
			if (et.ordinal() == o)
				return et;
		}
		return NONE;
	}

	/** Get an array of encoder type descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for (EncoderType et: values())
			d.add(et.description);
		return d.toArray(new String[0]);
	}
}
