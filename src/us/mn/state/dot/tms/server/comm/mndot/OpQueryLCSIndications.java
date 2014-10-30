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
import java.util.Iterator;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSIndication;
import us.mn.state.dot.tms.LCSIndicationHelper;
import us.mn.state.dot.tms.server.LCSArrayImpl;
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
	public OpQueryLCSIndications(LCSArrayImpl l) {
		super(PriorityLevel.DATA_30_SEC, l);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseTwo() {
		return new QueryStatus();
	}

	/** Phase to query the LCS status */
	protected class QueryStatus extends Phase<MndotProperty> {

		/** Query the status */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			MemoryProperty prop = new MemoryProperty(
				Address.RAMP_METER_DATA, status);
			mess.add(prop);
			mess.queryProps();
			if (isTurnedOn())
				return new QueryOutputs();
			else
				return null;
		}
	}

	/** Phase to query the LCS special function outputs */
	protected class QueryOutputs extends Phase<MndotProperty> {

		/** Query the outputs */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
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
		if (isSuccess())
			lcs_array.setIndicationsCurrent(getIndications(), null);
		super.cleanup();
	}

	/** Test if the LCS array is turned on */
	private boolean isTurnedOn() {
		return status[Address.OFF_STATUS] != MeterStatus.FLASH;
	}

	/** Get the displayed indications */
	private Integer[] getIndications() {
		Integer[] ind = new Integer[lcs_array.getLaneCount()];
		for (int i = 0; i < ind.length; i++)
			ind[i] = LaneUseIndication.DARK.ordinal();
		if (isTurnedOn()) {
			Iterator<LCSIndication> it =
				LCSIndicationHelper.iterator();
			while (it.hasNext()) {
				LCSIndication li = it.next();
				if (li.getLcs().getArray() == lcs_array) {
					if (li.getController() == controller)
						checkIndication(li, ind);
				}
			}
		}
		return ind;
	}

	/** Check if an indication is set */
	private void checkIndication(LCSIndication li, Integer[] ind) {
		if (Op170.getSpecFuncOutput(outputs, li.getPin())) {
			LCS lcs = li.getLcs();
			int i = lcs.getLane() - 1;
			// We must check bounds here in case the LCSIndication
			// was added after the "ind" array was created
			if (i >= 0 && i < ind.length)
				ind[i] = li.getIndication();
		}
	}
}
