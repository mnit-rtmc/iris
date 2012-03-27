/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
 * Constants is an interface with some traffic data related constants.
 *
 * @author Douglas Lau
 */
public interface Constants {

	/** Number of feet in one mile */
	public int FEET_PER_MILE = 5280;

	/** Maximum "realistic" volume for a 30-second sample */
	public int MAX_VOLUME = 37;

	/** Maximum number of scans in 30 seconds */
	public int MAX_SCANS = 1800;

	/** Maximum occupancy value (100%) */
	public int MAX_OCCUPANCY = 100;

	/** Valid density threshold for speed calculation */
	public float DENSITY_THRESHOLD = 1.2f;

	/** Maximum (valid) speed (miles per hour) */
	public float MAX_SPEED = 100.0f;

	/** Default average detector field length */
	public float DEFAULT_FIELD_LENGTH = 22.0f;

	/** Special case value for missing data */
	public byte MISSING_DATA = -1;

	/** Unknown status string */
	String UNKNOWN = "???";
}
