/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2022  SRF Consulting Group
 * Copyright (C) 2021-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ndotbeacon;

import java.io.IOException;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query gate arm status.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class OpQueryBeaconState extends OpNdotBeacon<NdotBeaconProperty> {

	/** Create a new gate arm query status operation */
	public OpQueryBeaconState(BeaconImpl d) {
		super(PriorityLevel.POLL_HIGH, d, false); // priority 2, non-exclusive
		// Retrieve NDOT Beacon status command (NDORv5 "Retrieve Gate Status" command)
		prop = new NdotBeaconProperty("*S#\r\n");
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<NdotBeaconProperty> phaseTwo() {
		return new QueryStatus();
	}

	/** Phase to query the gate status */
	protected class QueryStatus extends Phase<NdotBeaconProperty> {

		/** Query the status */
		protected Phase<NdotBeaconProperty> poll(
			CommMessage<NdotBeaconProperty> mess)
			throws IOException
		{
			mess.add(prop);
			mess.queryProps();
			if (!prop.gotValidResponse())
				throw new ParsingException("NO RESPONSE");
			return null;
		}
	}
}
