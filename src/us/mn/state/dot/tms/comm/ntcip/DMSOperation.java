/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.tms.DebugLog;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.comm.ChecksumException;
import us.mn.state.dot.tms.comm.Device2Operation;

/**
 * Operation to be performed on a dynamic message sign
 *
 * @author Douglas Lau
 */
abstract public class DMSOperation extends Device2Operation {

	/** DMS debug log */
	static protected final DebugLog DMS_LOG = new DebugLog("dms");

	/** Special duration value for indefinite duration */
	static protected final int DURATION_INDEFINITE = 65535;

	/** Filter message duration (valid for NTCIP) */
	static protected int getDuration(Integer d) {
		if(d == null || d >= DURATION_INDEFINITE)
			return DURATION_INDEFINITE;
		else if(d < 0)
			return 0;
		else
			return d;
	}

	/** DMS to operate */
	protected final DMSImpl dms;

	/** Create a new DMS operation */
	public DMSOperation(int p, DMSImpl d) {
		super(p, d);
		dms = d;
	}

	/** Log exceptions in the DMS debug log */
	public void handleException(IOException e) {
		if(e instanceof ChecksumException) {
			ChecksumException ce = (ChecksumException)e;
			DMS_LOG.log(dms.getName() + " (" + toString() +
				"), " + ce.getScannedData());
		}
		super.handleException(e);
	}

	/** Cleanup the operation */
	public void cleanup() {
		dms.setConfigure(success);
		super.cleanup();
	}
}
