/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
		return (RampMeter) namespace.lookupObject(RampMeter.SONAR_TYPE,
			name);
	}

	/** Get a ramp meter iterator */
	static public Iterator<RampMeter> iterator() {
		return new IteratorWrapper<RampMeter>(namespace.iterator(
			RampMeter.SONAR_TYPE));
	}

	/** Lookup the police panel pin for a ramp meter */
	static public Integer lookupPolicePanelPin(RampMeter meter) {
		CabinetStyle cs = lookupCabinetStyle(meter);
		if (cs != null) {
			switch (meter.getPin()) {
			case 2: // meter 1
				return cs.getPolicePanelPin1();
			case 3: // meter 2
				return cs.getPolicePanelPin2();
			}
		}
		return null;
	}

	/** Lookup the watchdog monitor reset pin for a ramp meter */
	static public Integer lookupWatchdogResetPin(RampMeter meter) {
		CabinetStyle cs = lookupCabinetStyle(meter);
		if (cs != null) {
			switch (meter.getPin()) {
			case 2: // meter 1
				return cs.getWatchdogResetPin1();
			case 3: // meter 2
				return cs.getWatchdogResetPin2();
			}
		}
		return null;
	}

	/** Lookup cabinet style for a ramp meter */
	static private CabinetStyle lookupCabinetStyle(RampMeter meter) {
		Controller ctrl = meter.getController();
		return (ctrl != null) ? ctrl.getCabinetStyle() : null;
	}

	/** Format the meter release rate */
	static public String formatRelease(Integer rate) {
		if (rate !=  null) {
			return rate.toString() + " " +
				I18N.get("units.vehicles.per.hour");
		} else
			return I18N.get("units.na");
	}

	/** Format the meter cycle time from the given release rate */
	static public String formatCycle(Integer rate) {
		if (rate != null) {
			int c = Math.round(36000f / rate);
			return "" + (c / 10) + "." + (c % 10) + " " +
				I18N.get("units.s");
		} else
			return I18N.get("units.na");
	}

	/** Filter a releae rate for valid range */
	static public int filterRate(int r) {
		r = Math.max(r, getMinRelease());
		return Math.min(r, getMaxRelease());
	}

	/** Get the absolute minimum release rate */
	static public int getMinRelease() {
		return SystemAttributeHelper.getMeterMinRelease();
	}

	/** Get the absolute maximum release rate */
	static public int getMaxRelease() {
		return SystemAttributeHelper.getMeterMaxRelease();
	}

	/** Get optional lock, or null */
	static public String optLock(RampMeter meter) {
		return (meter != null) ? meter.getLock() : null;
	}

	/** Get optional meter status attribute, or null */
	static private Object optStatus(RampMeter meter, String key) {
		String status = (meter != null) ? meter.getStatus() : null;
		return optJson(status, key);
	}

	/** Get optional status rate, or null */
	static public Integer optRate(RampMeter meter) {
		Object rate = optStatus(meter, RampMeter.RATE);
		return (rate instanceof Integer) ? (Integer) rate : null;
	}

	/** Test if a ramp meter is metering */
	static public boolean isMetering(RampMeter rm) {
		return optRate(rm) != null;
	}

	/** Get optional queue state, or null */
	static public String optQueue(RampMeter meter) {
		Object queue = optStatus(meter, RampMeter.QUEUE);
		return (queue != null) ? queue.toString() : null;
	}

	/** Get optional meter fault, or null */
	static public String optFault(RampMeter meter) {
		Object fault = optStatus(meter, RampMeter.FAULT);
		return (fault != null) ? fault.toString() : null;
	}

	/** Test if a meter has a fault */
	static public boolean hasFault(RampMeter meter) {
		return optFault(meter) != null;
	}
}
