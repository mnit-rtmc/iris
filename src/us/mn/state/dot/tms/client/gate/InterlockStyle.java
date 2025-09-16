/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.gate;

import java.awt.Color;
import us.mn.state.dot.tms.GateArmInterlock;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Styling for gate arm interlocks
 *
 * @author Douglas Lau
 */
public class InterlockStyle {

	/** Gate arm interlock */
	private final GateArmInterlock interlock;

	/** Create a gate arm interlock style */
	public InterlockStyle(GateArmInterlock gai) {
		interlock = (gai != null)
			? gai
			: GateArmInterlock.SYSTEM_DISABLE;
	}

	/** Create a gate arm interlock style */
	public InterlockStyle(int gai) {
		this(GateArmInterlock.fromOrdinal(gai));
	}

	/** Get foreground color */
	public Color foreground() {
		switch (interlock) {
		case NONE:
		case DENY_CLOSE:
			return Color.BLACK;
		default:
			return Color.WHITE;
		}
	}

	/** Get background color */
	public Color background() {
		switch (interlock) {
		case NONE:
			return Color.GREEN;
		case DENY_CLOSE:
			return Color.YELLOW;
		case DENY_OPEN:
		case DENY_ALL:
			return Color.RED;
		case SYSTEM_DISABLE:
			return Color.GRAY;
		}
	}

	/** Get interlock text */
	public String text() {
		switch (interlock) {
		case NONE:
			return I18N.get("gate.arm.interlock.none");
		case DENY_OPEN:
			return I18N.get("gate.arm.interlock.deny_open");
		case DENY_CLOSE:
			return I18N.get("gate.arm.interlock.deny_close");
		case DENY_ALL:
			return I18N.get("gate.arm.interlock.deny_all");
		case SYSTEM_DISABLE:
			return I18N.get("gate.arm.interlock.system_disable");
		}
	}
}
