/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  SRF Consulting Group
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

package us.mn.state.dot.tms.reports;

/**
 * This enumeration contains device classes for the IRIS report generator.
 *
 * @author John L. Stanley - SRF Consulting
 */
public enum RptDeviceClassEnum {
	CAMERA,
	DETECTOR,
	DMS,
	GPS,
	GATE,
	INCIDENT,
	METER;

	/** Values array */
	static private final RptDeviceClassEnum[] VALUES = values();

	/** Get a device class from an ordinal value */
	static public RptDeviceClassEnum fromOrdinal(short o) {
		if (o >= 0 && o < VALUES.length)
			return VALUES[o];
		else
			return null;
	}
}
