/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dinrelay;

import java.io.IOException;
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to change a beacon state.
 *
 * @author Douglas Lau
 */
public class OpChangeBeaconState extends OpDevice<DinRelayProperty> {

	/** Beacon device */
	private final BeaconImpl beacon;

	/** New state to change beacon */
	private final boolean flash;

	/** Create a new change beacon state operation */
	public OpChangeBeaconState(BeaconImpl b, boolean f) {
		super(PriorityLevel.COMMAND, b);
		beacon = b;
		flash = f;
	}

	/** Operation equality test */
	@Override
	public boolean equals(Object o) {
		if (o instanceof OpChangeBeaconState) {
			OpChangeBeaconState op = (OpChangeBeaconState)o;
			return beacon == op.beacon && flash == op.flash;
		} else
			return false;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<DinRelayProperty> phaseTwo() {
		return new ChangeBeacon();
	}

	/** Phase to change the beacon state */
	protected class ChangeBeacon extends Phase<DinRelayProperty> {

		/** Change the beacon state */
		protected Phase<DinRelayProperty> poll(CommMessage mess)
			throws IOException
		{
			int p = beacon.getPin();
			if (p < 1 && p > 8) {
				setErrorStatus("Invalid pin");
				return null;
			}
			CommandProperty prop = new CommandProperty(p, flash);
			mess.add(prop);
			mess.storeProps();
			beacon.setFlashingNotify(flash);
			return null;
		}
	}
}
