/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.meter;

import javax.swing.Action;

/**
 * Shrinks the queue at the selected RampMeter.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class ShrinkQueueAction extends TrafficDeviceAction {

	/** Create a new action the shrink the queue at the selected meter */
	public ShrinkQueueAction(RampMeter p) {
		super(p);
		putValue(Action.NAME, "Shrink");
		putValue(Action.SHORT_DESCRIPTION,
			"Reduce the queue at the ramp.");
		putValue(Action.LONG_DESCRIPTION,
			"Reduce the queue at the ramp by temporarily" +
			" reducing the release rate.");
	}

	/** Actually perform the action */
	protected void do_perform() {
		proxy.shrinkQueue();
	}
}
