/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2009  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;

/**
 * Operation to set the manual brightness level on a DMS
 *
 * @author Douglas Lau
 */
public class OpSendDMSBrightness extends OpDMS {

	/** Manual brightness level */
	protected final int level;

	/** Create a new DMS manual brightness operation */
	public OpSendDMSBrightness(DMSImpl d, int l) {
		super(COMMAND, d);
		level = l;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new SetManualBrightness();
	}

	/** Phase to set manual brightness level */
	public class SetManualBrightness extends Phase {

		/** Set the manual brightness level */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new DmsIllumManLevel(level));
			mess.setRequest();
			return null;
		}
	}
}
