/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query the outlet state of one DIN relay.
 *
 * @author Douglas Lau
 */
public class OpQueryOutlets extends OpDinRelay {

	/** Outlet property */
	private final OutletProperty property;

	/** Create a new operation to query the outlets */
	public OpQueryOutlets(ControllerImpl c, OutletProperty op) {
		super(PriorityLevel.DATA_30_SEC, c);
		property = op;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<DinRelayProperty> phaseOne() {
		return new QueryOutlets();
	}

	/** Phase to query the DIN relay outlet status */
	private class QueryOutlets extends Phase<DinRelayProperty> {

		/** Query the outlet status */
		protected Phase<DinRelayProperty> poll(
			CommMessage<DinRelayProperty> mess) throws IOException
		{
			mess.add(property);
			mess.queryProps();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		property.complete(isSuccess());
		super.cleanup();
	}
}
