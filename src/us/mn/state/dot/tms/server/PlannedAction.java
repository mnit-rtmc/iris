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

import java.util.ArrayList;
import us.mn.state.dot.tms.DeviceAction;
import us.mn.state.dot.tms.server.event.PriceMessageEvent;

/**
 * A planned action is produced by processing device actions with a
 * TagProcessor.
 *
 * @author Douglas Lau
 */
public class PlannedAction implements Comparable<PlannedAction> {

	/** Device action */
	public final DeviceAction action;

	/** Flag to indicate passing all action tag conditions */
	public final boolean condition;

	/** MULTI string after processing action tags */
	public final String multi;

	/** Sign message sources */
	public final int sources;

	/** Tolling prices */
	public final ArrayList<PriceMessageEvent> prices;

	/** Get a string representation */
	@Override
	public String toString() {
		return "" + action + " condition: " + condition;
	}

	/** Compare to another planned action */
	@Override
	public int compareTo(PlannedAction o) {
		int p = action.getMsgPriority() - o.action.getMsgPriority();
		return (p != 0)
			? p
			: action.getName().compareTo(o.action.getName());
	}

	/** Check actions for equality */
	@Override
	public boolean equals(Object o) {
		return (o instanceof PlannedAction) &&
			action.equals(((PlannedAction) o).action);
	}

	/** Create a new planned action */
	public PlannedAction(DeviceAction da, boolean c, String ms, int s,
		ArrayList<PriceMessageEvent> p)
	{
		action = da;
		condition = c;
		multi = ms;
		sources = s;
		prices = p;
	}
}
