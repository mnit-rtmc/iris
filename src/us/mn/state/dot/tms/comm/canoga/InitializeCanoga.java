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
package us.mn.state.dot.tms.comm.canoga;

import java.io.IOException;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.ControllerOperation;

/**
 * Controller operation to initialize a Canoga card
 *
 * @author Douglas Lau
 */
public class InitializeCanoga extends ControllerOperation {

	/** Flag to perform a controller restart */
	protected final boolean restart;

	/** Canoga card serial number */
	protected final SerialNumberRequest serial_number =
		new SerialNumberRequest();

	/** Canoga firmware version */
	protected final VersionRequest version = new VersionRequest();

	/** Create a new initialize Canoga object */
	public InitializeCanoga(ControllerImpl c, boolean r) {
		super(DOWNLOAD, c, c.toString());
		restart = r;
		controller.setSetup("OK");
	}

	/** Begin the sensor initialization operation */
	public void begin() {
		if(restart)
			phase = new QuerySerialNumber();
		else
			phase = new QuerySerialNumber();
	}

	/** Phase to query the serial number */
	protected class QuerySerialNumber extends Phase {

		/** Synchronize the clock */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(serial_number);
			mess.getRequest();
			return new QueryVersion();
		}
	}

	/** Phase to query the firmware version */
	protected class QueryVersion extends Phase {

		/** Query the firmware version */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(version);
			mess.getRequest();
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(success) {
			controller.setVersion(version.getValue() + " (" +
				serial_number.getValue() + ")");
		} else
			controller.setSetup(null);
		super.cleanup();
	}
}
