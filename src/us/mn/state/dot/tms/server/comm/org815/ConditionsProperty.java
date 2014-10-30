/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2014  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import us.mn.state.dot.tms.server.PrecipitationType;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Conditions property
 *
 * @author Douglas Lau
 */
public class ConditionsProperty extends Org815Property {

	/** Code indicating weather condition */
	static public enum ConditionCode {
		unknown("", null),
		sensor_init("**", null),
		sensor_error("ER", null),
		no_precip("  ", PrecipitationType.none),
		light_rain("R-", PrecipitationType.rain),
		moderate_rain("R ", PrecipitationType.rain),
		heavy_rain("R+", PrecipitationType.rain),
		light_snow("S-", PrecipitationType.snow),
		moderate_snow("S ", PrecipitationType.snow),
		heavy_snow("S+", PrecipitationType.snow),
		light_precip("P-", PrecipitationType.mix),
		moderate_precip("P ", PrecipitationType.mix),
		heavy_precip("P+", PrecipitationType.mix);

		protected final String code;
		protected final PrecipitationType p_type;
		private ConditionCode(String c, PrecipitationType pt) {
			code = c;
			p_type = pt;
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
	@Override
	public String toString() {
		return code + " rate: " + rate + " accum: " + accumulation;
	}

	/** Get the QUERY request byte code */
	@Override
	protected byte requestQueryByte() {
		return (byte)'A';
	}

	/** Current weather condition */
	private ConditionCode code = ConditionCode.unknown;

	/** Get the current weather condition code */
	public ConditionCode getConditionCode() {
		return code;
	}

	/** Get the current precipitation type */
	public PrecipitationType getPrecipitationType() {
		return code.p_type;
	}

	/** Current one-minute block average precipitation rate */
	private float rate = MISSING_DATA;

	/** Get the current one-minute block average precipitation rate */
	public float getRate() {
		return rate;
	}

	/** Accumulation since last reset */
	private float accumulation = MISSING_DATA;

	/** Get the accumulated precipitation since last reset */
	public float getAccumulation() {
		return accumulation;
	}

	/** Test if the accumulator should be reset */
	public boolean shouldReset() {
		return code == ConditionCode.no_precip && rate == 0 &&
		       accumulation > 0;
	}

	/** Parse a QUERY response */
	@Override
	protected void parseQuery(String line) throws IOException {
		// Responds with 15 or 16 characters, depending on version.
		//   Ver. 44S (03-23-04) responds with 15 characters
		//   ORGMR41S (11/29/2011) responds with 16 characters
		// The extra character always seems to be a space.
		if(line.length() < 15 || line.length() > 16)
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
	 * @return Precipitation rate in millimeters per hour. */
	private float parseRate(String r) throws IOException {
		if("----".equals(r))
			return MISSING_DATA;
		try {
			return Float.parseFloat(r);
		}
		catch(NumberFormatException e) {
			throw new ParsingException("Invalid rate: " + r);
		}
	}
}
