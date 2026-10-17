/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
 * Copyright (C) 2026  SRF Consulting Group
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
import us.mn.state.dot.tms.GateArmFlag;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.client.widget.IPanel;

/**
 * Styling for gate arm states
 *
 * @author Douglas Lau
 * @author John L. Stanley - SRF Consulting
 */
public class StateStyle {

	/** Gate arm state */
	private final GateArmState gas;

	/** Text description */
	private final String text;

	/** Gate override in progress */
	private final boolean gate_override;

	/** Create a gate arm state style */
	public StateStyle(GateArm ga) {
		gas = (ga != null)
		    ? GateArmState.fromOrdinal(ga.getArmState())
		    : GateArmState.CLOSED;
		gate_override = GateArmTheme.isGateOverride(ga);
		String t = "";
		if (ga != null) {
			t = gas.toString();
			// Close warning delay, or gate override waiting
			// to close
			if ((GateArmState.CLOSING == gas ||
			     GateArmState.OPEN == gas) &&
			    GateArmFlag.GATE_FLAG_CLOSE_DELAY.checkBit(
			    ga.getStatusFlags()))
				t = "CLOSE_DELAY";
			if (GateArmState.FAULT == gas) {
				String fault = ga.getFault();
				if (fault != null)
					t = fault;
			}
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
			return gate_override ? Color.WHITE : IPanel.DARK_BLUE;
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
			return gate_override
			      ? GateArmTheme.COLOR_GATE_OVERRIDE
			      : null;
		}
	}

	/** Get state text */
	public String text() {
		int len = Math.min(text.length(), 12);
		return text.substring(0, len);
	}
}
