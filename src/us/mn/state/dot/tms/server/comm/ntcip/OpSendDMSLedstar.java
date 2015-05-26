/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2015  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.server.comm.ntcip.mibledstar.MIB.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;

/**
 * Operation to set the Ledstar pixel current thresholds
 *
 * @author Douglas Lau
 */
public class OpSendDMSLedstar extends OpDMS {

	/** LDC pot base value */
	private final ASN1Integer potBase = ledLdcPotBase.makeInt();

	/** Pixel low current threshold */
	private final ASN1Integer currentLow = ledPixelLow.makeInt();

	/** Pixel high current threshols */
	private final ASN1Integer currentHigh = ledPixelHigh.makeInt();

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
			logStore(potBase);
			logStore(currentLow);
			logStore(currentHigh);
			mess.storeProps();
			return null;
		}
	}
}
