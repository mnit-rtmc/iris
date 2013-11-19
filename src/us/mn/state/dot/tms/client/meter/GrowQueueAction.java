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
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.proxy.ProxyAction;

/**
 * Increases the size of the queue at the selected ramp.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class GrowQueueAction extends ProxyAction<RampMeter> {

	/** Create a new action to grow the queue at the selected meter */
	public GrowQueueAction(RampMeter rm, boolean e) {
		super("ramp.meter.grow", rm);
		setEnabled(e);
	}

	/** Actually perform the action */
	protected void doActionPerformed(ActionEvent e) {
		if(proxy != null) {
			Integer rate = proxy.getRate();
			if(rate != null)
				proxy.setRateNext(rate - 150);
		}
	}
}
