/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSIndication;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;

/**
 * Operation to query the status of a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class LCSQueryStatus extends OpLCS {

	/** Device on/off status ("metering" status) */
	protected final byte[] status = new byte[1];

	/** Special function output buffer */
	protected final byte[] outputs = new byte[2];

	/** Create a new operation to query the LCS */
	public LCSQueryStatus(LCSArrayImpl l) {
		super(DATA_30_SEC, l);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new QueryStatus();
	}

	/** Phase to query the LCS status */
	protected class QueryStatus extends Phase {

		/** Query the status */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new MemoryRequest(Address.RAMP_METER_DATA,
				status));
			mess.getRequest();
			if(isTurnedOn())
				return new QueryOutputs();
			else
				return null;
		}
	}

	/** Phase to query the LCS special function outputs */
	protected class QueryOutputs extends Phase {

		/** Query the outputs */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new MemoryRequest(
				Address.SPECIAL_FUNCTION_OUTPUTS, outputs));
			mess.getRequest();
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(success)
			lcs_array.setIndicationsCurrent(getIndications(), null);
		super.cleanup();
	}

	/** Test if the LCS array is turned on */
	protected boolean isTurnedOn() {
		return status[Address.OFF_STATUS] != MeterStatus.FLASH;
	}

	/** Get the displayed indications */
	protected Integer[] getIndications() {
		final Integer[] ind = new Integer[lcs_array.getLaneCount()];
		for(int i = 0; i < ind.length; i++)
			ind[i] = LaneUseIndication.DARK.ordinal();
		if(isTurnedOn()) {
			lcs_array.findIndications(new Checker<LCSIndication>() {
				public boolean check(LCSIndication li) {
					if(li.getController() == controller)
						checkIndication(li, ind);
					return false;
				}
			});
		}
		return ind;
	}

	/** Check if an indication is set */
	protected void checkIndication(LCSIndication li, Integer[] ind) {
		if(isPinSet(li.getPin())) {
			LCS lcs = li.getLcs();
			int i = lcs.getLane() - 1;
			// We must check bounds here in case the LCSIndication
			// was added after the "ind" array was created
			if(i >= 0 && i < ind.length)
				ind[i] = li.getIndication();
		}
	}

	/** Test if a pin is set in the special function output buffer */
	protected boolean isPinSet(int pin) {
		int i = pin -
			Op170.SPECIAL_FUNCTION_OUTPUT_PIN;
		if(i >= 0 && i < 8)
			return ((outputs[0] >> i) & 1) != 0;
		i -= 8;
		if(i >= 0 && i < 8)
			return ((outputs[1] >> i) & 1) != 0;
		return false;
	}
}
