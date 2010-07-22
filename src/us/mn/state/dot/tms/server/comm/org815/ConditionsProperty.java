/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.org815;

import java.io.IOException;
import us.mn.state.dot.tms.Constants;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Conditions property
 *
 * @author Douglas Lau
 */
public class ConditionsProperty extends Org815Property {

	/** Code indicating weather condition */
	static public enum ConditionCode {
		unknown(""),
		sensor_init("**"),
		sensor_error("ER"),
		no_precip("  "),
		light_rain("R-"),
		moderate_rain("R "),
		heavy_rain("R+"),
		light_snow("S-"),
		moderate_snow("S "),
		heavy_snow("S+"),
		light_precip("P-"),
		moderate_precip("P "),
		heavy_precip("P+");

		protected final String code;
		private ConditionCode(String c) {
			code = c;
		}
		static public ConditionCode fromCode(String c) {
			for(ConditionCode cc: ConditionCode.values()) {
				if(cc.code.equals(c))
					return cc;
			}
			return unknown;
		}
	}

	/** Get a string value of the property */
	public String toString() {
		return code + " rate: " + rate + " accum: " + accumulation;
	}

	/** Get the QUERY request byte code */
	protected byte requestQueryByte() {
		return (char)'A';
	}

	/** Current weather condition */
	protected ConditionCode code = ConditionCode.unknown;

	/** Get the current weather condition code */
	public ConditionCode getConditionCode() {
		return code;
	}

	/** Current one-minute block average precipitation rate */
	protected float rate = Constants.MISSING_DATA;

	/** Get the current one-minute block average precipitation rate */
	public float getRate() {
		return rate;
	}

	/** Accumulation since last reset */
	protected float accumulation = Constants.MISSING_DATA;

	/** Get the accumulated precipitation since last reset */
	public float getAccumulation() {
		return accumulation;
	}

	/** Parse a QUERY response */
	protected void parseQuery(String line) throws IOException {
		if(line.length() != 15)
			throw new ParsingException("Invalid response: " + line);
		ConditionCode cc = ConditionCode.fromCode(line.substring(0, 2));
		if(cc == ConditionCode.unknown)
			throw new ParsingException("Bad condition: " + line);
		float rt = parseRate(line.substring(3, 7));
		float acc = parseAccumulation(line.substring(8, 15));
		code = cc;
		rate = rt;
		accumulation = acc;
	}

	/** Parse the one-minute block average precipitation rate.
	 * @param r 4-character rate to parse.
	 * @return Precipitation rate in milimeters per hour. */
	protected float parseRate(String r) throws IOException {
		if("----".equals(r))
			return Constants.MISSING_DATA;
		try {
			return Float.parseFloat(r);
		}
		catch(NumberFormatException e) {
			throw new ParsingException("Invalid rate: " + r);
		}
	}

	/** Parse the accumulated precipitation since last reset.
	 * @param a 7-character accumulation to parse.
	 * @return Accumulation since last reset in milimeters. */
	protected float parseAccumulation(String a) throws IOException {
		if("---.---".equals(a))
			return Constants.MISSING_DATA;
		try {
			return Float.parseFloat(a);
		}
		catch(NumberFormatException e) {
			throw new ParsingException("Invalid accum: " + a);
		}
	}
}
