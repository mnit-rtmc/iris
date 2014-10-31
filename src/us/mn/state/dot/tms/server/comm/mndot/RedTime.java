/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import us.mn.state.dot.tms.RampMeterType;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.units.Interval;

/**
 * Utility class to convert ramp meter red times to and from release rates.
 *
 * @author Douglas Lau
 */
public class RedTime {

	/** Calculate red time from a release rate.
	 * @param rate Release rate (vehicles per hour).
	 * @param mt Ordinal of ramp meter type.
	 * @return Red time (tenths of a second). */
	static public int fromReleaseRate(int rate, int mt) {
		float secs_per_veh = Interval.HOUR.divide(rate);
		if (mt == RampMeterType.SINGLE.ordinal())
			secs_per_veh /= 2;
		float green = SystemAttrEnum.METER_GREEN_SECS.getFloat();
		float yellow = SystemAttrEnum.METER_YELLOW_SECS.getFloat();
		float min_red = SystemAttrEnum.METER_MIN_RED_SECS.getFloat();
		float red_time = secs_per_veh - (green + yellow);
		float red_secs = Math.max(red_time, min_red);
		return Math.round(red_secs * 10);
	}

	/** Calculate release rate from a red time.
	 * @param red_time Red time (tenths of a second).
	 * @param mt Ordinal of ramp meter type.
	 * @return Release rate (vehicles per hour). */
	static public int toReleaseRate(int red_time, int mt) {
		float red_secs = red_time / 10.0f;
		float green = SystemAttrEnum.METER_GREEN_SECS.getFloat();
		float yellow = SystemAttrEnum.METER_YELLOW_SECS.getFloat();
		float secs_per_veh = red_secs + yellow + green;
		if (mt == RampMeterType.SINGLE.ordinal())
			secs_per_veh *= 2;
		Interval rate = new Interval(secs_per_veh);
		return Math.round(rate.per(Interval.HOUR));
	}

	/** Don't allow instantiation */
	private RedTime() { }
}
