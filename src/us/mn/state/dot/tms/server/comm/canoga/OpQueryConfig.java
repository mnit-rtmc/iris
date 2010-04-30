/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.canoga;

import java.io.IOException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpController;

/**
 * Operation to query the configuration of a Canoga card
 *
 * @author Douglas Lau
 */
public class OpQueryConfig extends OpController {

	/** Canoga card serial number */
	protected final SerialNumberProperty serial_number =
		new SerialNumberProperty();

	/** Canoga firmware version */
	protected final VersionProperty version = new VersionProperty();

	/** Create an operation to query the Canoga configuration */
	public OpQueryConfig(ControllerImpl c) {
		super(DOWNLOAD, c, c.toString());
	}

	/** Begin the query config operation */
	public void begin() {
		phase = new QuerySerialNumber();
	}

	/** Phase to query the serial number */
	protected class QuerySerialNumber extends Phase {

		/** Query the serial number */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(serial_number);
			mess.queryProps();
			return new QueryVersion();
		}
	}

	/** Phase to query the firmware version */
	protected class QueryVersion extends Phase {

		/** Query the firmware version */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(version);
			mess.queryProps();
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(success) {
			controller.setVersion(version.getValue() + " (" +
				serial_number.getValue() + ")");
		}
		super.cleanup();
	}
}
