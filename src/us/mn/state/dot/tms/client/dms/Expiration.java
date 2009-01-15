/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

	INDEFINITE("Indefinite", null),
	_5_MINUTES("5 Minutes", 5),
	_15_MINUTES("15 Minutes", 15),
	_30_MINUTES("30 Minutes", 30),
	_45_MINUTES("45 Minutes", 45),
	_1_HOUR("1 Hour", 60),
	_1_5_HOURS("1.5 Hours", 90),
	_2_HOURS("2 Hours", 120),
	_2_5_HOURS("2.5 Hours", 150),
	_3_HOURS("3 Hours", 180),
	_3_5_HOURS("3.5 Hours", 210),
	_4_HOURS("4 Hours", 240),
	_4_5_HOURS("4.5 Hours", 270),
	_5_HOURS("5 Hours", 300),
	_5_5_HOURS("5.5 Hours", 330),
	_6_HOURS("6 Hours", 360),
	_7_HOURS("7 Hours", 420),
	_8_HOURS("8 Hours", 480),
	_9_HOURS("9 Hours", 540),
	_10_HOURS("10 Hours", 600),
	_11_HOURS("11 Hours", 660),
	_12_HOURS("12 Hours", 720),
	_13_HOURS("13 Hours", 780),
	_14_HOURS("14 Hours", 840),
	_15_HOURS("15 Hours", 900),
	_16_HOURS("16 Hours", 960),
	_17_HOURS("17 Hours", 1020);

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
}
