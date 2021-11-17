/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2021  Minnesota Department of Transportation
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

	/** Set flag to enable gate arm system */
	public void setSystemEnable(boolean enable) {
		system_enable = enable;
	}

	/** Flag to indicate opposing direction open */
	private boolean opposing_open = false;

	/** Set flag to indicate opposing direction open */
	public void setOpposingOpen(boolean open) {
		opposing_open = open;
	}

	/** Flag to indicate prerequisite gate arm closed (not fully open) */
	private boolean prereq_closed = false;

	/** Set flag to indicate prerequisite arm closed (not fully open) */
	public void setPrereqClosed(boolean closed) {
		prereq_closed = closed;
	}

	/** Flag to indicate dependent gate arm open */
	private boolean dependent_open = false;

	/** Dependency transaction flag.
	 *
	 * Used during dependency check transactions, between calls to
	 * beginDependencies and commitDependencies */
	private boolean dep_open_temp = false;

	/** Begin dependency transaction */
	public void beginDependencies() {
		dep_open_temp = false;
	}

	/** Set temp flag indicating dependent gate arm open */
	public void setDependentOpen() {
		dep_open_temp = true;
	}

	/** Commit dependcy transaction */
	public void commitDependencies() {
		dependent_open = dep_open_temp;
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

	/** Check if gate open is denied */
	private boolean isOpenDenied() {
		return opposing_open || prereq_closed;
	}

	/** Check if gate close is denied */
	private boolean isCloseDenied() {
		return dependent_open;
	}
}
