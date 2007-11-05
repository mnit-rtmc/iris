/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client.meter;

import java.rmi.RemoteException;
import javax.swing.Action;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.device.TrafficDeviceAction;

/**
 * Turns on the selected RampMeter.
 *
 * @author <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @author Douglas Lau
 */
public class TurnOnAction extends TrafficDeviceAction {

	/** Create a new action to turn on the selected ramp meter */
	public TurnOnAction(MeterProxy p) {
		super(p);
		putValue(Action.NAME, "On");
		putValue(Action.SHORT_DESCRIPTION, "Start metering.");
		putValue(Action.LONG_DESCRIPTION, "Turn on the ramp meter.");
	}

	/** Actually perform the action */
	protected void do_perform() throws TMSException, RemoteException {
		MeterProxy p = (MeterProxy)proxy;
		p.meter.startMetering();
	}
}
