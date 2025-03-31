/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.MeterLock;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.client.proxy.ProxyAction;

/**
 * Turns on the selected RampMeter.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class TurnOnAction extends ProxyAction<RampMeter> {

	/** User ID */
	private final String user;

	/** Create a new action to turn on the selected ramp meter */
	public TurnOnAction(RampMeter rm, String u) {
		super("ramp.meter.on", rm);
		user = u;
	}

	/** Actually perform the action */
	@Override
	protected void doActionPerformed(ActionEvent e) {
		if (proxy != null) {
			MeterLock lk = new MeterLock(proxy.getLock());
			Integer rt = RampMeterHelper.optRate(proxy);
			if (rt == null)
				rt = RampMeterHelper.getMaxRelease();
			lk.setRate(rt);
			lk.setUser(user);
			proxy.setLock(lk.toString());
		}
	}
}
