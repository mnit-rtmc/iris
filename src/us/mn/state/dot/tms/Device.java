/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2024  Minnesota Department of Transportation
 * Copyright (C) 2025       SRF Consulting Group
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
 * Device is the base interface for all field devices, including detectors,
 * cameras, ramp meters, dynamic message signs, etc.
 *
 * @author Douglas Lau
 * @author John L. Stanley - SRF Consulting
 */
public interface Device extends ControllerIO {

	/** Set notes (including hashtags) */
	void setNotes(String n);

	/** Request a device operation (query message, test pixels, etc.) */
	void setDeviceRequest(int r);

	/** Get the operation description */
	String getOperation();

	/** Get hashtags from device notes */
	Hashtags getHashtags();

	/** Check if a device'a notes has a specific hashtag.
	 *  The tag string must have a '#' prefix. */
	boolean hasHashtag(String tag);

	/** Check if a device'a notes has any hashtag from an array.
	 *  Every string in the tagArray must have a '#' prefix. */
	boolean hasHashtag(String[] tagArray);
}
