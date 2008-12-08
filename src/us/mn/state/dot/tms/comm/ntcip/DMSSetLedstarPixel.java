/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;

/**
 * Operation to set the Ledstar pixel current thresholds
 *
 * @author Douglas Lau
 */
public class DMSSetLedstarPixel extends DMSOperation {

	/** LDC pot base value */
	protected final LedLdcPotBase potBase;

	/** Pixel low current threshold */
	protected final LedPixelLow currentLow;

	/** Pixel high current threshols */
	protected final LedPixelHigh currentHigh;

	/** Bad pixel limit */
	protected final LedBadPixelLimit badLimit;

	/** Create a new DMS set pixel threshold operation */
	public DMSSetLedstarPixel(DMSImpl d) {
		super(COMMAND, d);
		potBase = new LedLdcPotBase(d.getLdcPotBase());
		currentLow = new LedPixelLow(d.getPixelCurrentLow());
		currentHigh = new LedPixelHigh(d.getPixelCurrentHigh());
		badLimit = new LedBadPixelLimit(bad);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new SetPixelConfiguration();
	}

	/** Phase to set pixel configuration */
	protected class SetPixelConfiguration extends Phase {

		/** Set the LDC pot base */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(potBase);
			mess.add(currentLow);
			mess.add(currentHigh);
			mess.add(badLimit);
			mess.setRequest();
			return null;
		}
	}
}
