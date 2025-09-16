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
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.client.widget.IPanel;

/**
 * Styling for gate arm states
 *
 * @author Douglas Lau
 */
public class StateStyle {

	/** Gate arm state */
	private final GateArmState gas;

	/** Text description */
	private final String text;

	/** Create a gate arm state style */
	public StateStyle(GateArm ga) {
		gas = (ga != null)
		    ? GateArmState.fromOrdinal(ga.getArmState())
		    : GateArmState.CLOSED;
		String t = "";
		if (ga != null) {
			t = (GateArmState.FAULT == gas)
				? ga.getFault()
				: gas.toString();
		}
		text = t;
	}

	/** Get foreground color */
	public Color foreground() {
		switch (gas) {
		case UNKNOWN:
			return Color.WHITE;
		case FAULT:
			return Color.BLACK;
		default:
			return IPanel.DARK_BLUE;
		}
	}

	/** Get background color */
	public Color background() {
		switch (gas) {
		case UNKNOWN:
			return Color.GRAY;
		case FAULT:
			return Color.YELLOW;
		default:
			return null;
		}
	}

	/** Get state text */
	public String text() {
		int len = Math.min(text.length(), 12);
		return text.substring(0, len);
	}
}
