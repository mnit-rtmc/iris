/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
 * Copyright (C) 2008-2010  AHMCT, University of California
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
package us.mn.state.dot.tms.server.comm.dmsxml;

import java.io.IOException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query the configuration of a DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
class OpQueryConfig extends OpDms
{
	/** constructor */
	OpQueryConfig(DMSImpl d, User u) {
		super(PriorityLevel.DOWNLOAD, d,
			"Retrieving sign configuration", u);
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		return new PhaseGetConfig(null);
	}

	/** Cleanup the operation */
	public void cleanup() {
		m_dms.setConfigure(isSuccess());
		super.cleanup();
	}
}
