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
package us.mn.state.dot.tms.server.comm;

import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.User;
import us.mn.state.dot.tms.server.LcsImpl;

/**
 * LCSPoller is an interface for pollers which can send messages to LCS arrays.
 *
 * @author Douglas Lau
 */
public interface LCSPoller {

	/** Send a device request */
	void sendRequest(LcsImpl lcs, DeviceRequest r);

	/** Send new indications to an LCS array.
	 * @param lcs LCS array.
	 * @param lock LCS lock (JSON), or null. */
	void sendIndications(LcsImpl lcs, String lock);
}
