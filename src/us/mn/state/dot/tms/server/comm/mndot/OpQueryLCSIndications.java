/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsHelper;
import us.mn.state.dot.tms.LcsIndication;
import us.mn.state.dot.tms.LcsState;
import us.mn.state.dot.tms.server.LcsImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query the indications of a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class OpQueryLCSIndications extends OpLCS {

	/** Device on/off status ("metering" status) */
	private final byte[] status = new byte[1];

	/** Special function output buffer */
	private final byte[] outputs = new byte[2];

	/** Create a new operation to query the LCS */
	public OpQueryLCSIndications(LcsImpl l) {
		super(PriorityLevel.POLL_HIGH, l);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseTwo() {
		return new QueryStatus();
	}

	/** Phase to query the LCS status */
	private class QueryStatus extends Phase<MndotProperty> {

		/** Query the status */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
		{
			MemoryProperty prop = new MemoryProperty(
				Address.RAMP_METER_DATA, status);
			mess.add(prop);
			mess.queryProps();
			return isTurnedOn() ? new QueryOutputs() : null;
		}
	}

	/** Phase to query the LCS special function outputs */
	private class QueryOutputs extends Phase<MndotProperty> {

		/** Query the outputs */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
		{
			MemoryProperty prop = new MemoryProperty(
				Address.SPECIAL_FUNCTION_OUTPUTS, outputs);
			mess.add(prop);
			mess.queryProps();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		lcs.setIndicationsNotify(getIndications());
		super.cleanup();
	}

	/** Test if the LCS array is turned on */
	private boolean isTurnedOn() {
		return status[Address.OFF_STATUS] != MeterStatus.FLASH;
	}

	/** Get the displayed indications */
	private int[] getIndications() {
		if (!isSuccess()) {
			return LcsHelper.makeIndications(
				lcs,
				LcsIndication.UNKNOWN
			);
		}
		int[] ind = LcsHelper.makeIndications(lcs, LcsIndication.DARK);
		if (isTurnedOn()) {
			for (LcsState ls: LcsHelper.lookupStates(lcs)) {
				if (ls.getController() == controller)
					checkIndication(ls, ind);
			}
		}
		return ind;
	}

	/** Check if an indication is set */
	private void checkIndication(LcsState ls, int[] ind) {
		if (Op170.getSpecFuncOutput(outputs, ls.getPin())) {
			int ln = ls.getLane() - 1;
			// We must check bounds here in case the LcsState
			// was added after the "ind" array was created
			if (ln >= 0 && ln < ind.length)
				ind[ln] = ls.getIndication();
		}
	}
}
