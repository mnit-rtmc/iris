/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.natch;

import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DetectorImpl;
import us.mn.state.dot.tms.server.RampMeterImpl;

/**
 * Detector property
 *
 * @author Douglas Lau
 */
abstract public class DetectorProp extends NatchProp {

	/** Maximum detector number in table.
	 *
	 * The last 2 numbers (30 and 31) are reserved for
	 * ramp meter green count detectors. */
	static protected final int MAX_DET = 32;

	/** First ramp meter green detector number */
	static private final int METER_1_GREEN_DET = 30;

	/** Second ramp meter green detector number */
	static private final int METER_2_GREEN_DET = 31;

	/** Detector number (0-31) */
	public int detector_num;

	/** Check if detector number is valid */
	protected boolean isValidNum() {
		return detector_num >= 0 && detector_num < MAX_DET;
	}

	/** Lookup the detector from a controller */
	protected DetectorImpl lookupDet(ControllerImpl ctrl) {
		if (isValidNum()) {
			int pin = detector_num + 39;
			DetectorImpl det = ctrl.getDetectorAtPin(pin);
			if (det != null)
				return det;
			det = getMeter1Green(ctrl);
			if (det != null)
				return det;
			det = getMeter2Green(ctrl);
			if (det != null)
				return det;
		}
		return null;
	}

	/** Lookup the input pin for the detector */
	protected int lookupPin(ControllerImpl ctrl) {
		if (isValidNum()) {
			int pin = detector_num + 39;
			if (ctrl.getDetectorAtPin(pin) != null)
				return pin;
			else if (getMeter1Green(ctrl) != null)
				return NatchPoller.METER_1_PIN;
			else if (getMeter2Green(ctrl) != null)
				return NatchPoller.METER_2_PIN;
		}
		return 0;
	}

	/** Get the first meter green detector (if det num is correct) */
	private DetectorImpl getMeter1Green(ControllerImpl ctrl) {
		if (detector_num == METER_1_GREEN_DET) {
			RampMeterImpl meter = NatchPoller.lookupMeter(ctrl,
				NatchPoller.METER_1_PIN);
			if (meter != null)
				return meter.getGreenDet();
		}
		return null;
	}

	/** Get the second meter green detector (if det num is correct) */
	private DetectorImpl getMeter2Green(ControllerImpl ctrl) {
		if (detector_num == METER_2_GREEN_DET) {
			RampMeterImpl meter = NatchPoller.lookupMeter(ctrl,
				NatchPoller.METER_2_PIN);
			if (meter != null)
				return meter.getGreenDet();
		}
		return null;
	}

	/** Create a new detector property */
	protected DetectorProp(Counter c, int dn) {
		super(c);
		detector_num = dn;
	}
}
