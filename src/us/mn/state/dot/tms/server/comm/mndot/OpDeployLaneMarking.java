/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to deploy a 170 controller lane marking.
 *
 * @author Douglas Lau
 */
public class OpDeployLaneMarking extends Op170Device {

	/** Lane marking to deploy */
	private final LaneMarkingImpl lane_marking;

	/** Deploy flag */
	private final boolean deploy;

	/** Special function output buffer */
	private final byte[] outputs = new byte[2];

	/** Create a new deploy lane marking operation */
	public OpDeployLaneMarking(LaneMarkingImpl m, boolean d) {
		super(PriorityLevel.COMMAND, m);
		lane_marking = m;
		deploy = d;
	}

	/** Operation equality test */
	@Override
	public boolean equals(Object o) {
		if (o instanceof OpDeployLaneMarking) {
			OpDeployLaneMarking op = (OpDeployLaneMarking)o;
			return lane_marking == op.lane_marking &&
			       deploy == op.deploy;
		} else
			return false;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseTwo() {
		return new QueryOutputs();
	}

	/** Phase to query the special function outputs */
	protected class QueryOutputs extends Phase<MndotProperty> {

		/** Query the special function outputs */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			MemoryProperty prop = new MemoryProperty(
				Address.SPECIAL_FUNCTION_OUTPUTS, outputs);
			mess.add(prop);
			mess.queryProps();
			return new SetOutputs();
		}
	}

	/** Phase to set the special function outputs */
	protected class SetOutputs extends Phase<MndotProperty> {

		/** Set the special function outputs */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			updateOutputs();
			MemoryProperty prop = new MemoryProperty(
				Address.SPECIAL_FUNCTION_OUTPUTS, outputs);
			mess.add(prop);
			mess.storeProps();
			return null;
		}
	}

	/** Update the special function outputs */
	protected void updateOutputs() {
		int pin = lane_marking.getPin();
		if (deploy)
			Op170.setSpecFuncOutput(outputs, pin);
		else
			Op170.clearSpecFuncOutput(outputs, pin);
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			lane_marking.setDeployedStatus(deploy);
		super.cleanup();
	}
}
