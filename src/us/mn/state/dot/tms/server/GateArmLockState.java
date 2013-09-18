/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
	private boolean system_enable = false;

	/** Set flag to enable gate arm system.
	 * @param e True to enable gate arm system, or false to disable.
	 * @return True if interlock value changed. */
	public boolean setSystemEnable(boolean e) {
		GateArmInterlock gai = getInterlock();
		system_enable = e;
		return gai != getInterlock();
	}

	/** Flag to deny gate arm open (interlock) */
	private boolean deny_open = true;

	/** Set interlock flag to deny gate open.
	 * @param d True to deny gate open, or false to allow.
	 * @return True if interlock value changed. */
	public boolean setDenyOpen(boolean d) {
		GateArmInterlock gai = getInterlock();
		deny_open = d;
		return gai != getInterlock();
	}

	/** Check if gate open is denied */
	public boolean isOpenDenied() {
		return deny_open;
	}

	/** Flag to deny gate arm close (interlock) */
	private boolean deny_close = false;

	/** Set interlock flag to deny gate close.
	 * @param d True to deny gate close, or false to allow.
	 * @return True if interlock value changed. */
	public boolean setDenyClose(boolean d) {
		GateArmInterlock gai = getInterlock();
		deny_close = d;
		return gai != getInterlock();
	}

	/** Check if gate close is denied */
	public boolean isCloseDenied() {
		return deny_close;
	}

	/** Get the interlock enum */
	public GateArmInterlock getInterlock() {
		if(!system_enable)
			return GateArmInterlock.SYSTEM_DISABLE;
		else if(deny_open && deny_close)
			return GateArmInterlock.DENY_ALL;
		else if(deny_open)
			return GateArmInterlock.DENY_OPEN;
		else if(deny_close)
			return GateArmInterlock.DENY_CLOSE;
		else
			return GateArmInterlock.NONE;
	}

	/** Check if arm open interlock in effect.  When gate arm system is
	 * disabled, open interlock is shut off to allow manual control.
	 * @return True if gate arm open is denied. */
	public boolean isOpenInterlock() {
		return deny_open && system_enable;
	}
}
