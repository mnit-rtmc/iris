/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to change a beacon state.
 *
 * @author Douglas Lau
 */
public class OpChangeBeaconState extends OpDevice<CBWProperty> {

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

	/** Get URI path + query components */
	private String getPathQuery(int pin, boolean on) {
		String m = ControllerHelper.getSetup(controller, "hw", "model");
		Model mdl = Model.fromValue(m);
		return mdl.statePath() + mdl.commandQuery(pin, on);
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

	/** Create the second phase of the operation */
	@Override
	protected Phase<CBWProperty> phaseTwo() {
		return new ChangeBeacon(beacon.getPin());
	}

	/** Phase to change the beacon relay state */
	protected class ChangeBeacon extends Phase<CBWProperty> {

		/** Relay pin (for flasher or verify) */
		private final int pin;

		/** Create change relay phase */
		protected ChangeBeacon(int p) {
			pin = p;
		}

		/** Change the beacon state */
		protected Phase<CBWProperty> poll(
			CommMessage<CBWProperty> mess) throws IOException
		{
			String pq = getPathQuery(pin, flash);
			CBWProperty prop = new CBWProperty(pq);
			mess.add(prop);
			mess.storeProps();
			// After energizing the flashers, turn on the verify
			// circuit (if controlled by a different relay pin)
			Integer vp = beacon.getVerifyPin();
			return (vp != null && vp != pin)
			      ? new ChangeBeacon(vp)
			      : null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			beacon.setFlashingNotify(flash);
		super.cleanup();
	}
}
