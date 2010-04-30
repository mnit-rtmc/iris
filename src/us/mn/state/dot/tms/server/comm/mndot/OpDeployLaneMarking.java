/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.IOException;
import us.mn.state.dot.tms.server.LaneMarkingImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;

/**
 * Operation to deploy a 170 controller lane marking
 *
 * @author Douglas Lau
 */
public class OpDeployLaneMarking extends OpDevice {

	/** Lane marking to deploy */
	protected final LaneMarkingImpl lane_marking;

	/** Deploy flag */
	protected final boolean deploy;

	/** Special function output buffer */
	protected final byte[] outputs = new byte[2];

	/** Create a new deploy lane marking operation */
	public OpDeployLaneMarking(LaneMarkingImpl m, boolean d) {
		super(COMMAND, m);
		lane_marking = m;
		deploy = d;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new QueryOutputs();
	}

	/** Phase to query the special function outputs */
	protected class QueryOutputs extends Phase {

		/** Query the special function outputs */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(new MemoryProperty(
				Address.SPECIAL_FUNCTION_OUTPUTS, outputs));
			mess.queryProps();
			return new SetOutputs();
		}
	}

	/** Phase to set the special function outputs */
	protected class SetOutputs extends Phase {

		/** Set the special function outputs */
		protected Phase poll(CommMessage mess) throws IOException {
			updateOutputs();
			mess.add(new MemoryProperty(
				Address.SPECIAL_FUNCTION_OUTPUTS, outputs));
			mess.storeProps();
			return null;
		}
	}

	/** Update the special function outputs */
	protected void updateOutputs() {
		int pin = lane_marking.getPin();
		if(deploy)
			Op170.setSpecFuncOutput(outputs, pin);
		else
			Op170.clearSpecFuncOutput(outputs, pin);
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(success)
			lane_marking.setDeployedStatus(deploy);
		super.cleanup();
	}
}
