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
package us.mn.state.dot.tms.server.comm.ntcip;

import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.LaneUseMultiHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.DeviceContentionException;
import us.mn.state.dot.tms.server.comm.OpDevice;

/**
 * Operation to query indicaitons on a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class OpQueryLCSIndications extends OpLCS {

	/** Current lane */
	protected int lane = 0;

	/** Create a new operation to send LCS indications */
	public OpQueryLCSIndications(LCSArrayImpl l) {
		super(DEVICE_DATA, l);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		if(lane < dmss.length)
			return new LookupIndication();
		else
			return null;
	}

	/** Phase to lookup the indication on the DMS in the current lane */
	protected class LookupIndication extends Phase {

		/** Perform the acquire DMS phase */
		protected Phase poll(AddressedMessage mess)
			throws DeviceContentionException
		{
			DMSImpl dms = dmss[lane];
			if(dms != null)
				lookupDMSIndication(dms);
			lane++;
			if(lane < dmss.length)
				return this;
			else
				return null;
		}
	}

	/** Lookup the indications on the LCS array */
	protected void lookupDMSIndication(DMSImpl dms)
		throws DeviceContentionException
	{
		// We need to acquire access to the DMS to ensure there is no
		// message currently being sent
		OpDevice owner = dms.acquire(operation);
		if(owner != operation)
			throw new DeviceContentionException(owner);
		ind_after[lane] = lookupIndication(dms);
		dms.release(operation);
	}

	/** Lookup an indication on a DMS */
	protected Integer lookupIndication(DMSImpl dms) {
		if(dms.isFailed())
			return null;
		else {
			SignMessage sm = dms.getMessageCurrent();
			return lookupIndication(sm);
		}
	}

	/** Lookup an indication on a sign message */
	protected Integer lookupIndication(SignMessage sm) {
		MultiString ms = new MultiString(sm.getMulti());
		if(ms.isBlank())
			return LaneUseIndication.DARK.ordinal();
		LaneUseMulti lum = LaneUseMultiHelper.find(ms);
		if(lum != null)
			return lum.getIndication();
		else
			return null;
	}
}
