/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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

import java.util.Iterator;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Ramp meter helper methods.
 *
 * @author Douglas Lau
 */
public class RampMeterHelper extends BaseHelper {

	/** Disallow instantiation */
	private RampMeterHelper() {
		assert false;
	}

	/** Lookup the ramp meter with the specified name */
	static public RampMeter lookup(String name) {
		return (RampMeter)namespace.lookupObject(RampMeter.SONAR_TYPE,
			name);
	}

	/** Get a ramp meter iterator */
	static public Iterator<RampMeter> iterator() {
		return new IteratorWrapper<RampMeter>(namespace.iterator(
			RampMeter.SONAR_TYPE));
	}

	/** Lookup the preset for a ramp meter */
	static public CameraPreset getPreset(RampMeter meter) {
		if (meter != null)
			return meter.getPreset();
		else
			return null;
	}

	/** Format the meter release rate */
	static public String formatRelease(Integer rate) {
		if(rate !=  null) {
			return rate.toString() + " " +
				I18N.get("units.vehicles.per.hour");
		} else
			return I18N.get("units.na");
	}

	/** Format the meter cycle time from the given release rate */
	static public String formatCycle(Integer rate) {
		if(rate != null) {
			int c = Math.round(36000f / rate);
			return "" + (c / 10) + "." + (c % 10) + " " +
				I18N.get("units.s");
		} else
			return I18N.get("units.na");
	}
}
