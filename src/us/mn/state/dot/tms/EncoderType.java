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
 * Encoder type enumeration.   The ordinal values correspond to the records
 * in the iris.encoder_type look-up table.
 *
 * @author Douglas Lau
 */
public enum EncoderType {
	GENERIC,	/* 0 */
	AXIS,		/* 1 */
	INFINOVA;	/* 2 */

	/** Get a encoder type from an ordinal value */
	static public EncoderType fromOrdinal(int o) {
		if (o >= 0 && o < values().length)
			return values()[o];
		else
			return GENERIC;
	}
}
