/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2022  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to change a beacon state.
 * This extends OpQueryBeaconState in order to read state.xml as the first
 * phase.  This allows us to format the query parameters depending on the
 * device.
 *
 * @author Douglas Lau
 */
public class OpChangeBeaconState extends OpQueryBeaconState {

	/** New state to change beacon */
	private final boolean flash;

	/** Get beacon relay state */
	@Override
	protected boolean getBeaconRelay() {
		return flash;
	}

	/** Format the maintenance status */
	@Override
	protected String formatMaintStatus() {
		return null;
	}

	/** Create a new change beacon state operation */
	public OpChangeBeaconState(BeaconImpl b, boolean f) {
		super(PriorityLevel.COMMAND, b);
		flash = f;
	}

	/** Operation equality test */
	@Override
	public boolean equals(Object o) {
		if (o instanceof OpChangeBeaconState) {
			OpChangeBeaconState op = (OpChangeBeaconState) o;
			return beacon == op.beacon && flash == op.flash;
		} else
			return false;
	}

	/** Create the third phase of the operation */
	@Override
	protected Phase<CBWProperty> phaseThree() {
		return new ChangeBeacon();
	}

	/** Phase to change the beacon state */
	protected class ChangeBeacon extends Phase<CBWProperty> {

		/** Change the beacon state */
		protected Phase<CBWProperty> poll(
			CommMessage<CBWProperty> mess) throws IOException
		{
			CommandProperty prop = new CommandProperty(getPin(),
				flash);
			mess.add(prop);
			mess.storeProps();
			Integer vp = beacon.getVerifyPin();
			return (vp != null) ? new ChangeVerify(vp) : null;
		}
	}

	/** Phase to change current sensor (verify) circuit */
	protected class ChangeVerify extends Phase<CBWProperty> {

		/** Verify pin */
		private final int pin;

		/** Create change verify phase */
		protected ChangeVerify(int p) {
			pin = p;
		}

		/** Enable verify circuit */
		protected Phase<CBWProperty> poll(
			CommMessage<CBWProperty> mess) throws IOException
		{
			CommandProperty prop = new CommandProperty(pin, flash);
			mess.add(prop);
			mess.storeProps();
			return null;
		}
	}
}
