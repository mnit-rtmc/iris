/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Simple temperature formatting helper.
 *
 * @author Douglas Lau
 */
public class Temperature {

	/** Unknown temperature */
	static protected final String UNKNOWN = "???";

	/** Celsius temperature string */
	static protected final String CELSIUS = "\u00B0 C";

	/** Fahrenheit temperature string */
	static protected final String FAHRENHEIT = "\u00B0 F";

	/** Convert a celsius temperature to fahrenheit */
	static protected int toFahrenheit(int celsius) {
		return Math.round(celsius * 9 / 5f) + 32;
	}

	/** Convert a fahrenheit temperature to celsius */
	static protected int toCelsius(int fahrenheit) {
		return Math.round((fahrenheit - 32) * 5 / 9f);
	}

	/** Format a given temperature.
	 * @param temp Temperature in degrees celsius */
	static public String formatCelsius(Integer temp) {
		if(temp == null)
			return UNKNOWN;
		if(SystemAttrEnum.TEMP_FAHRENHEIT_ENABLE.getBoolean())
			return "" + toFahrenheit(temp) + FAHRENHEIT;
		else
			return "" + temp + CELSIUS;
	}

	/** Format a given temperature.
	 * @param temp Temperature in degrees fahrenheit */
	static public String formatFahrenheit(Integer temp) {
		if(temp == null)
			return UNKNOWN;
		if(SystemAttrEnum.TEMP_FAHRENHEIT_ENABLE.getBoolean())
			return "" + temp + FAHRENHEIT;
		else
			return "" + toCelsius(temp) + CELSIUS;
	}

	/** Private constructor (do not allow instantiation) */
	private Temperature() { }
}
