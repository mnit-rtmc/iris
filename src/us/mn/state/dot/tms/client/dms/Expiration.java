/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
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
 * An enum of possible expiration times.
 *
 * @author Douglas Lau
 */
public enum Expiration {

	INDEFINITE("", null),
	_5_MINUTES("5 Min", 5),
	_10_MINUTES("10 Min", 10),
	_15_MINUTES("15 Min", 15),
	_30_MINUTES("30 Min", 30),
	_45_MINUTES("45 Min", 45),
	_1_HOUR("60 Min", 60),
	_1_5_HOURS("90 Min", 90),
	_2_HOURS("2 Hours", 120),
	_3_HOURS("3 Hours", 180),
	_4_HOURS("4 Hours", 240),
	_5_HOURS("5 Hours", 300),
	_6_HOURS("6 Hours", 360),
	_7_HOURS("7 Hours", 420),
	_8_HOURS("8 Hours", 480),
	_9_HOURS("9 Hours", 540),
	_10_HOURS("10 Hours", 600),
	_12_HOURS("12 Hours", 720),
	_14_HOURS("14 Hours", 840),
	_16_HOURS("16 Hours", 960);

	public final String label;

	public final Integer duration;

	/**
	 * Create a new Expiration.
	 *
	 * @param l the expiration label
	 * @param d number of minutes of duration; null indicates no expiration
	 */
	private Expiration(String l, Integer d) {
		label = l;
		duration = d;
	}

	/** Get a string representation of the expiration */
	@Override
	public String toString() {
		return label;
	}
}
