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
package us.mn.state.dot.tms.comm;

import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.LCSArrayImpl;
import us.mn.state.dot.tms.SignRequest;

/**
 * LCSPoller is an interface for MessagePoller classes which can poll LCS
 * arrays.
 *
 * @author Douglas Lau
 */
public interface LCSPoller {

	/** Send a sign request */
	void sendRequest(LCSArrayImpl lcs_array, SignRequest r);

	/** Send new indications to an LCS array.
	 * @param lcs_array LCS array.
	 * @param ind New lane use indications.
	 * @param o User who deployed the indications. */
	void sendIndications(LCSArrayImpl lcs_array, Integer[] ind, User o);
}
