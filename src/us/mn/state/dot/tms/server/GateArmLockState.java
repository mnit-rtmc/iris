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

	/** Flag to indicate opposing direction open */
	private boolean opposing_open = false;

	/** Set flag to indicate opposing direction open.
	 * @param d True if opposing gate open; false otherwise.
	 * @return True if interlock value changed. */
	public boolean setOpposingOpen(boolean o) {
		GateArmInterlock gai = getInterlock();
		opposing_open = o;
		return gai != getInterlock();
	}

	/** Flag to indicate prerequisite gate arm closed (not fully open) */
	private boolean prereq_closed = false;

	/** Set flag to indicate prerequisite gate arm closed (not fully open).
	 * @param d True if prerequisite gate closed; false otherwise.
	 * @return True if interlock value changed. */
	public boolean setPrereqClosed(boolean c) {
		GateArmInterlock gai = getInterlock();
		prereq_closed = c;
		return gai != getInterlock();
	}

	/** Check if gate open is denied */
	public boolean isOpenDenied() {
		return opposing_open || prereq_closed;
	}

	/** Flag to indicate dependant gate arm open */
	private boolean dependant_open = false;

	/** Set flag to indicate dependant gate arm open.
	 * @param o True if dependant gate open; false otherwise.
	 * @return True if interlock value changed. */
	public boolean setDependantOpen(boolean o) {
		GateArmInterlock gai = getInterlock();
		dependant_open = o;
		return gai != getInterlock();
	}

	/** Check if gate close is denied */
	public boolean isCloseDenied() {
		return dependant_open;
	}

	/** Get the interlock enum */
	public GateArmInterlock getInterlock() {
		if(!system_enable)
			return GateArmInterlock.SYSTEM_DISABLE;
		else if(isOpenDenied() && isCloseDenied())
			return GateArmInterlock.DENY_ALL;
		else if(isOpenDenied())
			return GateArmInterlock.DENY_OPEN;
		else if(isCloseDenied())
			return GateArmInterlock.DENY_CLOSE;
		else
			return GateArmInterlock.NONE;
	}

	/** Check if arm open interlock in effect.  When gate arm system is
	 * disabled, open interlock is shut off to allow manual control.
	 * @return True if gate arm open is denied. */
	public boolean isOpenInterlock() {
		return isOpenDenied() && system_enable;
	}
}
