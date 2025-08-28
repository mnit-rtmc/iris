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
public class PlannedAction {

	/** Device action */
	public final DeviceAction action;

	/** Device being controlled */
	public final DeviceImpl device;

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
		return action.toString() + " on " + device;
	}

	/** Create a new planned action */
	public PlannedAction(DeviceAction da, DeviceImpl d, boolean c,
		String ms, int s, ArrayList<PriceMessageEvent> p)
	{
		action = da;
		device = d;
		condition = c;
		multi = ms;
		sources = s;
		prices = p;
	}
}
