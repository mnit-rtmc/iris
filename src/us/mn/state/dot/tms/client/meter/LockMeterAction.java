/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.proxy.ProxyAction;

/**
 * This action sets a lock on the selected meter.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class LockMeterAction extends ProxyAction<RampMeter> {

	/** Lock combo box component */
	private final JComboBox lock_cbx;

	/** Create a new action to lock the selected ramp meter */
	public LockMeterAction(RampMeter rm, JComboBox c, boolean e) {
		super("ramp.meter.locked", rm);
		lock_cbx = c;
		setEnabled(e);
	}

	/** Actually perform the action */
	protected void doActionPerformed(ActionEvent e) {
		if(proxy != null) {
			int s = lock_cbx.getSelectedIndex();
			if(s >= 0) {
				Integer lk = new Integer(s);
				if(s == 0)
					lk = null;
				proxy.setMLock(lk);
			}
		}
	}
}
