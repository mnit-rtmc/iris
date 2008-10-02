/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

/**
 * Encapsulates a possible expiration time.
 *
 * @author Erik Engstrom
 */
public class Expiration {

	private final String label;

	private final int duration;

	/**
	 * Create a new Expiration.
	 *
	 * @param text the text displayed by this expiration
	 * @param d integer containing the number of minutes this expiration
	 * represents; 65536 indicates that the message does not expire
	 */
	public Expiration(String text, int d) {
		label = text;
		duration = d;
	}

	/**
	 * Returns a String describing the duration of the Expiration
	 * @return duration of the expiration
	 */
	public String toString() {
		return label;
	}

	/**
	 * Gets the number of minutes represented by this expiration
	 * @return the number of minutes represented by this expiration
	 */
	public int getDuration() {
		return duration;
	}
}
