/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.IOException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;

/**
 * Send a level-1 restart request to a 170 controller
 *
 * @author Douglas Lau
 */
public class OpReset170 extends Op170 {

	/** Create a new send level-1 restart operation */
	public OpReset170(ControllerImpl c) {
		super(DOWNLOAD, c);
	}

	/** Begin the operation */
	public void begin() {
		phase = new Level1Restart();
	}

	/** Phase to restart the controller */
	protected class Level1Restart extends Phase {

		/** Restart the controller */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new Level1Request());
			mess.setRequest();
			return null;
		}
	}
}
