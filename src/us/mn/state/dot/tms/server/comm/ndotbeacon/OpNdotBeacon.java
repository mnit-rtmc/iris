/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2022  SRF Consulting Group
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
 *
 * Derived in part from MNDOT's IRIS code for controlling their
 * HySecurity STC gates.
 */
package us.mn.state.dot.tms.server.comm.ndotbeacon;

import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

import static us.mn.state.dot.tms.server.comm.ndotbeacon.NdotBeaconPoller.NDOTBEACON_LOG;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Base Op Class for Nebraska Beacon (NDOT Beacon) devices
 *
 * @author John L. Stanley - SRF Consulting
 */

abstract public class OpNdotBeacon<T extends ControllerProperty>
  extends OpDevice<T> {

	/** Log an error msg */
	protected void logError(String msg) {
		if (NDOTBEACON_LOG.isOpen())
			NDOTBEACON_LOG.log(controller.getName() + "! " + msg);
	}

	/** Beacon device */
	protected final BeaconImpl beacon;

	/** Status property */
	protected NdotBeaconProperty prop;

	/** Create a new NDOT Beacon operation */
	protected OpNdotBeacon(PriorityLevel p, BeaconImpl b, boolean ex) {
		super(p, b, ex);
		beacon = b;
	}

	/** Create a new NDOT Beacon operation */
	protected OpNdotBeacon(PriorityLevel p, BeaconImpl b) {
		super(p, b);
		beacon = b;
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess()) {
			beacon.setStateNotify(prop.getState());
			putCtrlFaults("other", prop.getFaultStatus());
			updateCtrlStatus();
		}
		super.cleanup();
	}
}
