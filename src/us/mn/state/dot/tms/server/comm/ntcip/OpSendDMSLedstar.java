/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mibledstar.*;

/**
 * Operation to set the Ledstar pixel current thresholds
 *
 * @author Douglas Lau
 */
public class OpSendDMSLedstar extends OpDMS {

	/** LDC pot base value */
	protected final LedLdcPotBase potBase = new LedLdcPotBase();

	/** Pixel low current threshold */
	protected final LedPixelLow currentLow = new LedPixelLow();

	/** Pixel high current threshols */
	protected final LedPixelHigh currentHigh = new LedPixelHigh();

	/** Create a new DMS set pixel threshold operation */
	public OpSendDMSLedstar(DMSImpl d) {
		super(PriorityLevel.COMMAND, d);
		potBase.setInteger(d.getLdcPotBase());
		currentLow.setInteger(d.getPixelCurrentLow());
		currentHigh.setInteger(d.getPixelCurrentHigh());
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		return new SetPixelConfiguration();
	}

	/** Phase to set pixel configuration */
	protected class SetPixelConfiguration extends Phase {

		/** Set the LDC pot base */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(potBase);
			mess.add(currentLow);
			mess.add(currentHigh);
			DMS_LOG.log(dms.getName() + ":= " + potBase);
			DMS_LOG.log(dms.getName() + ":= " + currentLow);
			DMS_LOG.log(dms.getName() + ":= " + currentHigh);
			mess.storeProps();
			return null;
		}
	}
}
