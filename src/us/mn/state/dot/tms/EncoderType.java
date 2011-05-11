/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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
 */
public enum EncoderType {

	/** Undefined encoder type (0) */
	NONE(" ", StreamType.NONE),

	/** Axis MJPEG (1) */
	AXIS_MJPEG("Axis MJPEG", StreamType.MJPEG),

	/** Axis MPEG4 (2) */
	AXIS_MPEG4("Axis MPEG4", StreamType.MPEG4),

	/** Infinova MPEG4 (3) */
	INFINOVA_MPEG4("Infinova MPEG4", StreamType.MPEG4);

	/** Create a new encoder type */
	private EncoderType(String d, StreamType st) {
		description = d;
		stream_type = st;
	}

	/** Description */
	public final String description;

	/** Stream type */
	public final StreamType stream_type;

	/** Get the string description of the encoder type */
	public String toString() {
		return description;
	}

	/** Get a encoder type from an ordinal value */
	static public EncoderType fromOrdinal(int o) {
		for(EncoderType et: EncoderType.values()) {
			if(et.ordinal() == o)
				return et;
		}
		return NONE;
	}

	/** Get an array of encoder type descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for(EncoderType et: EncoderType.values())
			d.add(et.description);
		return d.toArray(new String[0]);
	}
}
