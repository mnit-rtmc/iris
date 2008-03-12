/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.client.device.TrafficDeviceAction;

/**
 * Action to display a plotlet containing ramp meter data
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class MeterDataAction extends TrafficDeviceAction {

	/** Ramp meter to display data */
	protected final MeterProxy proxy;

	/** Desktop to put the plotlet */
	protected final SmartDesktop desktop;

	/** Create a new meter data action */
	public MeterDataAction(MeterProxy p, SmartDesktop d) {
		super(p);
		proxy = p;
		desktop = d;
		putValue(Action.NAME, "Data");
		putValue(Action.SHORT_DESCRIPTION, "Plot meter data.");
		putValue(Action.LONG_DESCRIPTION,
			"Plot historical data for this ramp meter.");
	}

	/** Actually perform the action */
	protected void do_perform() throws Exception {
		desktop.show(new MeterDataForm(proxy));
	}
}
