/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.rmi.RemoteException;
import javax.swing.Action;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.device.TrafficDeviceAction;

/**
 * Turns all modules off on the LaneControlSignal.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class ClearLcsAction extends TrafficDeviceAction {

	protected final String userName;

	/** Create a new action to clear the selected LCS */
	public ClearLcsAction(LcsProxy p, TmsConnection c) {
		super(p);
		putValue(Action.NAME, "Clear");
		putValue(Action.SHORT_DESCRIPTION, "Blank the LCS.");
		putValue(Action.LONG_DESCRIPTION,
			"Blank the Lane Control Signal.");
		userName = c.getUser().getFullName();
	}

	/** Actually perform the action */
	protected void do_perform() throws TMSException, RemoteException {
		LcsProxy lcs = (LcsProxy)proxy;
		int lanes = lcs.getSignals().length;
		int[] newSignals = new int[lanes];
		lcs.setSignals(newSignals, userName);
	}
}
