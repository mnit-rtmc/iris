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
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Query the state of a beacon
 *
 * @author Douglas Lau
 */
public class OpQueryBeaconState extends OpDevice<CBWProperty> {

	/** Exception message for "Invalid Http response" -- this is fragile,
	 *  since it matches a string literal from the JDK class
	 *  "sun.net.www.protocol.http.HttpUrlConnection" */
	static private final String INVALID_HTTP = "Invalid Http response";

	/** Beacon device */
	private final BeaconImpl beacon;

	/** Relay state property */
	private final CBWProperty property = new CBWProperty("state.xml");

	/** Create a new query beacon state operation */
	public OpQueryBeaconState(BeaconImpl b) {
		super(PriorityLevel.SHORT_POLL, b);
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
			try {
				mess.queryProps();
			}
			catch (IOException e) {
				// X-WR-1R12 models respond to "state.xml" with
				// invalid HTTP; try "stateFull.xml" instead
				if (INVALID_HTTP.equals(e.getMessage())) {
					property.setPath("stateFull.xml");
					mess.queryProps();
				} else
					throw e;
			}
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess()) {
			updateBeacon();
			setMaintStatus(formatMaintStatus());
		}
		super.cleanup();
	}

	/** Update the beacon state */
	private void updateBeacon() {
		beacon.setFlashingNotify(getRelay(beacon.getPin()));
	}

	/** Get relay status */
	private boolean getRelay(int pin) {
		return property.getRelay(pin);
	}

	/** Format the new maintenance status */
	private String formatMaintStatus() {
		Integer vp = beacon.getVerifyPin();
		if (vp != null) {
			boolean f = getRelay(beacon.getPin());
			boolean v = property.getInput(vp);
			if (f && !v)
				return "Verify failed";
			if (v && !f)
				return "Verify stuck";
		}
		return "";
	}
}
