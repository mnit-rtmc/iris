/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.Checker;

/**
 * Ramp meter helper methods.
 *
 * @author Douglas Lau
 */
public class RampMeterHelper extends BaseHelper {

	/** Disallow instantiation */
	protected RampMeterHelper() {
		assert false;
	}

	/** Find ramp meters using a Checker */
	static public RampMeter find(final Checker<RampMeter> checker) {
		return (RampMeter)namespace.findObject(RampMeter.SONAR_TYPE,
			checker);
	}

	/** Lookup the camera for a ramp meter */
	static public Camera getCamera(RampMeter meter) {
		if(meter != null)
			return meter.getCamera();
		else
			return null;
	}
}
