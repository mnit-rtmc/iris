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

import java.util.Arrays;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.OpDevice;

/**
 * An LCS array operation.
 *
 * @author Douglas Lau
 */
abstract public class OpLCS extends OpDevice {

	/** LCS array to query */
	protected final LCSArrayImpl lcs_array;

	/** Indications before operation */
	protected final Integer[] ind_before;

	/** Indications after operation */
	protected final Integer[] ind_after;

	/** Create a new LCS operation */
	protected OpLCS(int p, LCSArrayImpl l) {
		super(p, l);
		lcs_array = l;
		ind_before = l.getIndicationsCurrent();
		ind_after = Arrays.copyOf(ind_before, ind_before.length);
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(!Arrays.equals(ind_before, ind_after))
			lcs_array.setIndicationsCurrent(ind_after, null);
		super.cleanup();
	}
}
