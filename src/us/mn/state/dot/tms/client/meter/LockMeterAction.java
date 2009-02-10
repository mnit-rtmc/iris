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
import javax.swing.JComboBox;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.sonar.ProxyAction;

/**
 * This action sets a lock on the selected meter.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class LockMeterAction extends ProxyAction<RampMeter> {

	/** Lock combo box component */
	protected final JComboBox lockCmb;

	/** Create a new action to lock the selected ramp meter */
	public LockMeterAction(RampMeter p, JComboBox c) {
		super(p);
		lockCmb = c;
		putValue(Action.NAME, "Locked");
		putValue(Action.SHORT_DESCRIPTION,
			"Lock the ramp meter.");
		putValue(Action.LONG_DESCRIPTION,
			"Lock the meter at the current release rate.");
	}

	/** Actually perform the action */
	protected void do_perform() {
		int s = lockCmb.getSelectedIndex();
		if(s >= 0)
			proxy.setMLock(s);
	}
}
