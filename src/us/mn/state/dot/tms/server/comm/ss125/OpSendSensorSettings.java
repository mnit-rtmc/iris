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
import us.mn.state.dot.tms.ControllerHelper;
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

	/** General config request */
	protected final GeneralConfigRequest gen_config =
		new GeneralConfigRequest();

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
			mess.add(gen_config);
			mess.getRequest();
			SS125_LOG.log(controller.getName() + ": orientation " +
				gen_config.getOrientation());
			SS125_LOG.log(controller.getName() + ": location " +
				gen_config.getLocation());
			SS125_LOG.log(controller.getName() + ": description " +
				gen_config.getDescription());
			SS125_LOG.log(controller.getName() + ": serial # " +
				gen_config.getSerialNumber());
			SS125_LOG.log(controller.getName() + ": metric " +
				gen_config.isMetric());
			if(shouldUpdateGenConfig())
				return new SendGenConfig();
			else
				return null;
		}
	}

	/** Check if the general config should be updated */
	protected boolean shouldUpdateGenConfig() {
		String loc = ControllerHelper.getLocation(controller);
		if(!loc.equals(gen_config.getLocation()))
			return true;
		return gen_config.isMetric();
	}

	/** Phase to send the general config */
	protected class SendGenConfig extends Phase {

		/** Send the general config */
		protected Phase poll(AddressedMessage mess) throws IOException {
			gen_config.setLocation(ControllerHelper.getLocation(
				controller));
			gen_config.setMetric(false);
			mess.add(gen_config);
			SS125_LOG.log(controller.getName() + ":= location " +
				gen_config.getLocation());
			SS125_LOG.log(controller.getName() + ":= metric " +
				gen_config.isMetric());
			mess.setRequest();
			return null;
		}
	}
}
