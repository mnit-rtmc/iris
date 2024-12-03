/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022-2024  Minnesota Department of Transportation
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
 * Query the CBW controller setup
 *
 * @author Douglas Lau
 */
public class OpQuerySetup extends OpDevice<CBWProperty> {

	/** Exception message for "Invalid Http response" -- this is fragile,
	 *  since it matches a string literal from the JDK class
	 *  "sun.net.www.protocol.http.HttpUrlConnection" */
	static private final String INVALID_HTTP = "Invalid Http response";

	/** Create a new query setup operation */
	public OpQuerySetup(BeaconImpl b) {
		super(PriorityLevel.CONFIGURE, b);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<CBWProperty> phaseTwo() {
		return new QueryState();
	}

	/** Phase to query the state */
	private class QueryState extends Phase<CBWProperty> {

		/** Query the state */
		protected Phase<CBWProperty> poll(
			CommMessage<CBWProperty> mess) throws IOException
		{
			CBWProperty prop = new CBWProperty(
				Model.X_301.statePath());
			mess.add(prop);
			try {
				mess.queryProps();
			}
			catch (IOException e) {
				// X-WR-1R12 models respond to "state.xml" with
				// invalid HTTP; try "stateFull.xml" instead
				if (INVALID_HTTP.equals(e.getMessage())) {
					prop.setPathQuery(
						Model.X_WR_1R12.statePath());
					mess.queryProps();
				} else
					throw e;
			}
			controller.setSetupNotify(prop.getSetup());
			return null;
		}
	}
}
