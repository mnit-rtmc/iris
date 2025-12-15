/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  SRF Consulting Group
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
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.FutureOp;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to change the state of a
 * Nebraska Beacon (NDOT Beacon).
 *
 * @author John L. Stanley - SRF Consulting
 */
public class OpChangeBeaconState extends OpNdotBeacon<NdotBeaconProperty> {

	/** Beacon device */
	private final BeaconImpl beacon;

	/** New target state for beacon */
	private final boolean flash;

	/** Create a new change beacon state operation */
	public OpChangeBeaconState(BeaconImpl b, boolean f) {
		super(PriorityLevel.COMMAND, b); // priority 1, exclusive
		beacon = b;
		flash = f;
		String cmd;
		if (flash)
			cmd = "*L#\r\n"; // NDOT Beacon ON command (NDORv5 "Lower Gate" command)
		else
			cmd = "*R#\r\n"; // NDOT Beacon OFF command (NDORv5 "Raise Gate" command)
		prop = new NdotBeaconProperty(cmd);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<NdotBeaconProperty> phaseTwo() {
		return new ChangeBeacon();
	}

	/** Phase to change the beacon state */
	protected class ChangeBeacon extends Phase<NdotBeaconProperty> {

		/** Set the state of the beacon controller */
		protected Phase<NdotBeaconProperty> poll(
			CommMessage<NdotBeaconProperty> mess)
			throws IOException
		{
			mess.add(prop);
			mess.storeProps();
			if (!prop.gotValidResponse())
				throw new ParsingException("NO RESPONSE");

			// read the results 2 seconds later
			FutureOp.queueOp(beacon, 2,
				new OpQueryBeaconState(beacon));
			return null;
		}
	}
}
