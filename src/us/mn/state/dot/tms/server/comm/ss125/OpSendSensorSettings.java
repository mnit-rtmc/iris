/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

import java.io.IOException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;

/**
 * Controller operation to send settings to an SS125.
 *
 * @author Douglas Lau
 */
public class OpSendSensorSettings extends OpSS125 {

	/** Time interval for data binning */
	static protected final int BINNING_INTERVAL = 30;

	/** Flag to perform a controller restart */
	protected final boolean restart;

	/** Create a new operation to send settings to a sensor */
	public OpSendSensorSettings(ControllerImpl c, boolean r) {
		super(DOWNLOAD, c);
		restart = r;
	}

	/** Begin the sensor initialization operation */
	public void begin() {
		phase = new QueryGenConfig();
	}

	/** Phase to query the general config  */
	protected class QueryGenConfig extends Phase {

		/** Query the general config */
		protected Phase poll(AddressedMessage mess) throws IOException {
			GeneralConfigRequest gcr = new GeneralConfigRequest();
			mess.add(gcr);
			mess.getRequest();
			SS125_LOG.log(controller.getName() + ": orientation " +
				gcr.getOrientation());
			SS125_LOG.log(controller.getName() + ": location " +
				gcr.getLocation());
			SS125_LOG.log(controller.getName() + ": description " +
				gcr.getDescription());
			SS125_LOG.log(controller.getName() + ": serial # " +
				gcr.getSerialNumber());
			SS125_LOG.log(controller.getName() + ": metric " +
				gcr.isMetric());
			return null;
		}
	}
}
