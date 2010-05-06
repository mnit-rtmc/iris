/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dmslite;

import java.io.IOException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.utils.SString;

/**
 * Operation to query the configuration of a DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpQueryConfig extends OpDms
{
	/** constructor */
	public OpQueryConfig(DMSImpl d, User u) {
		super(PriorityLevel.DOWNLOAD, d,
			"Retrieving sign configuration", u);
	}

	/** Create the first phase of the operation */
	protected Phase phaseOne() {
		return new PhaseGetConfig(null);
	}

	/** Cleanup the operation */
	public void cleanup() {
		m_dms.setConfigure(success);
		super.cleanup();
	}
}
