/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2007  Minnesota Department of Transportation
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
 * Operation to set the brightness control mode for a DMS
 *
 * @author Douglas Lau
 */
public class DMSBrightnessControl extends DMSOperation {

	/** Manual control flag */
	protected final boolean manual;

	/** Create a new DMS brightness control object */
	public DMSBrightnessControl(DMSImpl d, boolean m) {
		super(COMMAND, d);
		manual = m;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new SetBrightnessControl();
	}

	/** Phase to set brightness control mode */
	protected class SetBrightnessControl extends Phase {

		/** Set the brightness control mode */
		protected Phase poll(AddressedMessage mess) throws IOException {
			int mode = DmsIllumControl.PHOTOCELL;
			if(manual)
				mode = DmsIllumControl.MANUAL;
			mess.add(new DmsIllumControl(mode));
			mess.setRequest();
			dms.setManualBrightness(manual);
			return null;
		}
	}
}
