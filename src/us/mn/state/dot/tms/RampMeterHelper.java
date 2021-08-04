/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2021  Minnesota Department of Transportation
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

	/** Lookup the police panel pin for a ramp meter */
	static public int lookupPolicePanelPin(RampMeter meter) {
		String sty = lookupCabinetStyle(meter);
		if (sty != null) {
			// Original 334Z cabinet used pin 82 for both meters
			if (sty.equals("334Z"))
				return 82;
			// 334Z-94 or later pin 81 (ramp 1) and 82 (ramp 2)
			if (sty.startsWith("334Z"))
				return (meter.getPin() == 2) ? 81 : 82;
		}
		// older cabinets used pin 80
		return 80;
	}

	/** Lookup cabinet style for a ramp meter */
	static private String lookupCabinetStyle(RampMeter meter) {
		Controller ctrl = meter.getController();
		if (ctrl != null) {
			Cabinet cab = ctrl.getCabinet();
			if (cab != null) {
				CabinetStyle cs = cab.getStyle();
				if (cs != null)
					return cs.getName();
			}
		}
		return null;
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
