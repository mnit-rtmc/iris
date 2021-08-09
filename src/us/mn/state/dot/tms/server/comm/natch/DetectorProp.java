/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * Detector property
 *
 * @author Douglas Lau
 */
abstract public class DetectorProp extends NatchProp {

	/** Detector number (0-31) */
	public int detector_num;

	/** Lookup the input pin for the detector */
	protected int lookupPin(ControllerImpl ctrl) {
		if (detector_num >= 0 && detector_num < 32) {
			int pin = detector_num + 39;
			if (ctrl.getDetectorAtPin(pin) != null)
				return pin;
		}
		return 0;
	}

	/** Create a new detector property */
	protected DetectorProp(Counter c, int dn) {
		super(c);
		detector_num = dn;
	}
}
