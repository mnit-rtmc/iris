/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cbw;

import java.io.IOException;
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Query the state of a beacon
 *
 * @author Douglas Lau
 */
public class OpQueryBeaconState extends OpDevice<CBWProperty> {

	/** Beacon device */
	private final BeaconImpl beacon;

	/** Relay state property */
	private final CBWProperty property = new CBWProperty("state.xml");

	/** Create a new query beacon state operation */
	public OpQueryBeaconState(BeaconImpl b) {
		super(PriorityLevel.DATA_30_SEC, b);
		beacon = b;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<CBWProperty> phaseTwo() {
		return new QueryBeacon();
	}

	/** Phase to query the beacon status */
	private class QueryBeacon extends Phase<CBWProperty> {

		/** Query the beacon status */
		protected Phase<CBWProperty> poll(
			CommMessage<CBWProperty> mess) throws IOException
		{
			mess.add(property);
			mess.queryProps();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			updateBeacon();
		super.cleanup();
	}

	/** Update the beacon state */
	private void updateBeacon() {
		int p = beacon.getPin();
		try {
			beacon.setFlashingNotify(property.getRelay(p));
		}
		catch (IndexOutOfBoundsException e) {
			setErrorStatus("Invalid pin");
		}
	}
}
