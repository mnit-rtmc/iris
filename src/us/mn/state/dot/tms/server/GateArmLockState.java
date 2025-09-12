/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import us.mn.state.dot.tms.GateArmInterlock;

/**
 * Lock state for a gate arm array.
 *
 * @author Douglas Lau
 */
public class GateArmLockState {

	/** Flag to enable gate arm system */
	private final boolean system_enable;

	/** Create gate arm lock state */
	public GateArmLockState(boolean e) {
		system_enable = e;
		opposing_open = false;
		downstream_closed = false;
		upstream_open = false;
	}

	/** Flag to indicate opposing direction possibly open */
	private boolean opposing_open;

	/** Set flag to indicate opposing direction possibly open */
	public void setOpposingOpen() {
		opposing_open = true;
	}

	/** Flag to indicate downstream gate arm possibly closed */
	private boolean downstream_closed;

	/** Set flag to indicate downstream arm possibly closed */
	public void setDownstreamClosed() {
		downstream_closed = true;
	}

	/** Flag to indicate upstream gate arm possibly open */
	private boolean upstream_open;

	/** Set flag indicating upstream arm possibly open */
	public void setUpstreamOpen() {
		upstream_open = true;
	}

	/** Check if gate open is denied */
	private boolean isOpenDenied() {
		return opposing_open || downstream_closed;
	}

	/** Check if gate close is denied */
	private boolean isCloseDenied() {
		return upstream_open;
	}

	/** Get the interlock enum */
	public GateArmInterlock getInterlock() {
		if (!system_enable)
			return GateArmInterlock.SYSTEM_DISABLE;
		else if (isOpenDenied() && isCloseDenied())
			return GateArmInterlock.DENY_ALL;
		else if (isOpenDenied())
			return GateArmInterlock.DENY_OPEN;
		else if (isCloseDenied())
			return GateArmInterlock.DENY_CLOSE;
		else
			return GateArmInterlock.NONE;
	}
}
