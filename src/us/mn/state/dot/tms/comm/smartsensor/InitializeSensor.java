/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.smartsensor;

import java.io.IOException;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.ControllerOperation;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * Controller operation to initialize a SmartSensor
 *
 * @author Douglas Lau
 */
public class InitializeSensor extends ControllerOperation {

	/** Time interval for data binning */
	static protected final int BINNING_INTERVAL = 30;

	/** Flag to perform a controller restart */
	protected final boolean restart;

	/** Create a new initialize sensor object */
	public InitializeSensor(ControllerImpl c, boolean r) {
		super(DOWNLOAD, c, c.toString());
		restart = r;
	}

	/** Begin the sensor initialization operation */
	public void begin() {
		if(restart)
			phase = new GetTimeInterval();
		else
			phase = new QueryVersion();
	}

	/** Phase to get the time interval (for binning) */
	protected class GetTimeInterval extends Phase {

		/** Get the time interval (for binning) */
		protected Phase poll(AddressedMessage mess) throws IOException {
			TimeIntervalRequest ti = new TimeIntervalRequest();
			mess.add(ti);
			mess.getRequest();
			if(ti.value == BINNING_INTERVAL)
				return new GetClassification();
			else
				return new SetTimeInterval();
		}
	}

	/** Phase to set the time interval (for binning) */
	protected class SetTimeInterval extends Phase {

		/** Set the time interval (for binning) */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new TimeIntervalRequest(BINNING_INTERVAL));
			mess.setRequest();
			return new GetClassification();
		}
	}

	/** Phase to get the classification lengths */
	protected class GetClassification extends Phase {

		/** Get the classification lengths */
		protected Phase poll(AddressedMessage mess) throws IOException {
			ClassificationRequest cr = new ClassificationRequest();
			mess.add(cr);
			mess.getRequest();
			if(cr.isDefault())
				return new QueryVersion();
			else
				return new SetClassification();
		}
	}

	/** Phase to set the classification lengths */
	protected class SetClassification extends Phase {

		/** Set the classification lengths */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new ClassificationRequest());
			mess.setRequest();
			return new QueryVersion();
		}
	}

	/** Phase to query the firmware version */
	protected class QueryVersion extends Phase {

		/** Query the firmware version */
		protected Phase poll(AddressedMessage mess) throws IOException {
			VersionRequest vr = new VersionRequest();
			mess.add(vr);
			mess.getRequest();
			controller.setVersion(vr.getVersion());
			return new SynchronizeClock();
		}
	}

	/** Phase to synchronize the clock */
	protected class SynchronizeClock extends Phase {

		/** Synchronize the clock */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new TimeRequest());
			mess.setRequest();
			return null;
		}
	}
}
