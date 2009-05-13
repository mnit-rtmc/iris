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
package us.mn.state.dot.tms.comm.mndot;

import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.LCSArrayImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DeviceContentionException;
import us.mn.state.dot.tms.comm.Operation;

/**
 * An LCS array operation.
 *
 * @author Douglas Lau
 */
abstract public class LCSOperation extends Controller170Operation {

	/** Get the controller for an LCS array */
	static protected ControllerImpl getController(LCSArrayImpl l) {
		// All the DMS should be assigned to the same
		// controller, so just pick the first one.
		LCS lcs = LCSArrayHelper.lookupLCS(l, 1);
		if(lcs != null) {
			DMS dms = DMSHelper.lookup(lcs.getName());
			if(dms != null) {
				Controller c = dms.getController();
				if(c instanceof ControllerImpl)
					return (ControllerImpl)c;
			}
		}
		return null;
	}

	/** This operation; needed for inner Phase classes */
	protected final LCSOperation operation;

	/** LCS array to query */
	protected final LCSArrayImpl lcs_array;

	/** Create a new LCS operation */
	protected LCSOperation(int p, LCSArrayImpl l) {
		super(p, getController(l));
		operation = this;
		lcs_array = l;
	}

	/** Begin the operation */
	public final void begin() {
		phase = new AcquireArray();
	}

	/** Phase to acquire exclusive ownership of the LCS array */
	protected class AcquireArray extends Phase {

		/** Perform the acquire LCS array phase */
		protected Phase poll(AddressedMessage mess)
			throws DeviceContentionException
		{
			Operation owner = lcs_array.acquire(operation);
			if(owner != operation)
				throw new DeviceContentionException(owner);
			return phaseOne();
		}
	}

	/** Create the first real phase of the operation */
	abstract protected Phase phaseOne();

	/** Cleanup the operation */
	public void cleanup() {
		lcs_array.release(operation);
		super.cleanup();
	}
}
